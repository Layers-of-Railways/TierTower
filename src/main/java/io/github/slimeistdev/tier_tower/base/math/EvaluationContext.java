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

package io.github.slimeistdev.tier_tower.base.math;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class EvaluationContext {
    private final Object2DoubleMap<String> variables;

    public EvaluationContext() {
        this.variables = new Object2DoubleOpenHashMap<>();
    }

    public EvaluationContext(Object2DoubleMap<String> variables) {
        this.variables = new Object2DoubleOpenHashMap<>(variables);
    }

    public EvaluationContext set(@NotNull String name, double value) {
        variables.put(name, value);
        return this;
    }

    public double getVariable(@NotNull String name) throws EvaluationException {
        if (!variables.containsKey(name)) {
            throw new EvaluationException("Variable '" + name + "' not found in context.");
        }
        return variables.getDouble(name);
    }
}
