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

package io.github.slimeistdev.tier_tower.registry;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class TierTowerRecipeSerializers {
    public static final RecipeSerializer<ItemSinkRecipe> ITEM_SINK = register("item_sink", new ItemSinkRecipe.Serializer());

    @SuppressWarnings("SameParameterValue")
    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String key, S recipeSerializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TierTower.asKey(Registries.RECIPE_SERIALIZER, key), recipeSerializer);
    }

    public static void register() {
        TierTower.LOGGER.info("Registering recipe serializers for " + TierTower.NAME);
    }
}
