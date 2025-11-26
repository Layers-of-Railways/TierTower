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

package io.github.slimeistdev.tier_tower.base.data;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import io.github.slimeistdev.tier_tower.foundation.block_entity.FluidStorageBlockEntity;
import io.github.slimeistdev.tier_tower.foundation.block_entity.ItemStorageBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class BuilderTransformers {
    private static ResourceLocation extend(ResourceLocation rl, String suffix) {
        return new ResourceLocation(rl.getNamespace(), rl.getPath() + suffix);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> lightLevel(int level) {
        return b -> b.properties(p -> p.lightLevel(s -> level));
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> lightLevel(ToIntFunction<BlockState> lightEmission) {
        return b -> b.properties(p -> p.lightLevel(lightEmission));
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> eminentBlock() {
        return b -> b.transform(lightLevel(7));
    }

    public static <T extends RotatedPillarBlock, P> NonNullUnaryOperator<BlockBuilder<T, P>> pillarState() {
        return b -> b.blockstate((c, p) -> p.axisBlock(
            c.get(),
            extend(p.blockTexture(c.get()), "_side"),
            extend(p.blockTexture(c.get()), "_top")
        ));
    }

    public static <T extends BlockEntity, P, A, C> NonNullUnaryOperator<BlockEntityBuilder<T, P>> blockEntityApi(
        BlockApiLookup<A, C> lookup,
        BiFunction<? super T, C, @Nullable A> apiProvider
    ) {
        return b -> b.onRegister(bet ->
            lookup.registerForBlockEntity(apiProvider, bet));
    }

    public static <T extends BlockEntity & ItemStorageBlockEntity, P> NonNullUnaryOperator<BlockEntityBuilder<T, P>> itemStorage() {
        return blockEntityApi(ItemStorage.SIDED, ItemStorageBlockEntity::getItemStorage);
    }

    public static <T extends BlockEntity & FluidStorageBlockEntity, P> NonNullUnaryOperator<BlockEntityBuilder<T, P>> fluidStorage() {
        return blockEntityApi(FluidStorage.SIDED, FluidStorageBlockEntity::getFluidStorage);
    }
}
