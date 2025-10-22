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

import io.github.slimeistdev.tier_tower.utils.SearchUtils;
import net.minecraft.resources.ResourceLocation;

public final class Tier {
    private final ResourceLocation id;
    private final int levelCount;
    private final int[] levelingCosts;
    /** How many points, within this tier, are needed to get to a level */
    private final int[] levelBaseCosts;
    private final int totalCost;

    public Tier(ResourceLocation id, int[] levelingCosts) {
        this.id = id;
        this.levelCount = levelingCosts.length;
        this.levelingCosts = levelingCosts;

        this.levelBaseCosts = new int[levelCount];

        for (int i = 1; i < levelCount; i++) {
            levelBaseCosts[i] = levelBaseCosts[i - 1] + levelingCosts[i - 1];
        }
        this.totalCost = levelBaseCosts[levelCount - 1] + levelingCosts[levelCount - 1];
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getLevelCount() {
        return levelCount;
    }

    public int getLevelingCost(int level) {
        if (level < 0 || level >= levelCount) {
            throw new IndexOutOfBoundsException("Level must be between 0 and " + (levelCount - 1));
        }
        return levelingCosts[level];
    }

    public int getCostUpTo(int level) {
        if (level < 0 || level >= levelCount) {
            throw new IndexOutOfBoundsException("Level must be between 0 and " + (levelCount - 1));
        }

        return levelBaseCosts[level];
    }

    public int getLevel(int pointsWithinTier) {
        if (pointsWithinTier < 0 || pointsWithinTier >= totalCost) {
            throw new IndexOutOfBoundsException("Points within tier must be between 0 and " + (totalCost - 1));
        }

        // we know that this will never return -1, because lbc[0] == 0, and pwt >= 0
        return SearchUtils.binarySearchLE(levelBaseCosts, pointsWithinTier);
    }

    public int getTotalLevelingCost() {
        return totalCost;
    }
}
