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

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.multiloader.Loader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AllModsLoadedCondition(String... modIds) implements SimpleLoadCondition {
    @Override
    public @NotNull ResourceLocation getConditionId(@NotNull Loader loader) {
        return switch (loader) {
            case FABRIC -> new ResourceLocation("fabric", "all_mods_loaded");
        };
    }

    @Override
    public void writeParameters(@NotNull JsonObject object, @NotNull Loader loader) {
        Preconditions.checkArgument(modIds.length > 0, "Must register at least one mod id.");

        JsonArray mods = new JsonArray();
        for (String modId : modIds) {
            mods.add(modId);
        }
        object.add("values", mods);
    }
}
