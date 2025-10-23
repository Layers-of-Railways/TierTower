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

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.datapack.DatapackHelper;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.network.PlayerSelection;
import io.github.slimeistdev.tier_tower.content.backend.tier.pack_data.SequencePackData;
import io.github.slimeistdev.tier_tower.content.backend.tier.pack_data.TierPackData;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import io.github.slimeistdev.tier_tower.network.packets.s2c.SequenceSyncPacket;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.Map.Entry;

public class TierManager {
    public static final ResourceLocation MAIN_SEQUENCE = TierTower.asResource("main");
    private static final Map<ResourceLocation, Sequence> SEQUENCES = new HashMap<>();
    private static int epoch = 1;

    public static int getEpoch() {
        return epoch;
    }

    public static @Nullable Sequence getSequence(ResourceLocation sequenceId) {
        return SEQUENCES.get(sequenceId);
    }

    public static SequenceSyncPacket makeSyncPacket() {
        return new SequenceSyncPacket(List.copyOf(SEQUENCES.values()));
    }

    public static class ReloadListener extends SimplePreparableReloadListener<ReloadListener.PreparedData> implements IdentifiableResourceReloadListener {
        private static final Gson GSON = new Gson();
        public static final ResourceLocation ID = TierTower.asResource("tiers");
        public static final ReloadListener INSTANCE = new ReloadListener();
        private static final Logger LOGGER = LogUtils.getLogger();

        protected ReloadListener() {}

        @Override
        public ResourceLocation getFabricId() {
            return ID;
        }

        @Override
        protected @NotNull PreparedData prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, TierPackData> tiers = new HashMap<>();
            Map<ResourceLocation, SequencePackData> sequences = new HashMap<>();

            DatapackHelper.scanDirectory(resourceManager, "tier_tower/tier", GSON, "tier", TierPackData.CODEC, tiers, LOGGER);
            DatapackHelper.scanDirectory(resourceManager, "tier_tower/sequence", GSON, "tier sequence", SequencePackData.CODEC, sequences, LOGGER);

            return new PreparedData(tiers, sequences);
        }

        @Override
        protected void apply(@NotNull PreparedData prepared, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
            SEQUENCES.clear();

            Set<ResourceLocation> usedTiers = new HashSet<>();

            for (Entry<ResourceLocation, SequencePackData> entry : prepared.sequences().entrySet()) {
                Sequence sequence = processSequence(entry.getKey(), entry.getValue(), prepared.tiers(), usedTiers);
                if (sequence != null) {
                    SEQUENCES.put(entry.getKey(), sequence);
                }
            }

            Set<ResourceLocation> unusedTiers = new HashSet<>(prepared.tiers().keySet());
            unusedTiers.removeAll(usedTiers);

            if (!unusedTiers.isEmpty()) {
                LOGGER.warn("The following tiers were defined but not used in any sequence: {}. They will not be accessible.", unusedTiers);
            }

            LOGGER.info("Loaded {} sequences.", SEQUENCES.size());

            if (!SEQUENCES.containsKey(MAIN_SEQUENCE)) {
                LOGGER.error("Main sequence {} is not defined in the datapack! This is required for the mod to function properly. A fallback will be generated.", MAIN_SEQUENCE);
                SEQUENCES.put(
                    MAIN_SEQUENCE,
                    new Sequence(
                        MAIN_SEQUENCE,
                        new Tier[] {
                            new Tier(
                                TierTower.asResource("quartz"),
                                new int[] { 100, 200, 300, 400, 500 }
                            )
                        },
                        null
                    )
                );
            }

            epoch++;

            if (epoch > 2) {
                TierTowerPackets.PACKETS.sendTo(PlayerSelection.all(), makeSyncPacket());
            }
        }

        private static @Nullable Sequence processSequence(ResourceLocation sequenceId, SequencePackData sequenceData, Map<ResourceLocation, TierPackData> tiers, Set<ResourceLocation> usedTiers) {
            if (sequenceData.tiers().isEmpty()) {
                LOGGER.warn("Sequence {} has no tiers defined, skipping sequence.", sequenceId);
                return null;
            }
            List<Tier> tierList = new ArrayList<>(sequenceData.tiers().size());
            int levelingCost = sequenceData.defaultBaseLevelingCost();

            Set<ResourceLocation> sequenceUsedTiers = new HashSet<>();

            for (int i = 0; i < sequenceData.tiers().size(); i++) {
                ResourceLocation tierId = sequenceData.tiers().get(i);
                TierPackData tierData = tiers.get(tierId);
                if (tierData == null) {
                    LOGGER.warn("Tier {} in sequence {} not found, skipping tier.", tierId, sequenceId);
                    continue;
                }
                if (!usedTiers.add(tierId)) {
                    LOGGER.warn("Tier {} in sequence {} is already used in another sequence, skipping tier.", tierId, sequenceId);
                    continue;
                }
                if (!sequenceUsedTiers.add(tierId)) {
                    LOGGER.warn("Tier {} in sequence {} is duplicated, skipping tier.", tierId, sequenceId);
                    continue;
                }

                levelingCost = tierData.baseLevelingCost().orElse(levelingCost);

                int[] costs = new int[tierData.levelCount()];
                costs[0] = levelingCost;

                double cost = levelingCost;
                EvaluationContext ctx = new EvaluationContext()
                    .set("prev", cost)
                    .set("levels", costs.length);

                for (int j = 0; j < costs.length; j++) {
                    try {
                        cost = tierData.levelingCostFunction().evaluate(ctx);
                    } catch (EvaluationException e) {
                        LOGGER.error("Failed to evaluate leveling cost for level {} of tier {} in sequence {}. It will inherit the cost of level {}", j+1, tierId, sequenceId, j, e);
                    }
                    ctx.set("prev", cost);

                    // the loop computes one more cost than is needed for this tier, to provide the next tier with a base cost
                    if (j + 1 < costs.length) {
                        costs[j + 1] = (int) Math.max(1, Math.round(cost));
                    }
                }
                levelingCost = Math.max(1, costs[costs.length - 1]);

                tierList.add(new Tier(tierId, costs));
            }

            if (tierList.isEmpty()) {
                LOGGER.warn("Sequence {} has no valid tiers defined, skipping sequence.", sequenceId);
                return null;
            }

            return new Sequence(sequenceId, tierList.toArray(new Tier[0]), sequenceData.nextSequence().orElse(null));
        }

        protected record PreparedData(Map<ResourceLocation, TierPackData> tiers, Map<ResourceLocation, SequencePackData> sequences) {}
    }
}
