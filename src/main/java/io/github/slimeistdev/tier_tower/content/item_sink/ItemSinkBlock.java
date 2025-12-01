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
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.foundation.block_entity.IBE;
import io.github.slimeistdev.tier_tower.mixin_ducks.common.ServerPlayer_Duck;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlockEntities;
import io.github.slimeistdev.tier_tower.registry.TierTowerSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class ItemSinkBlock extends BaseEntityBlock implements IBE<ItemSinkBlockEntity> {
    private static final int[][] RESPAWN_OFFSETS = new int[][] {
        {0, 0},
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1},
        {1, 1},
        {-1, -1},
        {1, -1},
        {-1, 1},
    };
    private static final int[] RESPAWN_Y_OFFSETS = new int[] {0, 1, -1, 2, -2};

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
        if (entity instanceof Player && entity.fallDistance >= 6.0f) {
            if (entity instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.isSurvival()) {
                handlePrestige(serverPlayer);
            }
            return;
        }

        super.fallOn(level, state, pos, entity, fallDistance);

        if (entity instanceof ItemEntity itemEntity) {
            tryAbsorbItem(level, pos, itemEntity);
        }
    }

    protected void handlePrestige(@NotNull ServerPlayer player) {
        PlayerTower tower = TierTower.CITY.getTower(player);
        if (tower == null) return;

        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();

        BlockPos lastSafePos = ((ServerPlayer_Duck) player).tt$getLastSafePos();
        if (lastSafePos == null) {
            player.server.getPlayerList().respawn(player, true);
            return;
        }

        Optional<Vec3> standUpPos$ = findStandUpPositionAtOffset(
            player.getType(),
            player.level(),
            lastSafePos
        );
        if (standUpPos$.isEmpty()) {
            player.server.getPlayerList().respawn(player, true);
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10 * 20, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15 * 20, 127, false, false));

        Vec3 standUpPos = standUpPos$.get();
        player.teleportTo(standUpPos.x, standUpPos.y, standUpPos.z);

        boolean success = tower.doPrestige(player);
        Vec3 feet = player.position();
        Vec3 eyes = player.getEyePosition();
        for (int i = 0; i < 10; i++) {
            double t = i / 10.0;
            Vec3 pos = feet.lerp(eyes, t);
            Vector3f color = EminentSnake.randomColor(success ? EminentSnake.COLORS : EminentSnake.BLUE_COLORS, random);
            level.sendParticles(new EminentSnakeParticleOptions(color, 2), pos.x, pos.y, pos.z, 16, 0.25, 0.125, 0.25, 0);
            level.sendParticles(new DustParticleOptions(color, 1.5f), pos.x, pos.y, pos.z, 8, 0.25, 0.125, 0.25, 0);
        }

        if (!success) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 20, 0, false, true, false));
            Vec3 pos = player.position();
            level.playSeededSound(null, pos.x, pos.y, pos.z, TierTowerSoundEvents.EMINENT_SNAKE_STRIKE, SoundSource.BLOCKS, 1.0f, 0.9f, level.random.nextLong());
        }
    }

    private static Optional<Vec3> findStandUpPositionAtOffset(
        EntityType<?> entityType,
        CollisionGetter collisionGetter,
        BlockPos pos
    ) {
        return findStandUpPositionAtOffset(entityType, collisionGetter, pos, true)
            .or(() -> findStandUpPositionAtOffset(entityType, collisionGetter, pos, false));
    }

    private static Optional<Vec3> findStandUpPositionAtOffset(
        EntityType<?> entityType,
        CollisionGetter collisionGetter,
        BlockPos pos,
        boolean onlySafePositions
    ) {
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int yOffset : RESPAWN_Y_OFFSETS) {
            for (int[] offset : RESPAWN_OFFSETS) {
                mutPos.setWithOffset(pos, offset[0], yOffset, offset[1]);
                Vec3 safeLoc = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutPos, onlySafePositions);
                if (safeLoc != null) {
                    return Optional.of(safeLoc);
                }
            }
        }

        return Optional.empty();
    }

    public void tryAbsorbItem(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemEntity itemEntity) {
        if (itemEntity.getOwner() instanceof ServerPlayer serverPlayer) {
            withBlockEntityDo(level, pos, be -> be.tryAbsorbItem(itemEntity, serverPlayer));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ItemSinkBlockEntity be) {
            if (be.isUnbreakable()) return List.of();
        }
        return super.getDrops(state, params);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ItemSinkBlockEntity be) {
            if (be.isUnbreakable()) return 0.0f;
        }

        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag != null && beTag.getBoolean("Unbreakable")) {
            tooltip.add(Component.translatable("tooltip.tier_tower.item_sink.unbreakable").withStyle(ChatFormatting.BLUE));
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
