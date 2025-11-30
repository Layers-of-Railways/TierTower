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

import com.google.common.math.LongMath;
import io.github.slimeistdev.tier_tower.TierTower;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class FluidFormatter {
    public static final Format NUMBER_FORMAT = new Format();

    public static class Format extends SimplePreparableReloadListener<Unit> implements IdentifiableResourceReloadListener {
        public static final ResourceLocation ID = TierTower.asResource("format_reload_listener");
        private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

        private Format() {}

        public NumberFormat get() {
            return format;
        }

        @Override
        protected @NotNull Unit prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
            return Unit.INSTANCE;
        }

        @Override
        protected void apply(@NotNull Unit object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
            Locale locale = Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected());
            format = NumberFormat.getNumberInstance(locale);
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(0);
            format.setGroupingUsed(true);
        }

        @Override
        public ResourceLocation getFabricId() {
            return ID;
        }
    }

    static String format(double d) {
        return NUMBER_FORMAT.get()
            .format(d).replace("\u00A0", " ");
    }

    /**
     * Return a unicode string representing a fraction, like ¹⁄₈₁.
     */
    public static String getUnicodeFraction(long numerator, long denominator, boolean simplify) {
        if (numerator < 0 || denominator < 0)
            throw new IllegalArgumentException("Numerator and denominator must be non negative.");

        if (simplify && denominator != 0) {
            long g = LongMath.gcd(numerator, denominator);
            numerator /= g;
            denominator /= g;
        }

        StringBuilder numString = new StringBuilder();

        while (numerator > 0) {
            numString.append(SUPERSCRIPT[(int) (numerator % 10)]);
            numerator /= 10;
        }

        StringBuilder denomString = new StringBuilder();

        while (denominator > 0) {
            denomString.append(SUBSCRIPT[(int) (denominator % 10)]);
            denominator /= 10;
        }

        return numString.reverse().toString() + FRACTION_BAR + denomString.reverse();
    }

    /**
     * Convert a non negative fluid amount in droplets to a unicode string
     * representing the amount in millibuckets. For example, passing 163 will result
     * in
     *
     * <pre>
     * 2 ¹⁄₈₁
     * </pre>
     *
     * .
     */
    public static String getUnicodeMillibuckets(long droplets, boolean simplify) {
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        String result = format(droplets / 81);

        if (droplets % 81 != 0 && !simplify) {
            result += " " + getUnicodeFraction(droplets % 81, 81, true);
        }

        return result;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Component formatFluid(long droplets) {
        if (droplets < 0) {
            throw new IllegalArgumentException("Fluid amount must be non negative.");
        }

        long buckets = droplets / FluidConstants.BUCKET;
        long fraction = droplets % FluidConstants.BUCKET;

        if (buckets > 0 && fraction > 0) {
            return Component.translatable(
                "unit.tier_tower.fluid.buckets.millibuckets",
                format(buckets),
                getUnicodeMillibuckets(fraction, false)
            );
        } else if (fraction > 0) {
            return Component.translatable(
                "unit.tier_tower.fluid.millibuckets",
                getUnicodeMillibuckets(fraction, false)
            );
        } else {
            return Component.translatable(
                "unit.tier_tower.fluid.buckets",
                format(buckets)
            );
        }
    }

    private static final char[] SUPERSCRIPT = new char[] { '\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078',
        '\u2079' };
    private static final char FRACTION_BAR = '\u2044';
    private static final char[] SUBSCRIPT = new char[] { '\u2080', '\u2081', '\u2082', '\u2083', '\u2084', '\u2085', '\u2086', '\u2087', '\u2088',
        '\u2089' };
}
