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

package io.github.slimeistdev.tier_tower.content.backend.tier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;

import java.util.Optional;

public record TierPackData(int levelCount, Optional<Either<Integer, Node>> baseLevelingCost, Node levelingCostFunction) {
    private static final Codec<Either<Integer, Node>> BASE_COST_CODEC = Codec.either(
        Codec.intRange(1, Integer.MAX_VALUE),
        Node.limitedCodec("prev")
    );
    private static final Codec<Node> COST_FUNCTION_CODEC = Node.limitedCodec("levels", "base", "prev", "level");

    public static final Codec<TierPackData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("level_count").forGetter(TierPackData::levelCount),
        BASE_COST_CODEC.optionalFieldOf("base_leveling_cost").forGetter(TierPackData::baseLevelingCost),
        COST_FUNCTION_CODEC.fieldOf("leveling_cost_function").forGetter(TierPackData::levelingCostFunction)
    ).apply(i, TierPackData::new));
}
