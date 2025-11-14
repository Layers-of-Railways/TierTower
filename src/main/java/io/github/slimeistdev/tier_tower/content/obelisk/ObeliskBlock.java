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

package io.github.slimeistdev.tier_tower.content.obelisk;

import io.github.slimeistdev.tier_tower.foundation.block_entity.IBE;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObeliskBlock extends BaseEntityBlock implements IBE<ObeliskBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DisplayFace> DISPLAY_FACE = EnumProperty.create("display_face", DisplayFace.class);

    public ObeliskBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(DISPLAY_FACE, DisplayFace.UP));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        Direction facing = state.getValue(FACING);
        if (hit.getDirection() != facing)
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(state.getBlock().asItem()))
            return InteractionResult.PASS;

        Vec3 relativeHit = hit.getLocation().subtract(Vec3.atCenterOf(hit.getBlockPos()));
        double x = relativeHit.x * facing.getStepZ() - relativeHit.z * facing.getStepX();
        double y = relativeHit.y;

        DisplayFace newFace;
        if (Math.abs(x) <= 2/16f && Math.abs(y) <= 2/16f) {
            newFace = DisplayFace.NONE;
        } else if (Math.abs(x) >= Math.abs(y)) {
            newFace = x > 0 ? DisplayFace.RIGHT : DisplayFace.LEFT;
        } else {
            newFace = y > 0 ? DisplayFace.UP : DisplayFace.DOWN;
        }
        level.setBlock(pos, state.setValue(DISPLAY_FACE, newFace), 3);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, DISPLAY_FACE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Class<ObeliskBlockEntity> getBlockEntityClass() {
        return ObeliskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ObeliskBlockEntity> getBlockEntityType() {
        return TierTowerBlockEntities.OBELISK.get();
    }

    public enum DisplayFace implements StringRepresentable {
        UP("up"),
        RIGHT("right"),
        DOWN("down"),
        LEFT("left"),
        NONE("none");

        private final String name;
        DisplayFace(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
