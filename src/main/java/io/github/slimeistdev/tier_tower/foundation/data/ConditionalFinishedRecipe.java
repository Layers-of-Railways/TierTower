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

package io.github.slimeistdev.tier_tower.foundation.data;

import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.foundation.data.load_conditions.LoadCondition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ConditionalFinishedRecipe implements FinishedRecipe {
    private final @NotNull FinishedRecipe delegate;
    private final @NotNull LoadCondition condition;

    public ConditionalFinishedRecipe(@NotNull FinishedRecipe delegate, @NotNull LoadCondition condition) {
        this.delegate = delegate;
        this.condition = condition;
    }

    public static @NotNull FinishedRecipe wrap(@NotNull FinishedRecipe recipe, @Nullable LoadCondition condition) {
        return condition == null ? recipe : new ConditionalFinishedRecipe(recipe, condition);
    }

    public static @NotNull Consumer<FinishedRecipe> wrap(@NotNull Consumer<FinishedRecipe> consumer, @Nullable LoadCondition condition) {
        return condition == null ? consumer : (recipe) -> consumer.accept(wrap(recipe, condition));
    }

    @Override
    public void serializeRecipeData(@NotNull JsonObject json) {
        delegate.serializeRecipeData(json);
        LoadCondition.writeCondition(json, condition);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public @NotNull RecipeSerializer<?> getType() {
        return delegate.getType();
    }

    @Override
    public @Nullable JsonObject serializeAdvancement() {
        JsonObject adv = delegate.serializeAdvancement();
        if (adv != null)
            LoadCondition.writeCondition(adv, condition);

        return adv;
    }

    @Override
    public @Nullable ResourceLocation getAdvancementId() {
        return delegate.getAdvancementId();
    }
}
