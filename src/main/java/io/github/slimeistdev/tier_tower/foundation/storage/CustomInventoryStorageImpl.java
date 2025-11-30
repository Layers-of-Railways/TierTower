/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 * Copyright (c) 2025 The Tier Tower Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.slimeistdev.tier_tower.foundation.storage;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
class CustomInventoryStorageImpl extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> implements CustomInventoryStorage {
    private static final Map<UpdateFreeContainer, CustomInventoryStorageImpl> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static CustomInventoryStorage of(UpdateFreeContainer inventory, @Nullable Direction direction) {
        CustomInventoryStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, inv -> {
            if (inv instanceof Inventory /*playerInventory*/) {
                throw new IllegalArgumentException("Cannot wrap player inventories");
            } else {
                return new CustomInventoryStorageImpl(inv);
            }
        });
        storage.resizeSlotList();
        return storage.getSidedWrapper(direction);
    }

    final UpdateFreeContainer inventory;
    /**
     * This {@code backingList} is the real list of wrappers.
     * The {@code parts} in the superclass is the public-facing unmodifiable sublist with exactly the right amount of slots.
     */
    final List<CustomInventorySlotWrapper> backingList;
    /**
     * This participant ensures that markDirty is only called once for the entire inventory.
     */
    final MarkDirtyParticipant markDirtyParticipant = new MarkDirtyParticipant();

    CustomInventoryStorageImpl(UpdateFreeContainer inventory) {
        super(Collections.emptyList());
        this.inventory = inventory;
        this.backingList = new ArrayList<>();
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots() {
        return parts;
    }

    /**
     * Resize slot list to match the current size of the inventory.
     */
    private void resizeSlotList() {
        int inventorySize = inventory.getContainerSize();

        // If the public-facing list must change...
        if (inventorySize != parts.size()) {
            // Ensure we have enough wrappers in the backing list.
            while (backingList.size() < inventorySize) {
                backingList.add(new CustomInventorySlotWrapper(this, backingList.size()));
            }

            // Update the public-facing list.
            parts = Collections.unmodifiableList(backingList.subList(0, inventorySize));
        }
    }

    private CustomInventoryStorage getSidedWrapper(@Nullable Direction direction) {
        if (inventory instanceof WorldlyContainer && direction != null) {
            throw new NotImplementedException("Wrapping WorldlyContainer is not implemented yet");
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "CustomInventoryStorage[" + DebugMessages.forInventory(inventory) + "]";
    }

    class MarkDirtyParticipant extends SnapshotParticipant<Unit> {
        @Override
        protected Unit createSnapshot() {
            return Unit.INSTANCE;
        }

        @Override
        protected void readSnapshot(Unit snapshot) {}

        @Override
        protected void onFinalCommit() {
            inventory.setChanged();
        }
    }
}
