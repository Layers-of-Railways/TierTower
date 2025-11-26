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

package io.github.slimeistdev.tier_tower.utils;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

@SuppressWarnings("UnstableApiUsage")
public class EminenceConstants {
    public static final int BOTTLES_PER_BUCKET = Math.toIntExact(FluidConstants.BUCKET / FluidConstants.BOTTLE);

    public static final int EMINENCE_PER_BUCKET = 600;
    public static final int EMINENCE_PER_BOTTLE = assertFractionExact(EMINENCE_PER_BUCKET, BOTTLES_PER_BUCKET);
    public static final int FLUID_PER_EMINENCE = assertFractionExact(FluidConstants.BUCKET, EMINENCE_PER_BUCKET);

    @SuppressWarnings("SameParameterValue")
    private static int assertFractionExact(int numerator, int denominator) {
        if (numerator % denominator != 0) {
            throw new IllegalArgumentException("Eminence fraction " + numerator + "/" + denominator + " is not exact!");
        }
        return numerator / denominator;
    }

    @SuppressWarnings("SameParameterValue")
    private static int assertFractionExact(long numerator, long denominator) {
        if (numerator % denominator != 0) {
            throw new IllegalArgumentException("Eminence fraction " + numerator + "/" + denominator + " is not exact!");
        }
        return Math.toIntExact(numerator / denominator);
    }
}
