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

package io.github.slimeistdev.tier_tower.events;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.network.PlayerSelection;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import io.github.slimeistdev.tier_tower.network.packets.s2c.TowerSummaryPacket;
import io.github.slimeistdev.tier_tower.registry.TierTowerCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;

public class CommonEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(TierTowerCommands::register);
        ServerWorldEvents.LOAD.register((server, level) -> onLoadLevel(level));
        ServerPlayConnectionEvents.JOIN.register((connection, packetSender, server) -> onPlayerJoin(connection.player));
    }

    private static void onLoadLevel(LevelAccessor level) {
        TierTower.CITY.levelLoaded(level);
    }

    private static void onPlayerJoin(ServerPlayer player) {
        TierTowerPackets.PACKETS.onPlayerJoin(player);

        MinecraftServer server = player.getServer();
        if (server == null) return;

        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
            PlayerTower tower = TierTower.CITY.getTower(otherPlayer);
            if (tower == null) continue;

            if (otherPlayer == player) {
                TierTowerPackets.PACKETS.sendTo(PlayerSelection.all(), new TowerSummaryPacket(tower));
            } else {
                TierTowerPackets.PACKETS.sendTo(PlayerSelection.of(player), new TowerSummaryPacket(tower));
            }
        }
    }
}
