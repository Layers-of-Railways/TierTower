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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import io.github.slimeistdev.tier_tower.utils.SearchUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Sequence {
    public static final Codec<Sequence> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.list(TierTowerRegistries.TIER_CODEC).fieldOf("tiers")
            .forGetter(Sequence::getTierKeys),
        Codec.INT.fieldOf("default_base_leveling_cost")
            .forGetter(s -> s.defaultBaseLevelingCost),
        TierTowerRegistries.SEQUENCE_CODEC.optionalFieldOf("next_sequence")
            .forGetter(s -> Optional.ofNullable(s.getNextSequenceKey()))
    ).apply(i, (t, c, s) -> new Sequence(t, c, s.orElse(null))));

    private @Nullable List<ResourceKey<TierPackData>> unfrozenTiers;
    private @Nullable ResourceKey<Sequence> unfrozenNextSequence;

    private final Tier[] tiers;
    private final int defaultBaseLevelingCost;
    private @Nullable Holder<Sequence> nextSequence;
    // derived values
    /** How many points are needed to get to level 1 (index 0) of a tier */
    private final int[] tierBaseCosts;
    private final Object2IntMap<ResourceLocation> idMap;

    public Sequence(@NotNull List<ResourceKey<TierPackData>> tiers, int defaultBaseLevelingCost, @Nullable ResourceKey<Sequence> nextSequence) {
        this.unfrozenTiers = List.copyOf(tiers);
        this.unfrozenNextSequence = nextSequence;

        this.defaultBaseLevelingCost = defaultBaseLevelingCost;

        this.tiers = new Tier[tiers.size()];
        this.idMap = new Object2IntOpenHashMap<>();
        this.tierBaseCosts = new int[tiers.size()];
    }

    private static void checkForDuplicates(List<ResourceKey<TierPackData>> tiers) {
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("A sequence must contain at least one tier");
        }

        Set<ResourceKey<TierPackData>> seen = new HashSet<>();
        for (ResourceKey<TierPackData> tier : tiers) {
            if (!seen.add(tier)) {
                throw new IllegalArgumentException("Duplicate tier pack data ID: " + tier);
            }
        }
    }

    @ApiStatus.Internal
    public void freeze(HolderLookup.Provider lookupProvider) {
        if (unfrozenTiers == null) return;
        checkForDuplicates(unfrozenTiers);

        RegistryLookup<Sequence> sequenceLookup = lookupProvider.lookupOrThrow(TierTowerRegistries.SEQUENCE);
        RegistryLookup<TierPackData> tierLookup = lookupProvider.lookupOrThrow(TierTowerRegistries.TIER);

        nextSequence = unfrozenNextSequence == null ? null : sequenceLookup.getOrThrow(unfrozenNextSequence);

        // construct tiers
        int nextBaseCost = defaultBaseLevelingCost;
        for (int i = 0; i < tiers.length; i++) {
            Holder<TierPackData> holder = tierLookup.getOrThrow(unfrozenTiers.get(i));
            Pair<Tier, Integer> constructed = Tier.constructFrom(holder, nextBaseCost);
            Tier tier = constructed.getFirst();
            nextBaseCost = constructed.getSecond();

            tiers[i] = tier;
            idMap.put(tier.getId(), i);
        }

        // compute base costs
        for (int i = 1; i < tiers.length; i++) {
            int prevCost = tierBaseCosts[i - 1];
            Tier prevTier = tiers[i - 1];

            tierBaseCosts[i] = prevCost + prevTier.getTotalLevelingCost();
        }

        unfrozenNextSequence = null;
        unfrozenTiers = null;
    }

    private void ensureFrozen() {
        if (unfrozenTiers != null) {
            throw new IllegalStateException("Sequence must be frozen before use");
        }
    }

    private List<ResourceKey<TierPackData>> getTierKeys() {
        return unfrozenTiers != null ? unfrozenTiers : Arrays.stream(tiers).map(Tier::getKey).toList();
    }

    public LevelingState getLevelingState(final int totalPoints) {
        ensureFrozen();
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
        ensureFrozen();
        if (tierIndex < 0 || tierIndex >= tiers.length) {
            throw new IndexOutOfBoundsException("Tier index must be between 0 and " + (tiers.length - 1));
        }
        return tierBaseCosts[tierIndex];
    }

    public Tier getTier(int index) {
        ensureFrozen();
        if (index < 0 || index >= tiers.length) {
            throw new IndexOutOfBoundsException("Index must be between 0 and " + (tiers.length - 1));
        }
        return tiers[index];
    }

    public @Nullable Tier getTier(ResourceLocation id) {
        ensureFrozen();
        int index = idMap.getOrDefault(id, -1);
        return index >= 0 ? tiers[index] : null;
    }

    public int indexOf(ResourceLocation tierId) {
        ensureFrozen();
        return idMap.getOrDefault(tierId, -1);
    }

    public @Nullable Tier getNextTier(Tier currentTier) {
        ensureFrozen();
        int index = idMap.getOrDefault(currentTier.getId(), -1);
        if (index < 0 || index + 1 >= tiers.length) {
            return null; // No next tier available
        }
        return tiers[index + 1];
    }

    public int getTierCount() {
        return tiers.length;
    }

    public @Nullable Holder<Sequence> getNextSequence() {
        ensureFrozen();
        return nextSequence;
    }

    public @Nullable ResourceKey<Sequence> getNextSequenceKey() {
        ensureFrozen();
        return nextSequence != null ? nextSequence.unwrapKey().orElse(null) : null;
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
