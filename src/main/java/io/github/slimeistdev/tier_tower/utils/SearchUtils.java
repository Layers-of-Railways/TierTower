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

public class SearchUtils {
    /**
     * Find the largest integer <= maxValue in a sorted array
     * @param values the sorted array of integers to search
     * @param maxValue the maximum value to compare against
     * @return the largest index where values[index] <= maxValue, or -1 if no such index exists
     */
    public static int binarySearchLE(final int[] values, final int maxValue) {
        if (values.length == 0 || values[0] > maxValue) return -1;

        int left = 0;
        int right = values.length - 1;

        while (left < right) {
            int mid = right - (right - left) / 2; // bias up
            int v = values[mid];

            if (v > maxValue) {
                right = mid - 1;
            } else {
                left = mid;
            }
        }

        return right;
    }
}
