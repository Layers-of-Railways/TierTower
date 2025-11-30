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

import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.base.math.ast.VariableNode;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

public class MathPreconditions {
    public static @NotNull Node checkFunction(String functionName, String function, String... allowedVariables) {
        Node parsed;
        try {
            parsed = Parser.parse(function);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse " + functionName + " function: " + e.getMessage(), e);
        }

        Set<String> variables = new HashSet<>();
        parsed.visitSelfAndChildren(n -> {
            if (n instanceof VariableNode vn)
                variables.add(vn.name());
        });
        for (String allowed : allowedVariables) {
            variables.remove(allowed);
        }
        if (!variables.isEmpty()) {
            throw new IllegalArgumentException("Invalid variables in " + functionName + " function: " + variables);
        }

        return parsed;
    }

    public static @NotNull Node checkAndEvaluateFunction(String functionName, String function, UnaryOperator<EvaluationContext> contextBuilder) {
        return checkAndEvaluateFunction(functionName, function, contextBuilder.apply(new EvaluationContext()));
    }

    public static @NotNull Node checkAndEvaluateFunction(String functionName, String function, EvaluationContext context) {
        Node parsed = checkFunction(functionName, function, context.getVariables());

        try {
            parsed.evaluate(context);
        } catch (EvaluationException e) {
            throw new IllegalArgumentException("Failed to evaluate " + functionName + " function: " + e.getMessage(), e);
        }

        return parsed;
    }
}
