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

import com.mojang.datafixers.util.Either;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.base.math.ast.VariableNode;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TierBuilder {
    private int levelCount = 10;
    private @Nullable Either<Integer, String> baseLevelingCost = null;
    private @Nullable String levelingCostFunction;

    public TierBuilder() {
    }

    public TierBuilder levelCount(int levelCount) {
        if (levelCount <= 0) {
            throw new IllegalArgumentException("Level count must be greater than 0");
        }
        this.levelCount = levelCount;
        return this;
    }

    public TierBuilder clearBaseLevelingCost() {
        this.baseLevelingCost = null;
        return this;
    }

    public TierBuilder baseLevelingCost(int baseLevelingCost) {
        if (baseLevelingCost <= 0) {
            throw new IllegalArgumentException("Base leveling cost must be greater than 0");
        }
        this.baseLevelingCost = Either.left(baseLevelingCost);
        return this;
    }

    public TierBuilder factoredBaseLevelingCost(double factor) {
        return customBaseLevelingCost("prev * " + factor);
    }

    public TierBuilder customBaseLevelingCost(@NotNull String baseLevelingCostFunction) {
        this.baseLevelingCost = Either.right(baseLevelingCostFunction);
        return this;
    }

    public TierBuilder additiveLevelingCost(int incrementPerLevel) {
        return customLevelingCost("prev + " + incrementPerLevel);
    }

    public TierBuilder multiplicativeLevelingCost(double multiplierPerLevel) {
        return customLevelingCost("prev * " + multiplierPerLevel);
    }

    public TierBuilder levelInterpolatedMultiplicativeCost(double multiplier) {
        return customLevelingCost("prev * (%f ^ (1 / levels))".formatted(multiplier));
    }

    public TierBuilder multOffsetBoostCost(int multiplier, int offset, int boost) {
        return customLevelingCost("(%d * level + %d) * %d + base".formatted(multiplier, offset, boost));
    }

    public TierBuilder customLevelingCost(@NotNull String levelingCostFunction) {
        this.levelingCostFunction = levelingCostFunction;
        return this;
    }

    public TierPackData build() {
        if (levelingCostFunction == null) {
            throw new IllegalArgumentException("Leveling cost function must be set");
        }

        Either<Integer, Node> baseLevelingCost = this.baseLevelingCost == null ? null : this.baseLevelingCost.mapRight(fn -> {
            Node parsed;
            try {
                parsed = Parser.parse(fn);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Failed to parse base leveling cost function: " + fn, e);
            }

            // Check
            Set<String> variables = new HashSet<>();
            parsed.visitSelfAndChildren(node -> {
                if (node instanceof VariableNode variableNode) {
                    variables.add(variableNode.name());
                }
            });
            variables.remove("prev");
            if (!variables.isEmpty()) {
                throw new IllegalArgumentException("Base leveling cost function contains unsupported variables: " + variables);
            }

            try {
                parsed.evaluate(new EvaluationContext()
                    .setFinal("prev", 1)
                );
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Failed to evaluate base leveling cost function: " + fn, e);
            }

            return parsed;
        });

        int testBaseLevelingCost = baseLevelingCost == null ? 1 : baseLevelingCost.map(i -> i, n -> {
            EvaluationContext ctx = new EvaluationContext()
                .setFinal("prev", 1);
            try {
                return (int) Math.max(1, Math.round(n.evaluate(ctx)));
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Failed to evaluate base leveling cost function: " + n.repr(), e);
            }
        });

        Node parsedFunction;
        try {
            parsedFunction = Parser.parse(levelingCostFunction);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse leveling cost function: " + levelingCostFunction, e);
        }

        // Check
        Set<String> variables = new HashSet<>();
        parsedFunction.visitSelfAndChildren(node -> {
            if (node instanceof VariableNode variableNode) {
                variables.add(variableNode.name());
            }
        });
        variables.removeAll(Set.of("levels", "base", "prev", "level"));
        if (!variables.isEmpty()) {
            throw new IllegalArgumentException("Leveling cost function contains unsupported variables: " + variables);
        }

        try {
            parsedFunction.evaluate(new EvaluationContext()
                .setFinal("levels", levelCount)
                .setFinal("base", testBaseLevelingCost)
                .set("prev", testBaseLevelingCost)
                .set("level", 1)
            );
        } catch (EvaluationException e) {
            throw new IllegalArgumentException("Failed to evaluate leveling cost function: " + levelingCostFunction, e);
        }

        return new TierPackData(levelCount, Optional.ofNullable(baseLevelingCost), parsedFunction);
    }
}
