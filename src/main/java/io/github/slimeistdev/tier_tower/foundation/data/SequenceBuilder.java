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

import io.github.slimeistdev.tier_tower.base.math.MathPreconditions;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SequenceBuilder {
    private final List<ResourceKey<TierPackData>> tiers = new ArrayList<>();
    private @Nullable Integer defaultBaseLevelingCost = null;
    private @Nullable String prestigeValueFunction = null;
    private @Nullable String prestigeMultiplierFunction = null;
    private @Nullable ResourceKey<Sequence> nextSequence = null;

    public SequenceBuilder() {
    }

    public SequenceBuilder tier(@NotNull SimpleGenEntry<TierPackData> tier) {
        return tier(tier.getKey());
    }

    public SequenceBuilder tier(@NotNull ResourceKey<TierPackData> tier) {
        if (tiers.contains(tier)) {
            throw new IllegalArgumentException("Tier already exists in the sequence: " + tier);
        }
        tiers.add(tier);
        return this;
    }

    @SafeVarargs
    public final SequenceBuilder tiers(@NotNull SimpleGenEntry<TierPackData>... tiers) {
        for (var tier : tiers) {
            tier(tier);
        }
        return this;
    }

    @SafeVarargs
    public final SequenceBuilder tiers(@NotNull ResourceKey<TierPackData>... tiers) {
        for (var tier : tiers) {
            tier(tier);
        }
        return this;
    }

    public SequenceBuilder defaultBaseLevelingCost(int baseLevelingCost) {
        if (baseLevelingCost <= 0) {
            throw new IllegalArgumentException("Base leveling cost must be greater than 0");
        }
        this.defaultBaseLevelingCost = baseLevelingCost;
        return this;
    }

    public SequenceBuilder penaltyPrestigeValueFunction(int penaltyPoints) {
        return customPrestigeValueFunction("points - " + penaltyPoints);
    }

    public SequenceBuilder customPrestigeValueFunction(@NotNull String prestigeValueFunction) {
        this.prestigeValueFunction = prestigeValueFunction;
        return this;
    }

    public SequenceBuilder dividingPrestigeMultiplierFunction(int divisor) {
        return customPrestigeMultiplierFunction("1 + (prestige_points / " + divisor + ")");
    }

    public SequenceBuilder capPrestigeMultiplierFunction(int cap) {
        if (cap <= 1) {
            throw new IllegalArgumentException("Cap must be greater than 1");
        }
        if (prestigeMultiplierFunction == null) {
            throw new IllegalStateException("Prestige multiplier function must be defined before applying a cap");
        }
        return customPrestigeMultiplierFunction("min(" + cap + ", " + prestigeMultiplierFunction + ")");
    }

    public SequenceBuilder customPrestigeMultiplierFunction(@NotNull String prestigeMultiplierFunction) {
        this.prestigeMultiplierFunction = prestigeMultiplierFunction;
        return this;
    }

    public SequenceBuilder nextSequence(@NotNull SimpleGenEntry<Sequence> nextSequence) {
        return nextSequence(nextSequence.getKey());
    }

    public SequenceBuilder nextSequence(ResourceKey<Sequence> nextSequence) {
        this.nextSequence = nextSequence;
        return this;
    }

    public Sequence build() {
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("Sequence must contain at least one tier");
        }
        if (defaultBaseLevelingCost == null || defaultBaseLevelingCost <= 0) {
            throw new IllegalArgumentException("Default base leveling cost must be defined and greater than 0");
        }

        if (prestigeValueFunction == null) {
            throw new IllegalArgumentException("Prestige value function must be defined");
        }

        Node parsedValueFn = MathPreconditions.checkAndEvaluateFunction("prestige value function", prestigeValueFunction,
            ctx -> ctx
                .set("points", 10000)
                .set("tier", 1)
                .set("level", 1)
                .set("level_points", 50));

        Node parsedMultiplierFn = MathPreconditions.checkAndEvaluateFunction("prestige multiplier function", prestigeMultiplierFunction,
            ctx -> ctx
                .set("prestige_points", 5000));

        return new Sequence(
            tiers,
            defaultBaseLevelingCost,
            parsedValueFn,
            parsedMultiplierFn,
            nextSequence
        );
    }
}
