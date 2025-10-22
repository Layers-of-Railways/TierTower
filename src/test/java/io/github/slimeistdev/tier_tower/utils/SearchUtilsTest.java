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

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SearchUtilsTest {
    private void testLE(int expected, int[] array, int maxValue) {
        assertTimeoutPreemptively(Duration.ofMillis(100), () -> {
            assertEquals(expected, SearchUtils.binarySearchLE(array, maxValue));
        }, "Binary search should be fast");
    }

    @Test
    void binarySearchLE() {
        final int[] arr = new int[] {0, 2, 4, 6, 8, 10};

        assertAll(
            () -> testLE(0, arr, 0),
            () -> testLE(1, arr, 2),
            () -> testLE(1, arr, 3),
            () -> testLE(2, arr, 4),
            () -> testLE(5, arr, 10),
            () -> testLE(5, arr, 20),
            () -> testLE(-1, arr, -1)
        );
    }
}