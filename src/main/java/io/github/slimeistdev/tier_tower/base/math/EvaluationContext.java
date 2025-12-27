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

import io.github.slimeistdev.tier_tower.base.math.ast.CallNode.CallEvaluator;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EvaluationContext {
    private static final Map<String, CallEvaluator> BUILT_IN_FUNCTIONS = new HashMap<>();
    static {
        BUILT_IN_FUNCTIONS.put("sqrt", CallEvaluator.op(Math::sqrt));
        BUILT_IN_FUNCTIONS.put("log", CallEvaluator.op(Math::log));
        BUILT_IN_FUNCTIONS.put("exp", CallEvaluator.op(Math::exp));
        BUILT_IN_FUNCTIONS.put("pow", CallEvaluator.op2(Math::pow));
        BUILT_IN_FUNCTIONS.put("e", CallEvaluator.constant(Math.E));

        BUILT_IN_FUNCTIONS.put("abs", CallEvaluator.op(Math::abs));
        BUILT_IN_FUNCTIONS.put("floor", CallEvaluator.op(Math::floor));
        BUILT_IN_FUNCTIONS.put("ceil", CallEvaluator.op(Math::ceil));
        BUILT_IN_FUNCTIONS.put("round", CallEvaluator.op(Math::round));
        BUILT_IN_FUNCTIONS.put("min", CallEvaluator.many1(args -> {
            double min = args[0];
            for (int i = 1; i < args.length; i++) {
                if (args[i] < min) {
                    min = args[i];
                }
            }
            return min;
        }));
        BUILT_IN_FUNCTIONS.put("max", CallEvaluator.many1(args -> {
            double max = args[0];
            for (int i = 1; i < args.length; i++) {
                if (args[i] > max) {
                    max = args[i];
                }
            }
            return max;
        }));

        BUILT_IN_FUNCTIONS.put("pi", CallEvaluator.constant(Math.PI));
        BUILT_IN_FUNCTIONS.put("sin", CallEvaluator.op(Math::sin));
        BUILT_IN_FUNCTIONS.put("cos", CallEvaluator.op(Math::cos));
        BUILT_IN_FUNCTIONS.put("tan", CallEvaluator.op(Math::tan));
        BUILT_IN_FUNCTIONS.put("atan2", CallEvaluator.op2(Math::atan2));
    }

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

    public CallEvaluator getFunction(@NotNull String name) throws EvaluationException {
        if (!BUILT_IN_FUNCTIONS.containsKey(name)) {
            throw new EvaluationException("Function '" + name + "' not found in context.");
        }
        return BUILT_IN_FUNCTIONS.get(name);
    }

    public String[] getVariables() {
        return variables.keySet().toArray(new String[0]);
    }
}
