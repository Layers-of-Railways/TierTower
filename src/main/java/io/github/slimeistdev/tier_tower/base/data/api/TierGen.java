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

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.base.math.ast.VariableNode;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;
import io.github.slimeistdev.tier_tower.content.backend.tier.pack_data.SequencePackData;
import io.github.slimeistdev.tier_tower.content.backend.tier.pack_data.TierPackData;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class TierGen implements DataProvider {
    protected final PackOutput.PathProvider tierPathProvider;
    protected final PackOutput.PathProvider sequencePathProvider;

    public TierGen(PackOutput output) {
        this.tierPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tier_tower/tier");
        this.sequencePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tier_tower/sequence");
    }

    protected abstract void registerTiers(Consumer<GenEntry<TierPackData>> provider);

    protected abstract void registerSequences(Consumer<GenEntry<SequencePackData>> provider);

    private static <T> void registerCodec(
        String name,
        Consumer<Consumer<GenEntry<T>>> registrar,
        Codec<T> codec,
        CachedOutput output,
        PackOutput.PathProvider pathProvider,
        List<CompletableFuture<?>> futures
    ) {
        Set<ResourceLocation> ids = new HashSet<>();
        registrar.accept(entry -> {
            if (!ids.add(entry.getId())) {
                throw new IllegalStateException("Duplicate " + name + " ID: " + entry.getId());
            }

            futures.add(DataProvider.saveStable(
                output,
                codec.encodeStart(JsonOps.INSTANCE, entry.get())
                    .resultOrPartial(TierTower.LOGGER::error)
                    .orElseThrow(),
                pathProvider.json(entry.getId())
            ));
        });
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        registerCodec("tier", this::registerTiers, TierPackData.CODEC, output, tierPathProvider, futures);
        registerCodec("sequence", this::registerSequences, SequencePackData.CODEC, output, sequencePathProvider, futures);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public abstract @NotNull String getName();

    public static class GenEntry<T> implements NonNullSupplier<T> {
        private final ResourceLocation id;
        private NonNullSupplier<T> supplier;
        private T value;

        public GenEntry(ResourceLocation id, NonNullSupplier<T> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        public GenEntry(ResourceLocation id, T value) {
            this.id = id;
            this.value = value;
        }

        public ResourceLocation getId() {
            return id;
        }

        public T get() {
            if (supplier != null) {
                value = supplier.get();
                supplier = null;
            }
            return value;
        }
    }

    protected static class TierBuilder {
        private int levelCount = 100;
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
            variables.removeAll(Set.of("prev", "levels"));
            if (!variables.isEmpty()) {
                throw new IllegalArgumentException("Leveling cost function contains unsupported variables: " + variables);
            }

            try {
                parsedFunction.evaluate(new EvaluationContext()
                    .set("prev", baseLevelingCost == null ? 1 : baseLevelingCost)
                    .set("levels", levelCount));
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Failed to evaluate leveling cost function: " + levelingCostFunction, e);
            }

            return new TierPackData(levelCount, Optional.ofNullable(baseLevelingCost), parsedFunction);
        }
    }

    protected static class SequenceBuilder {
        private final List<ResourceLocation> tiers = new ArrayList<>();
        private @Nullable Integer defaultBaseLevelingCost = null;
        private @Nullable ResourceLocation nextSequence = null;

        public SequenceBuilder() {}

        public SequenceBuilder tier(@NotNull GenEntry<TierPackData> tier) {
            return tier(tier.getId());
        }

        public SequenceBuilder tier(@NotNull ResourceLocation tier) {
            if (tiers.contains(tier)) {
                throw new IllegalArgumentException("Tier ID already exists in the sequence: " + tier);
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

        public SequenceBuilder nextSequence(@NotNull GenEntry<SequencePackData> nextSequence) {
            return nextSequence(nextSequence.getId());
        }

        public SequenceBuilder nextSequence(ResourceLocation nextSequence) {
            this.nextSequence = nextSequence;
            return this;
        }

        public SequencePackData build() {
            if (tiers.isEmpty()) {
                throw new IllegalArgumentException("Sequence must contain at least one tier");
            }
            if (defaultBaseLevelingCost == null || defaultBaseLevelingCost <= 0) {
                throw new IllegalArgumentException("Default base leveling cost must be defined and greater than 0");
            }

            return new SequencePackData(
                List.copyOf(tiers),
                defaultBaseLevelingCost,
                Optional.ofNullable(nextSequence)
            );
        }
    }
}
