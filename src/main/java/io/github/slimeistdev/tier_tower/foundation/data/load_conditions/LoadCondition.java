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

package io.github.slimeistdev.tier_tower.foundation.data.load_conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.multiloader.Loader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface LoadCondition {
    static void writeCondition(@NotNull JsonObject resource, @NotNull LoadCondition condition) {
        JsonArray fabricConditions = new JsonArray();
        fabricConditions.add(condition.toJson(Loader.FABRIC));
        resource.add("fabric:load_conditions", fabricConditions);
    }

    static @NotNull String conditionIdKey(Loader loader) {
        return switch (loader) {
            case FABRIC -> "condition";
        };
    }

    static @NotNull LoadCondition not(LoadCondition condition) {
        return new NotCondition(condition);
    }

    static @NotNull LoadCondition allModsLoaded(@NotNull String... modIds) {
        return new AllModsLoadedCondition(modIds);
    }

    static @NotNull LoadCondition anyModLoaded(@NotNull String... modIds) {
        return new AnyModLoadedCondition(modIds);
    }

    static @NotNull LoadCondition modLoaded(@NotNull String modId) {
        return allModsLoaded(modId);
    }

    static @NotNull LoadCondition modMissing(@NotNull String modId) {
        return not(anyModLoaded(modId));
    }

    @NotNull ResourceLocation getConditionId(@NotNull Loader loader);
    @NotNull JsonObject toJson(@NotNull Loader loader);
}
