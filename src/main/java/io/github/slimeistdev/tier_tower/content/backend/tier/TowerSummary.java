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

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence.LevelingState;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;

// Intended for the client's view of a player's progression
public record TowerSummary(ResourceKey<Sequence> sequenceId, LevelingState levelingState) {
    public static final TowerSummary ZERO = new TowerSummary(TierTower.MAIN_SEQUENCE, LevelingState.ZERO);

    public static TowerSummary read(FriendlyByteBuf buf) {
        return new TowerSummary(buf.readResourceKey(TierTowerRegistries.SEQUENCE), LevelingState.read(buf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceKey(sequenceId);
        levelingState.write(buf);
    }
}
