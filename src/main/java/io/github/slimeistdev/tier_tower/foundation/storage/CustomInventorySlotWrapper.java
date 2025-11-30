/*
 * Tier Tower
 * Copyright (c) 2025 The Tier Tower Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.tier_tower.foundation.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * A wrapper around a single slot of an inventory.
 * We must ensure that only one instance of this class exists for every inventory slot,
 * or the transaction logic will not work correctly.
 * This is handled by the Map in CustomInventoryStorageImpl.
 */
@SuppressWarnings("UnstableApiUsage")
public class CustomInventorySlotWrapper extends SingleStackStorage {
    /**
     * The strong reference to the InventoryStorageImpl ensures that the weak value doesn't get GC'ed when individual slots are still being accessed.
     */
    private final CustomInventoryStorageImpl storage;
    final int slot;
    private ItemStack lastReleasedSnapshot = null;

    CustomInventorySlotWrapper(CustomInventoryStorageImpl storage, int slot) {
        this.storage = storage;
        this.slot = slot;
    }

    @Override
    protected ItemStack getStack() {
        return storage.inventory.getItem(slot);
    }

    @Override
    protected void setStack(ItemStack stack) {
        storage.inventory.setItemNoUpdate(slot, stack);
    }

    @Override
    public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        if (!canInsert(slot, insertedVariant.toStack())) {
            return 0;
        }

        return super.insert(insertedVariant, maxAmount, transaction);
    }

    private boolean canInsert(int slot, ItemStack stack) {
        return storage.inventory.canPlaceItem(slot, stack);
    }

    @Override
    public int getCapacity(ItemVariant variant) {
        return Math.min(storage.inventory.getMaxStackSize(), variant.getItem().getMaxStackSize());
    }

    // We override updateSnapshots to also schedule a markDirty call for the backing inventory.
    @Override
    public void updateSnapshots(TransactionContext transaction) {
        storage.markDirtyParticipant.updateSnapshots(transaction);
        super.updateSnapshots(transaction);
    }

    @Override
    protected void releaseSnapshot(ItemStack snapshot) {
        lastReleasedSnapshot = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        // Try to apply the change to the original stack
        ItemStack original = lastReleasedSnapshot;
        ItemStack currentStack = getStack();

        storage.inventory.finalizeSetItem(slot, original, currentStack);

        if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
            // None is empty and the items match: just update the amount and NBT, and reuse the original stack.
            original.setCount(currentStack.getCount());
            original.setTag(currentStack.hasTag() ? currentStack.getTag().copy() : null);
            setStack(original);
        } else {
            // Otherwise assume everything was taken from original so empty it.
            original.setCount(0);
        }
    }

    @Override
    public String toString() {
        return "CustomInventorySlotWrapper[%s#%d]".formatted(DebugMessages.forInventory(storage.inventory), slot);
    }
}
