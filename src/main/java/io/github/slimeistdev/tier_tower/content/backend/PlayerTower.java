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

package io.github.slimeistdev.tier_tower.content.backend;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.events.ProgressionCallback;
import io.github.slimeistdev.tier_tower.base.network.PlayerSelection;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence.LevelingState;
import io.github.slimeistdev.tier_tower.content.backend.tier.Tier;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import io.github.slimeistdev.tier_tower.network.packets.s2c.TowerSummaryPacket;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Only available on the server */
public class PlayerTower {
    public static final Codec<PlayerTower> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(p -> p.playerId),
        Codec.unboundedMap(TierTowerRegistries.SEQUENCE_CODEC, SequenceState.CODEC).fieldOf("sequences").forGetter(p -> p.sequences),
        TierTowerRegistries.SEQUENCE_CODEC.fieldOf("current_sequence").forGetter(p -> p.currentSequence),
        Codec.BOOL.fieldOf("locked").forGetter(PlayerTower::isLocked)
    ).apply(i, PlayerTower::new));

    private final UUID playerId;
    private final Map<ResourceKey<Sequence>, SequenceState> sequences = new HashMap<>();
    private @NotNull ResourceKey<Sequence> currentSequence = TierTower.MAIN_SEQUENCE;
    private boolean locked = false;

    // cache variables
    private @Nullable LevelingState $levelingState = null;
    private double $prestigeMultiplier = Double.NaN;

    @Nullable RegistryAccess registryAccess;

    public PlayerTower(UUID playerId) {
        this.playerId = playerId;
    }

    // codec constructor
    private PlayerTower(UUID playerId, Map<ResourceKey<Sequence>, SequenceState> sequences,
                        @NotNull ResourceKey<Sequence> currentSequence, boolean locked) {
        this(playerId);
        this.sequences.putAll(sequences);
        this.currentSequence = currentSequence;
        this.locked = locked;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    private @NotNull LevelingState ensureCurrent() {
        SequenceState state = sequences.get(currentSequence);

        Sequence sequence;
        if (state != null && (sequence = state.getSequence(registryAccess)) != null) {
            if ($levelingState == null)
                $levelingState = sequence.getLevelingState(state.totalPoints);
            return $levelingState;
        }

        boolean needsAdd = false;

        if (state == null) {
            state = new SequenceState(currentSequence);
            $prestigeMultiplier = Double.NaN;
            needsAdd = true;
        }

        while ((sequence = state.getSequence(registryAccess)) == null) {
            if (currentSequence.equals(TierTower.MAIN_SEQUENCE)) {
                throw new IllegalStateException("The main sequence does not exist. This is a critical error");
            }

            sequences.remove(currentSequence);

            currentSequence = TierTower.MAIN_SEQUENCE;
            state = sequences.computeIfAbsent(currentSequence, SequenceState::new);
            $prestigeMultiplier = Double.NaN;
            needsAdd = true;
        }

        if (needsAdd) {
            sequences.put(currentSequence, state);
            markDirty();
        }

        $levelingState = sequence.getLevelingState(state.totalPoints);

        return $levelingState;
    }

    public boolean trySwitchToSequence(@NotNull ResourceKey<Sequence> id) {
        return trySwitchToSequence(id, false) != null;
    }

    @SuppressWarnings("SameParameterValue")
    private @Nullable Pair<@NotNull SequenceState, @NotNull Sequence> trySwitchToSequence(@NotNull ResourceKey<Sequence> id, boolean discardCurrent) {
        SequenceState state = sequences.get(id);

        if (state == null) {
            state = new SequenceState(id);
        }

        Sequence sequence = state.getSequence(registryAccess);
        if (sequence == null) {
            return null;
        }

        if (discardCurrent && !id.equals(currentSequence)) {
            sequences.remove(currentSequence);
        }
        currentSequence = id;
        sequences.put(currentSequence, state);
        $levelingState = null; // reset the cached leveling state
        $prestigeMultiplier = Double.NaN;
        markDirty();
        syncData();
        return Pair.of(state, sequence);
    }

    protected @NotNull SequenceState getSequenceState() {
        ensureCurrent();
        return sequences.get(currentSequence);
    }

    public @NotNull Sequence getSequence() {
        ensureCurrent();
        // this is safe because ensureCurrent() guarantees that the sequence exists
        return Objects.requireNonNull(sequences.get(currentSequence).getSequence(registryAccess));
    }

    public boolean doPrestige(@NotNull ServerPlayer player) {
        final LevelingState levelingState = ensureCurrent();
        final SequenceState sequenceState = getSequenceState();
        final Sequence sequence = getSequence();

        int prestigePoints = sequence.calculatePrestigePoints(sequenceState.totalPoints, levelingState);
        if (prestigePoints <= 0)
            return false;

        sequenceState.prestigePoints += prestigePoints;
        sequenceState.totalPoints = 0;
        $levelingState = sequence.getLevelingState(0);
        $prestigeMultiplier = Double.NaN;

        markDirty();
        syncData();

        ProgressionCallback.PRESTIGE.invoker().onLevelUp(player, this);

        return true;
    }

    public int getPrestigePoints() {
        return getSequenceState().prestigePoints;
    }

    public void setPrestigePoints(int points) {
        if (points < 0) {
            points = 0;
        }

        SequenceState sequenceState = getSequenceState();
        sequenceState.prestigePoints = points;
        $prestigeMultiplier = Double.NaN;

        markDirty();
        syncData();
    }

    public double getPrestigeMultiplier() {
        final SequenceState sequenceState = getSequenceState();
        final Sequence sequence = getSequence();

        if (Double.isNaN($prestigeMultiplier)) {
            $prestigeMultiplier = sequence.calculatePrestigeMultiplier(sequenceState.prestigePoints);
        }

        return $prestigeMultiplier;
    }

    public int applyPrestigeToPoints(int basePoints, @NotNull RandomSource random) {
        double maxMultiplier = getPrestigeMultiplier();
        double multiplier = 1.0 + (random.nextDouble() * (maxMultiplier - 1.0));
        return (int)Math.round(basePoints * multiplier);
    }

    public void addPoints(int points, @NotNull ServerPlayer player) {
        if (points <= 0) return;

        final var levelState = ensureCurrent();

        SequenceState sequenceState = getSequenceState();
        Sequence sequence = getSequence();
        int tierIndex = levelState.tierIndex();
        int levelIndex = levelState.levelIndex();
        int levelPoints = levelState.levelPoints();
        int surplusPoints = levelState.surplusPoints();

        boolean leveledUp = false;
        boolean tieredUp = false;

        // distribute points, first to the current level, then to the current tier
        while (points > 0) {
            Tier tier = sequence.getTier(tierIndex);
            int levelingCost = tier.getLevelingCost(levelIndex);

            if (levelPoints + points < levelingCost) {
                levelPoints += points;
                points = 0;
            } else { // level up!
                points -= (levelingCost - levelPoints);
                levelPoints = 0;
                levelIndex++;
                leveledUp = true;

                if (levelIndex >= tier.getLevelCount()) { // move up a tier
                    levelIndex = 0;
                    tierIndex++;
                    tieredUp = true;

                    if (tierIndex >= sequence.getTierCount()) { // move up to the next sequence
                        ResourceKey<Sequence> nextSequence = sequence.getNextSequenceKey();
                        Pair<SequenceState, Sequence> newSeq;
                        if (nextSequence == null || (newSeq = trySwitchToSequence(nextSequence, false)) == null) {
                            // restore maxed-out state
                            tierIndex--;
                            levelIndex = tier.getLevelCount() - 1;
                            levelPoints = levelingCost;
                            tieredUp = false;
                            break;
                        }
                        sequenceState = newSeq.getFirst();
                        sequence = newSeq.getSecond();
                        tierIndex = 0;
                        // try to mix surplus points back in
                        points += surplusPoints;
                        surplusPoints = 0;
                    }
                }
            }
        }
        surplusPoints += points;

        // just completely recalculate the total points,
        // trying to do it incrementally with switching sequences is too complicated
        // WARN: this breaks if we turn off `discardCurrent` in the trySwitchToSequence call
        sequenceState.totalPoints = sequence.getCostUpTo(tierIndex)
            + sequence.getTier(tierIndex).getCostUpTo(levelIndex)
            + levelPoints
            + surplusPoints;

        $levelingState = new LevelingState(
            tierIndex,
            levelIndex,
            levelPoints,
            surplusPoints
        );

        markDirty();
        syncData();

        if (player != null) {
            if (tieredUp) {
                ProgressionCallback.TIER.invoker().onLevelUp(player, this);
            } else if (leveledUp) {
                ProgressionCallback.LEVEL.invoker().onLevelUp(player, this);
            }
        }
    }

    public boolean removePoints(int points) {
        if (points <= 0) return false;

        SequenceState sequenceState = getSequenceState();
        Sequence sequence = getSequence();

        int totalPoints = sequenceState.totalPoints - points;
        if (totalPoints < 0) return false;

        sequenceState.totalPoints = totalPoints;
        $levelingState = sequence.getLevelingState(totalPoints);

        markDirty();
        syncData();

        return true;
    }

    /**
     * Sets state of current sequence
     *
     * @param tierIndex index of tier
     * @param level     level within tier
     * @param points    points within level
     * @return the new summary after setting the state, or null if any parameters exceed the maxima
     */
    public @Nullable TowerSummary setTierLevelAndPoints(int tierIndex, int level, int points) {
        if (tierIndex < 0 || level < 0 || points < 0) {
            return null;
        }

        SequenceState sequenceState = getSequenceState();
        Sequence sequence = getSequence();

        if (tierIndex >= sequence.getTierCount()) {
            return null;
        }
        Tier tier = sequence.getTier(tierIndex);

        if (level >= tier.getLevelCount()) {
            return null;
        }
        if (points >= tier.getLevelingCost(level)) {
            return null;
        }

        sequenceState.totalPoints = sequence.getCostUpTo(tierIndex) + tier.getCostUpTo(level) + points;

        $levelingState = new LevelingState(
            tierIndex,
            level,
            points,
            0
        );

        markDirty();
        syncData();

        return new TowerSummary(currentSequence, $levelingState, new TowerSummary.Prestige(sequenceState.prestigePoints, getPrestigeMultiplier()));
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        markDirty(); // no syncData because clients aren't informed of lock state
    }

    public void markDirty() {
        TierTower.CITY.markCityDirty();
    }

    public void syncData() {
        TierTowerPackets.PACKETS.sendTo(PlayerSelection.all(), new TowerSummaryPacket(this));
    }

    public @NotNull TowerSummary summarize() {
        LevelingState levelingState = ensureCurrent();
        return new TowerSummary(currentSequence, levelingState, new TowerSummary.Prestige(getSequenceState().prestigePoints, getPrestigeMultiplier()));
    }

    public int totalPoints() {
        return getSequenceState().totalPoints;
    }

    protected static class SequenceState {
        public static final Codec<SequenceState> CODEC = RecordCodecBuilder.create(i -> i.group(
            TierTowerRegistries.SEQUENCE_CODEC.fieldOf("sequence_id").forGetter(s -> s.sequenceId),
            Codec.INT.fieldOf("total_points").forGetter(s -> s.totalPoints),
            Codec.INT.optionalFieldOf("prestige_points", 0).forGetter(s -> s.prestigePoints)
        ).apply(i, SequenceState::new));

        private final ResourceKey<Sequence> sequenceId;
        private @Nullable Sequence cachedSequence;

        private int totalPoints;
        private int prestigePoints;

        private SequenceState(ResourceKey<Sequence> sequenceId) {
            this(sequenceId, 0, 0);
        }

        private SequenceState(ResourceKey<Sequence> sequenceId, int totalPoints, int prestigePoints) {
            this.sequenceId = sequenceId;
            this.totalPoints = totalPoints;
            this.prestigePoints = prestigePoints;
        }

        /**
         * @return the sequence associated with this state, or null if it does not exist. If the sequence does not exist,
         * this SequenceState must be discarded.
         */
        public @Nullable Sequence getSequence(@Nullable RegistryAccess registryAccess) {
            if (cachedSequence == null && registryAccess != null) {
                cachedSequence = registryAccess.registryOrThrow(TierTowerRegistries.SEQUENCE).get(sequenceId);
            }

            return cachedSequence;
        }
    }
}
