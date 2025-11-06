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

package io.github.slimeistdev.tier_tower.network.packets.s2c;

import io.github.slimeistdev.tier_tower.TierTowerClient;
import io.github.slimeistdev.tier_tower.base.network.S2CPacket;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record TowerSummaryPacket(UUID player, TowerSummary summary) implements S2CPacket {
    public TowerSummaryPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), TowerSummary.read(buf));
    }

    public TowerSummaryPacket(PlayerTower tower) {
        this(tower.getPlayerId(), tower.summarize());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        summary.write(buffer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Minecraft mc) {
        TierTowerClient.SUBURB.setSummary(player, summary, mc);
    }
}
