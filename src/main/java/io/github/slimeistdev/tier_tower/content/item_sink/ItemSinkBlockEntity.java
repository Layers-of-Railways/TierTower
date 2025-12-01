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

package io.github.slimeistdev.tier_tower.content.item_sink;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import io.github.slimeistdev.tier_tower.foundation.block_entity.TickingBlockEntity;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemSinkBlockEntity extends BlockEntity implements TickingBlockEntity {
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
    private final RecipeManager.CachedCheck<Container, ? extends ItemSinkRecipe> quickCheck;

    private final List<EminentSnake> snakes = new ArrayList<>();
    private final Map<UUID, EminentSnake> recentSnakes = new HashMap<>();
    private boolean isUnbreakable;

    public ItemSinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        quickCheck = RecipeManager.createCheck(TierTowerRecipeTypes.ITEM_SINK);
    }

    public boolean isUnbreakable() {
        return isUnbreakable;
    }

    @Override
    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            if (!snakes.isEmpty()) setChanged();
            snakes.removeIf(snake -> !snake.tick(serverLevel, worldPosition));
        }
    }

    private void addSnake(int points, @NotNull ServerPlayer beneficiary) {
        UUID uuid = beneficiary.getUUID();

        var recent = recentSnakes.get(uuid);
        if (recent != null && recent.age >= 0 && recent.age <= EminentSnake.DEBOUNCE) {
            recent.points += points;
            recent.age /= 2;
            setChanged();
            return;
        }

        var tower = TierTower.CITY.getTower(beneficiary);
        if (tower != null) {
            points = tower.applyPrestigeToPoints(points, beneficiary.getRandom());
        }

        EminentSnake snake = new EminentSnake(uuid, points);
        snake.owner = beneficiary;
        snakes.add(snake);
        recentSnakes.put(uuid, snake);
        setChanged();
    }

    public void tryAbsorbItem(@NotNull ItemEntity item, @NotNull ServerPlayer beneficiary) {
        if (level == null) return;

        recipeContainer.setItem(0, item.getItem());
        var recipe$ = quickCheck.getRecipeFor(recipeContainer, level);
        recipeContainer.setItem(0, ItemStack.EMPTY);
        if (recipe$.isEmpty()) return;
        var recipe = recipe$.get();

        int points = recipe.points() * item.getItem().getCount();
        item.discard();

        addSnake(points, beneficiary);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Snakes", CompoundTag.TAG_LIST)) {
            ListTag snakes = tag.getList("Snakes", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < snakes.size(); i++) {
                this.snakes.add(EminentSnake.load(snakes.getCompound(i), worldPosition));
            }
        }

        isUnbreakable = tag.getBoolean("Unbreakable");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        if (!snakes.isEmpty()) {
            ListTag snakes = new ListTag();
            for (EminentSnake snake : this.snakes) {
                CompoundTag snakeTag = new CompoundTag();
                snake.save(snakeTag);
                snakes.add(snakeTag);
            }
            tag.put("Snakes", snakes);
        }

        tag.putBoolean("Unbreakable", isUnbreakable);
    }
}
