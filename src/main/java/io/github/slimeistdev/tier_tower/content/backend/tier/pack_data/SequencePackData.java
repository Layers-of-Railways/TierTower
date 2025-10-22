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

package io.github.slimeistdev.tier_tower.content.backend.tier.pack_data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record SequencePackData(List<ResourceLocation> tiers, int defaultBaseLevelingCost, Optional<ResourceLocation> nextSequence) {
    public static final Codec<SequencePackData> CODEC = RecordCodecBuilder.create(i -> i.group(
        ResourceLocation.CODEC.listOf().fieldOf("tiers").forGetter(SequencePackData::tiers),
        Codec.INT.fieldOf("default_base_leveling_cost").forGetter(SequencePackData::defaultBaseLevelingCost),
        ResourceLocation.CODEC.optionalFieldOf("next_sequence").forGetter(SequencePackData::nextSequence)
    ).apply(i, SequencePackData::new));
}
