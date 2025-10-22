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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Sequence {
    private final ResourceLocation id;
    private final Tier[] tiers;
    /** How many points are needed to get to level 1 (index 0) of a tier */
    private final int[] tierBaseCosts;
    private final Object2IntMap<ResourceLocation> idMap;
    private final @Nullable ResourceLocation nextSequence;

    public Sequence(ResourceLocation id, Tier[] tiers, @Nullable ResourceLocation nextSequence) {
        this.id = id;
        this.tiers = tiers;
        this.idMap = new Object2IntOpenHashMap<>();
        this.nextSequence = nextSequence;

        for (int i = 0; i < this.tiers.length; i++) {
            Tier tier = this.tiers[i];

            if (idMap.containsKey(tier.getId())) {
                throw new IllegalArgumentException("Duplicate tier ID: " + tier.getId());
            }
            idMap.put(tier.getId(), i);
        }

        this.tierBaseCosts = new int[tiers.length];
        for (int i = 1; i < tiers.length; i++) {
            int prevCost = tierBaseCosts[i - 1];
            Tier currentTier = tiers[i];

            tierBaseCosts[i] = prevCost + currentTier.getTotalLevelingCost();
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public LevelingState getLevelingState(final int totalPoints) {
        int tierIdx = SearchUtils.binarySearchLE(tierBaseCosts, totalPoints);
        assert tierIdx >= 0: "tierBaseCosts[0] should be 0, so a tier should be findable";

        Tier tier = tiers[tierIdx];
        int pointsWithinTier = totalPoints - tierBaseCosts[tierIdx];
        int levelIdx = tier.getLevel(pointsWithinTier);

        int levelPoints0 = pointsWithinTier - tier.getCostUpTo(levelIdx);

        int levelPoints = Math.min(tier.getLevelingCost(levelIdx), levelPoints0);
        int surplusPoints = levelPoints0 - levelPoints;

        return new LevelingState(tierIdx, levelIdx, levelPoints, surplusPoints);
    }

    public int getCostUpTo(int tierIndex) {
        if (tierIndex < 0 || tierIndex >= tiers.length) {
            throw new IndexOutOfBoundsException("Tier index must be between 0 and " + (tiers.length - 1));
        }
        return tierBaseCosts[tierIndex];
    }

    public Tier getTier(int index) {
        if (index < 0 || index >= tiers.length) {
            throw new IndexOutOfBoundsException("Index must be between 0 and " + (tiers.length - 1));
        }
        return tiers[index];
    }

    public @Nullable Tier getTier(ResourceLocation id) {
        int index = idMap.getOrDefault(id, -1);
        return index >= 0 ? tiers[index] : null;
    }

    public @Nullable Tier getNextTier(Tier currentTier) {
        int index = idMap.getOrDefault(currentTier.getId(), -1);
        if (index < 0 || index + 1 >= tiers.length) {
            return null; // No next tier available
        }
        return tiers[index + 1];
    }

    public int getTierCount() {
        return tiers.length;
    }

    public @Nullable ResourceLocation getNextSequence() {
        return nextSequence;
    }

    public record LevelingState(int tierIndex, int levelIndex, int levelPoints, int surplusPoints) {
        public static final LevelingState ZERO = new LevelingState(0, 0, 0, 0);

        public static LevelingState read(FriendlyByteBuf buf) {
            return new LevelingState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(tierIndex);
            buf.writeVarInt(levelIndex);
            buf.writeVarInt(levelPoints);
            buf.writeVarInt(surplusPoints);
        }
    }
}
