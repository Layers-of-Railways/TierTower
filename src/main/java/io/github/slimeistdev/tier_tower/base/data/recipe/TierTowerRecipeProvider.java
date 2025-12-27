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

package io.github.slimeistdev.tier_tower.base.data.recipe;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlocks;
import io.github.slimeistdev.tier_tower.registry.TierTowerItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class TierTowerRecipeProvider extends RecipeProvider {
    protected final List<GeneratedRecipe> all = new ArrayList<>();

    public TierTowerRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        all.forEach(c -> c.register(writer));
        TierTower.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    @FunctionalInterface
    public interface GeneratedRecipe {
        void register(Consumer<FinishedRecipe> consumer);
    }

    public static class I {
        public static ItemLike diamond() {
            return Items.DIAMOND;
        }

        public static ItemLike honeycomb() {
            return Items.HONEYCOMB;
        }

        public static ItemLike bottledEminence() {
            return TierTowerItems.BOTTLED_EMINENCE;
        }

        public static ItemLike blastFurnace() {
            return Blocks.BLAST_FURNACE;
        }

        public static ItemLike ironBlock() {
            return Blocks.IRON_BLOCK;
        }

        public static ItemLike ironBars() {
            return Blocks.IRON_BARS;
        }

        public static ItemLike netheriteIngot() {
            return Items.NETHERITE_INGOT;
        }

        public static ItemLike deepslatePillar() {
            return TierTowerBlocks.DEEPSLATE_PILLAR;
        }

        public static ItemLike deepslateBricks() {
            return Blocks.DEEPSLATE_BRICKS;
        }

        public static ItemLike deepslateTiles() {
            return Blocks.DEEPSLATE_TILES;
        }

        public static ItemLike glass() {
            return Blocks.GLASS;
        }

        public static ItemLike amethystShard() {
            return Items.AMETHYST_SHARD;
        }

        public static ItemLike goldIngot() {
            return Items.GOLD_INGOT;
        }

        public static ItemLike eminentGlass() {
            return TierTowerBlocks.EMINENT_GLASS;
        }
    }
}
