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

import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemSinkRecipeBuilder {
    private final @NotNull ResourceLocation id;
    private @Nullable Ingredient ingredient;
    private @Nullable Integer points;

    public ItemSinkRecipeBuilder(@NotNull ResourceLocation id) {
        this.id = id;
    }

    public ItemSinkRecipeBuilder require(@NotNull ItemLike... items) {
        return require(Ingredient.of(items));
    }

    public ItemSinkRecipeBuilder require(@NotNull ItemStack... stacks) {
        return require(Ingredient.of(stacks));
    }

    public ItemSinkRecipeBuilder require(@NotNull TagKey<Item> tag) {
        return require(Ingredient.of(tag));
    }

    public ItemSinkRecipeBuilder require(@NotNull Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public ItemSinkRecipeBuilder points(int points) {
        this.points = points;
        return this;
    }

    public void build(@NotNull Consumer<FinishedRecipe> consumer) {
        if (ingredient == null) {
            throw new IllegalStateException("Ingredient must be set");
        }
        if (points == null) {
            throw new IllegalStateException("Points must be set");
        }
        consumer.accept(new DataGenResult(new ItemSinkRecipe(id, ingredient, points)));
    }

    public record DataGenResult(ItemSinkRecipe recipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add("ingredient", recipe.ingredient().toJson());
            json.addProperty("points", recipe.points());
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return recipe.id();
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return recipe.getSerializer();
        }

        @Override
        public @Nullable JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
