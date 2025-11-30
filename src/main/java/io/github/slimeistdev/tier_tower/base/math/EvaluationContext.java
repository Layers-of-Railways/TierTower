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

import java.util.HashSet;
import java.util.Set;

public class EvaluationContext {
    private final Object2DoubleMap<String> variables;
    private final Set<String> finalVariables;

    public EvaluationContext() {
        this.variables = new Object2DoubleOpenHashMap<>();
        this.finalVariables = new HashSet<>();
    }

    public EvaluationContext(Object2DoubleMap<String> variables, Set<String> finalVariables) {
        this.variables = new Object2DoubleOpenHashMap<>(variables);
        this.finalVariables = new HashSet<>(finalVariables);
    }

    public EvaluationContext(EvaluationContext original) {
        this(original.variables, original.finalVariables);
    }

    public EvaluationContext set(@NotNull String name, double value) {
        if (finalVariables.contains(name)) {
            throw new IllegalStateException("Variable '" + name + "' is final and cannot be modified.");
        }
        variables.put(name, value);
        return this;
    }

    public EvaluationContext setFinal(@NotNull String name, double value) {
        set(name, value);
        finalVariables.add(name);
        return this;
    }

    public double getVariable(@NotNull String name) throws EvaluationException {
        if (!variables.containsKey(name)) {
            throw new EvaluationException("Variable '" + name + "' not found in context.");
        }
        return variables.getDouble(name);
    }

    public String[] getVariables() {
        return variables.keySet().toArray(new String[0]);
    }
}
