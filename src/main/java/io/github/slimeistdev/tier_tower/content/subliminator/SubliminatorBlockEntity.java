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
import io.github.slimeistdev.tier_tower.foundation.block_entity.FluidStorageBlockEntity;
import io.github.slimeistdev.tier_tower.foundation.block_entity.ItemStorageBlockEntity;
import io.github.slimeistdev.tier_tower.foundation.block_entity.TickingBlockEntity;
import io.github.slimeistdev.tier_tower.foundation.fluids.SingleFluidTypeTank;
import io.github.slimeistdev.tier_tower.foundation.storage.WrappedSingleSlotStorage;
import io.github.slimeistdev.tier_tower.registry.TierTowerFluids;
import io.github.slimeistdev.tier_tower.registry.TierTowerMenuTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;
import io.github.slimeistdev.tier_tower.utils.EminenceConstants;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel;
import static net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel;

@SuppressWarnings("UnstableApiUsage")
public class SubliminatorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, TickingBlockEntity, ItemStorageBlockEntity, FluidStorageBlockEntity {
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;

    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    // because shorts
    public static final int DATA_STORED_EMINENCE_LO_LO = 4;
    public static final int DATA_STORED_EMINENCE_LO_HI = 5;
    public static final int DATA_STORED_EMINENCE_HI_LO = 6;
    public static final int DATA_STORED_EMINENCE_HI_HI = 7;
    public static int NUM_DATA_VALUES = 8;

    public static final long MAX_STORED_FLUID = FluidConstants.BUCKET * 4;

    public static final int COOKING_TIME_MULTIPLIER = 20;
    public static final int COOKING_COOL_SPEED = AbstractFurnaceBlockEntity.BURN_COOL_SPEED;

    private final CraftingContainer recipeContainer = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return false;
        }
    }, 1, 1);

    final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
            if (index == SLOT_FUEL)
                return isFuel(stack);

            return true;
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
            ItemStack original = this.items.get(slot);
            boolean same = !stack.isEmpty() && ItemStack.isSameItemSameTags(original, stack);
            this.items.set(slot, stack);
            if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }

            if (slot == SLOT_INPUT && !same) {
                cookingTotalTime = getTotalCookTime(level, SubliminatorBlockEntity.this);
                cookingProgress = 0; // fixme this is getting called as part of abort callbacks
            }

            this.setChanged();
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SubliminatorBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return Container.stillValidBlockEntity(SubliminatorBlockEntity.this, player);
        }

        @Override
        public void fromTag(@NotNull ListTag containerNbt) {
            this.items.clear();

            for (int i = 0; i < containerNbt.size(); i++) {
                CompoundTag slotTag = containerNbt.getCompound(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot < items.size()) {
                    items.set(slot, ItemStack.of(slotTag));
                }
            }

            setChanged();
        }

        @Override
        public @NotNull ListTag createTag() {
            ListTag containerNbt = new ListTag();

            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack stack = items.get(slot);
                if (!stack.isEmpty()) {
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putByte("Slot", (byte)slot);
                    stack.save(slotTag);
                    containerNbt.add(slotTag);
                }
            }

            return containerNbt;
        }
    };

    private record CookingData(int cookingProgress, int cookingTotalTime) {}

    private class CookingDataParticipant extends SnapshotParticipant<CookingData> {
        @Override
        protected CookingData createSnapshot() {
            return new CookingData(cookingProgress, cookingTotalTime);
        }

        @Override
        protected void readSnapshot(CookingData snapshot) {
            if (cookingProgress == snapshot.cookingProgress && cookingTotalTime == snapshot.cookingTotalTime) return;

            cookingProgress = snapshot.cookingProgress;
            cookingTotalTime = snapshot.cookingTotalTime;

            setChanged();
        }
    }

    private final InventoryStorage inventoryWrapper = InventoryStorage.of(inventory, null);
    private final SingleSlotStorage<ItemVariant> inputStorage = new WrappedSingleSlotStorage<>(
        inventoryWrapper.getSlot(SLOT_INPUT),
        new CookingDataParticipant()
    );
    private final SingleSlotStorage<ItemVariant> fuelStorage = inventoryWrapper.getSlot(SLOT_FUEL);

    private final SingleFluidTypeTank eminenceTank = new SingleFluidTypeTank(
        TierTowerFluids.WISPY_EMINENCE.getSource(),
        MAX_STORED_FLUID,
        this::setChanged
    );
    private final Storage<FluidVariant> extractionTank = FilteringStorage.extractOnlyOf(eminenceTank);

    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;
    long clientStoredEminenceFluid;
    @SuppressWarnings("PointlessBitwiseExpression")
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTime;
                case DATA_LIT_DURATION -> litDuration;
                case DATA_COOKING_PROGRESS -> cookingProgress;
                case DATA_COOKING_TOTAL_TIME -> cookingTotalTime;
                case DATA_STORED_EMINENCE_LO_LO -> Math.toIntExact((getStoredEminenceFluid() & 0x0000_0000_0000_FFFFL) >>>  0);
                case DATA_STORED_EMINENCE_LO_HI -> Math.toIntExact((getStoredEminenceFluid() & 0x0000_0000_FFFF_0000L) >>> 16);
                case DATA_STORED_EMINENCE_HI_LO -> Math.toIntExact((getStoredEminenceFluid() & 0x0000_FFFF_0000_0000L) >>> 32);
                case DATA_STORED_EMINENCE_HI_HI -> Math.toIntExact((getStoredEminenceFluid() & 0xFFFF_0000_0000_0000L) >>> 48);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LIT_TIME:
                    litTime = value;
                    break;
                case DATA_LIT_DURATION:
                    litDuration = value;
                    break;
                case DATA_COOKING_PROGRESS:
                    cookingProgress = value;
                    break;
                case DATA_COOKING_TOTAL_TIME:
                    cookingTotalTime = value;
                    break;
                case DATA_STORED_EMINENCE_LO_LO:
                    clientStoredEminenceFluid = (clientStoredEminenceFluid & 0xFFFF_FFFF_FFFF_0000L) | ((value & 0xFFFFL) <<  0);
                    break;
                case DATA_STORED_EMINENCE_LO_HI:
                    clientStoredEminenceFluid = (clientStoredEminenceFluid & 0xFFFF_FFFF_0000_FFFFL) | ((value & 0xFFFFL) << 16);
                    break;
                case DATA_STORED_EMINENCE_HI_LO:
                    clientStoredEminenceFluid = (clientStoredEminenceFluid & 0xFFFF_0000_FFFF_FFFFL) | ((value & 0xFFFFL) << 32);
                    break;
                case DATA_STORED_EMINENCE_HI_HI:
                    clientStoredEminenceFluid = (clientStoredEminenceFluid & 0x0000_FFFF_FFFF_FFFFL) | ((value & 0xFFFFL) << 48);
                    break;
            }
        }

        @Override
        public int getCount() {
            return NUM_DATA_VALUES;
        }
    };
    private final RecipeManager.CachedCheck<Container, ? extends ItemSinkRecipe> quickCheck;

    public SubliminatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.quickCheck = RecipeManager.createCheck(TierTowerRecipeTypes.ITEM_SINK);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.tier_tower.subliminator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new SubliminatorMenu(TierTowerMenuTypes.SUBLIMINATOR.get(), containerId, inventory, this.inventory, this.dataAccess);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {}

    private static int getTotalCookTime(Level level, SubliminatorBlockEntity be) {
        be.recipeContainer.setItem(0, be.inventory.getItem(SLOT_INPUT));
        int cookTime = be.quickCheck.getRecipeFor(be.recipeContainer, level).map(r -> r.points() * COOKING_TIME_MULTIPLIER).orElse(200);
        be.recipeContainer.setItem(0, ItemStack.EMPTY);
        return cookTime;
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    private int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return getFuel().getOrDefault(item, 0);
        }
    }

    private long getStoredEminenceFluid() {
        return level == null || level.isClientSide ? clientStoredEminenceFluid : eminenceTank.amount;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items", CompoundTag.TAG_LIST)) {
            this.inventory.fromTag(tag.getList("Items", CompoundTag.TAG_COMPOUND));
        } else {
            this.inventory.clearContent();
        }
        this.litTime = tag.getShort("BurnTime");
        this.cookingProgress = tag.getShort("CookTime");
        this.cookingTotalTime = tag.getShort("CookTimeTotal");
        this.litDuration = getBurnDuration(inventory.getItem(SLOT_FUEL));

        this.eminenceTank.readNbt(tag.contains("Tank") ? tag.getCompound("Tank") : null);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort("BurnTime", (short)this.litTime);
        tag.putShort("CookTime", (short)this.cookingProgress);
        tag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
        tag.put("Items", this.inventory.createTag());

        CompoundTag tankNbt = new CompoundTag();
        this.eminenceTank.writeNbt(tankNbt);
        tag.put("Tank", tankNbt);
    }

    private boolean canSublimate(@Nullable ItemSinkRecipe recipe) {
        try (Transaction tx = Transaction.openOuter()) {
            return sublimate(recipe, tx); // will be automatically rolled back
        }
    }

    private void sublimate(@Nullable ItemSinkRecipe recipe) {
        try (Transaction tx = Transaction.openOuter()) {
            if (!sublimate(recipe, tx)) return;
            tx.commit();
        }
    }

    private boolean sublimate(@Nullable ItemSinkRecipe recipe, @NotNull TransactionContext ctx) {
        if (recipe == null) return false;

        int eminence = recipe.points();
        if (eminence <= 0) return false;

        long fluidAmount = (long) eminence * EminenceConstants.FLUID_PER_EMINENCE;

        try (Transaction tx = Transaction.openNested(ctx)) {
            if (eminenceTank.insert(eminenceTank.getNonBlankVariant(), fluidAmount, tx) != fluidAmount) return false;

            ItemVariant inputItem = inputStorage.getResource();
            if (!recipe.matches(inputItem)) return false;
            if (inputStorage.extract(inputItem, 1, tx) != 1) return false;

            // all good
            tx.commit();
        }

        return true;
    }

    @SuppressWarnings("ConstantValue") // Intelli'ntJ
    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        boolean wasLit = isLit();
        boolean changed = false;
        if (isLit()) {
            litTime--;
        }

        ItemStack fuelStack = inventory.getItem(SLOT_FUEL);
        boolean hasInput = !inventory.getItem(SLOT_INPUT).isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();
        if (isLit() || hasFuel && hasInput) {
            ItemSinkRecipe recipe;
            if (hasInput) {
                recipeContainer.setItem(0, inventory.getItem(SLOT_INPUT));
                recipe = quickCheck.getRecipeFor(recipeContainer, level).orElse(null);
                recipeContainer.setItem(0, ItemStack.EMPTY);
            } else {
                recipe = null;
            }

            if (!isLit() && canSublimate(recipe)) {
                litTime = getBurnDuration(fuelStack);
                litDuration = litTime;
                if (isLit()) {
                    changed = true;
                    if (hasFuel) {
                        Item fuelItem = fuelStack.getItem();
                        fuelStack.shrink(1);
                        if (fuelStack.isEmpty()) {
                            Item remainderItem = fuelItem.getCraftingRemainingItem();
                            inventory.setItem(SLOT_FUEL, remainderItem == null ? ItemStack.EMPTY : new ItemStack(remainderItem));
                        }
                    }
                }
            }

            // can't just do a wrapping transaction, since that would reset cooking progress
            if (isLit() && canSublimate(recipe)) {
                cookingProgress++;
                if (cookingProgress == cookingTotalTime) {
                    cookingProgress = 0;
                    cookingTotalTime = getTotalCookTime(level, this);
                    sublimate(recipe);
                    changed = true;
                }
            } else {
                cookingProgress = 0;
            }
        } else if (!isLit() && cookingProgress > 0) {
            //       ^ this is not, in fact, a constant value
            cookingProgress = Mth.clamp(cookingProgress - COOKING_COOL_SPEED, 0, cookingTotalTime);
        }

        if (wasLit != isLit()) {
            changed = true;
            state = state.setValue(SubliminatorBlock.LIT, isLit());
            level.setBlock(getBlockPos(), state, Block.UPDATE_ALL);
        }

        if (changed) {
            setChanged(level, getBlockPos(), state);
        }
    }

    @Override
    public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        if (side == null)
            return null;

        return side == Direction.UP ? inputStorage : fuelStorage;
    }

    @Override
    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return extractionTank;
    }
}
