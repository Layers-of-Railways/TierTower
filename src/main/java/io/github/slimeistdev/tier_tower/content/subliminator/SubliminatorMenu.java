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

package io.github.slimeistdev.tier_tower.content.subliminator;

import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;

import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_COOKING_PROGRESS;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_COOKING_TOTAL_TIME;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_LIT_DURATION;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_LIT_TIME;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_STORED_EMINENCE_HI_HI;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_STORED_EMINENCE_HI_LO;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_STORED_EMINENCE_LO_HI;
import static io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity.DATA_STORED_EMINENCE_LO_LO;

public class SubliminatorMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int DATA_COUNT = SubliminatorBlockEntity.NUM_DATA_VALUES;
    private static final int INV_SLOT_START = SLOT_COUNT;
    private static final int INV_SLOT_END = INV_SLOT_START + 27;
    private static final int USE_ROW_SLOT_START = INV_SLOT_END;
    private static final int USE_ROW_SLOT_END = USE_ROW_SLOT_START + 9;
    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final RecipeType<? extends ItemSinkRecipe> recipeType;

    public SubliminatorMenu(
        MenuType<? extends SubliminatorMenu> menuType,
        int containerId,
        Inventory playerInventory
    ) {
        this(
            menuType,
            containerId,
            playerInventory,
            new SimpleContainer(SLOT_COUNT),
            new SimpleContainerData(DATA_COUNT)
        );
    }

    public SubliminatorMenu(
        MenuType<? extends SubliminatorMenu> menuType,
        int containerId,
        Inventory playerInventory,
        Container container,
        ContainerData data
    ) {
        super(menuType, containerId);
        this.recipeType = TierTowerRecipeTypes.ITEM_SINK;
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.level();
        this.addSlot(new Slot(container, INPUT_SLOT, 56, 17));
        this.addSlot(new SubliminatorFuelSlot(container, FUEL_SLOT, 56, 53));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i *  9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return container.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack originalStack;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();
            if (index != INPUT_SLOT && index != FUEL_SLOT) { // inventory was clicked
                if (canSubliminate(slotStack)) {
                    if (!moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isFuel(slotStack)) {
                    if (!moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END) {
                    if (!moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        } else {
            originalStack = ItemStack.EMPTY;
        }

        return originalStack;
    }

    private boolean canSubliminate(@NotNull ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(recipeType, new SimpleContainer(stack), level).isPresent();
    }

    private boolean isFuel(@NotNull ItemStack stack) {
        return AbstractFurnaceBlockEntity.isFuel(stack);
    }

    public int getSubliminationProgress() {
        int cookingProgress = data.get(DATA_COOKING_PROGRESS);
        int cookingTotalTime = data.get(DATA_COOKING_TOTAL_TIME);
        return cookingProgress == 0 || cookingTotalTime == 0 ? 0 : cookingProgress * 24 / cookingTotalTime;
    }

    public int getLitProgress() {
        int litDuration = data.get(DATA_LIT_DURATION);
        if (litDuration == 0) litDuration = 200;

        return data.get(DATA_LIT_TIME) * 13 / litDuration;
    }

    public boolean isLit() {
        return data.get(DATA_LIT_TIME) > 0;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public long getStoredFluid() {
        return ((long) data.get(DATA_STORED_EMINENCE_HI_HI) & 0xFFFFL) << 48L
            |  ((long) data.get(DATA_STORED_EMINENCE_HI_LO) & 0xFFFFL) << 32L
            |  ((long) data.get(DATA_STORED_EMINENCE_LO_HI) & 0xFFFFL) << 16L
            |  ((long) data.get(DATA_STORED_EMINENCE_LO_LO) & 0xFFFFL) <<  0L;
    }
}