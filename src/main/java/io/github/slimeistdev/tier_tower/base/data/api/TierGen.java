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

package io.github.slimeistdev.tier_tower.base.data.api;

import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.base.math.ast.VariableNode;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class TierGen {
    private TierGen() {}

    @FunctionalInterface
    public interface DynamicHolderProvider {
        @NotNull <T> Holder<T> get(ResourceKey<T> key);
    }

    public record BootstapLookup<T>(BootstapContext<T> context) implements DynamicHolderProvider {
        @Override
        public @NotNull <R> Holder<R> get(ResourceKey<R> key) {
            ResourceKey<Registry<R>> registryKey = ResourceKey.createRegistryKey(key.registry());
            return context.lookup(registryKey).getOrThrow(key);
        }
    }

    public record HolderLookupWrapper(HolderLookup.Provider provider) implements DynamicHolderProvider {
        @Override
        public @NotNull <T> Holder<T> get(ResourceKey<T> key) {
            ResourceKey<Registry<T>> registryKey = ResourceKey.createRegistryKey(key.registry());
            return provider.lookupOrThrow(registryKey).getOrThrow(key);
        }
    }

    public static class GenEntry<T> implements NonNullFunction<DynamicHolderProvider, T> {
        private final ResourceKey<T> key;
        private NonNullFunction<DynamicHolderProvider, T> factory;
        private T value;

        public GenEntry(ResourceKey<T> key, NonNullFunction<DynamicHolderProvider, T> factory) {
            this.key = key;
            this.factory = factory;
        }

        public GenEntry(ResourceKey<T> key, NonNullSupplier<T> supplier) {
            this(key, $ -> supplier.get());
        }

        public GenEntry(ResourceKey<T> key, T value) {
            this.key = key;
            this.value = value;
        }

        public ResourceKey<T> getKey() {
            return key;
        }

        @Override
        public @NotNull T apply(DynamicHolderProvider provider) {
            if (factory != null) {
                value = factory.apply(provider);
                factory = null;
            }
            return value;
        }

        public @Nullable T get() {
            return value;
        }
    }

    public static class TierBuilder {
        private int levelCount = 10;
        private @Nullable Integer baseLevelingCost = null;
        private @Nullable String levelingCostFunction;

        public TierBuilder() {}

        public TierBuilder levelCount(int levelCount) {
            this.levelCount = levelCount;
            return this;
        }

        public TierBuilder clearBaseLevelingCost() {
            this.baseLevelingCost = null;
            return this;
        }

        public TierBuilder baseLevelingCost(int baseLevelingCost) {
            this.baseLevelingCost = baseLevelingCost;
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
            if (levelCount <= 0) {
                throw new IllegalArgumentException("Level count must be greater than 0");
            }
            if (baseLevelingCost != null && baseLevelingCost <= 0) {
                throw new IllegalArgumentException("Base leveling cost must be greater than 0");
            }
            if (levelingCostFunction == null) {
                throw new IllegalArgumentException("Leveling cost function must be set");
            }

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
                    .setFinal("base", baseLevelingCost == null ? 1 : baseLevelingCost)
                    .set("prev", baseLevelingCost == null ? 1 : baseLevelingCost)
                    .set("level", 1)
                );
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Failed to evaluate leveling cost function: " + levelingCostFunction, e);
            }

            return new TierPackData(levelCount, Optional.ofNullable(baseLevelingCost), parsedFunction);
        }
    }

    public static class SequenceBuilder {
        private final List<ResourceKey<TierPackData>> tiers = new ArrayList<>();
        private @Nullable Integer defaultBaseLevelingCost = null;
        private @Nullable ResourceKey<Sequence> nextSequence = null;

        public SequenceBuilder() {}

        public SequenceBuilder tier(@NotNull GenEntry<TierPackData> tier) {
            return tier(tier.getKey());
        }

        public SequenceBuilder tier(@NotNull ResourceKey<TierPackData> tier) {
            if (tiers.contains(tier)) {
                throw new IllegalArgumentException("Tier already exists in the sequence: " + tier);
            }
            tiers.add(tier);
            return this;
        }

        public SequenceBuilder defaultBaseLevelingCost(int baseLevelingCost) {
            if (baseLevelingCost <= 0) {
                throw new IllegalArgumentException("Base leveling cost must be greater than 0");
            }
            this.defaultBaseLevelingCost = baseLevelingCost;
            return this;
        }

        public SequenceBuilder nextSequence(@NotNull GenEntry<Sequence> nextSequence) {
            return nextSequence(nextSequence.getKey());
        }

        public SequenceBuilder nextSequence(ResourceKey<Sequence> nextSequence) {
            this.nextSequence = nextSequence;
            return this;
        }

        public Sequence build(DynamicHolderProvider lookup) {
            if (tiers.isEmpty()) {
                throw new IllegalArgumentException("Sequence must contain at least one tier");
            }
            if (defaultBaseLevelingCost == null || defaultBaseLevelingCost <= 0) {
                throw new IllegalArgumentException("Default base leveling cost must be defined and greater than 0");
            }

            return new Sequence(
                tiers.stream().map(lookup::get).toList(),
                defaultBaseLevelingCost,
                nextSequence == null ? null : lookup.get(nextSequence)
            );
        }
    }
}
