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

import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.content.cosmetics.BadgeState;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientTowerManager {
    private final Map<UUID, TowerSummary> summaries = new HashMap<>();
    private final Map<UUID, BadgeState> badgeStates = new HashMap<>();
    private final Map<ResourceKey<Sequence>, Sequence> cachedSequences = new HashMap<>();

    public ClientTowerManager() {
        cleanUp();
    }

    public void cleanUp() {
        this.summaries.clear();
        this.badgeStates.clear();
        this.cachedSequences.clear();
    }

    public @NotNull TowerSummary getSummary(UUID player) {
        return summaries.getOrDefault(player, TowerSummary.ZERO);
    }

    public @NotNull BadgeState getBadgeState(UUID player) {
        return badgeStates.getOrDefault(player, BadgeState.ZERO);
    }

    public void setSummary(UUID player, TowerSummary summary, Minecraft mc) {
        summaries.put(player, summary);

        if (mc.level != null) {
            var registryAccess = mc.level.registryAccess();
            var sequence = getSequence(summary.sequenceId(), registryAccess);
            if (sequence != null) {
                var tier = sequence.getTier(summary.levelingState().tierIndex());
                badgeStates.put(player, new BadgeState(tier.getId(), summary.levelingState().levelIndex()));
            }
        }
    }

    public @Nullable Sequence getSequence(ResourceKey<Sequence> key, RegistryAccess registryAccess) {
        if (cachedSequences.containsKey(key)) {
            return cachedSequences.get(key);
        }

        Registry<Sequence> registry = registryAccess.registryOrThrow(TierTowerRegistries.SEQUENCE);
        Sequence sequence = registry.get(key);
        if (sequence != null) {
            cachedSequences.put(key, sequence);
        }

        return sequence;
    }
}
