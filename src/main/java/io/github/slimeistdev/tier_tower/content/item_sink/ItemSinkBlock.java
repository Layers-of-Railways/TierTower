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

import io.github.slimeistdev.tier_tower.foundation.block_entity.IBE;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemSinkBlock extends BaseEntityBlock implements IBE<ItemSinkBlockEntity> {
    public ItemSinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity instanceof ItemEntity itemEntity) {
            tryAbsorbItem(level, pos, itemEntity);
        }
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);

        if (entity instanceof ItemEntity itemEntity) {
            tryAbsorbItem(level, pos, itemEntity);
        }
    }

    public void tryAbsorbItem(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemEntity itemEntity) {
        if (itemEntity.getOwner() instanceof ServerPlayer serverPlayer) {
            withBlockEntityDo(level, pos, be -> be.tryAbsorbItem(itemEntity, serverPlayer));
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Class<ItemSinkBlockEntity> getBlockEntityClass() {
        return ItemSinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ItemSinkBlockEntity> getBlockEntityType() {
        return TierTowerBlockEntities.ITEM_SINK.get();
    }
}
