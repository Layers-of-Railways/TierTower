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

import com.google.gson.JsonObject;
import io.github.slimeistdev.tier_tower.multiloader.Loader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface SimpleLoadCondition extends LoadCondition {
    @ApiStatus.OverrideOnly
    void writeParameters(@NotNull JsonObject object, @NotNull Loader loader);

    @Override
    default @NotNull JsonObject toJson(@NotNull Loader loader) {
        JsonObject object = new JsonObject();
        object.addProperty(LoadCondition.conditionIdKey(loader), getConditionId(loader).toString());
        writeParameters(object, loader);
        return object;
    }
}
