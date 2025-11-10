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

package io.github.slimeistdev.tier_tower.content.item_sink.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeSerializers;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ItemSinkRecipe(ResourceLocation id, Ingredient ingredient, int points) implements Recipe<Container> {
    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TierTowerRecipeSerializers.ITEM_SINK;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TierTowerRecipeTypes.ITEM_SINK;
    }

    public static class Serializer implements RecipeSerializer<ItemSinkRecipe> {
        @Override
        public @NotNull ItemSinkRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
            JsonElement ingredient$ = GsonHelper.isArrayNode(serializedRecipe, "ingredient")
                ? GsonHelper.getAsJsonArray(serializedRecipe, "ingredient")
                : GsonHelper.getAsJsonObject(serializedRecipe, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(ingredient$, false);
            int points = GsonHelper.getAsInt(serializedRecipe, "points");
            return new ItemSinkRecipe(recipeId, ingredient, points);
        }

        @Override
        public @NotNull ItemSinkRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            return new ItemSinkRecipe(recipeId, Ingredient.fromNetwork(buffer), buffer.readVarInt());
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull ItemSinkRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeVarInt(recipe.points);
        }
    }
}
