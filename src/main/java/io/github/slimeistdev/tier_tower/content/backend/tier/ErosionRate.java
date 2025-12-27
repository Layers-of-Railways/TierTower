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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/** The rate at which a decaying tier loses points.
 *
 * @param loss the number of *points* lost per interval
 * @param interval the number of ticks between each loss
 */
public record ErosionRate(int loss, int interval) {
    public static final Codec<ErosionRate> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("loss").forGetter(ErosionRate::loss),
        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("interval").forGetter(ErosionRate::interval)
    ).apply(i, ErosionRate::new));

    public ErosionRate {
        if (loss <= 0) {
            throw new IllegalArgumentException("ErosionRate loss must be positive, got " + loss);
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("ErosionRate interval must be positive, got " + interval);
        }
    }

    @Override
    public @NotNull String toString() {
        return "ErosionRate[" + loss + " points per " + interval + " ticks]";
    }
}
