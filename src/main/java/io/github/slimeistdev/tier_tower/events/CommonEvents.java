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
import io.github.slimeistdev.tier_tower.base.events.DynamicRegistryFreezeCallback;
import io.github.slimeistdev.tier_tower.base.events.ProgressionCallback;
import io.github.slimeistdev.tier_tower.base.network.PlayerSelection;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import io.github.slimeistdev.tier_tower.network.packets.s2c.TowerSummaryPacket;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import io.github.slimeistdev.tier_tower.registry.TierTowerSoundEvents;
import io.github.slimeistdev.tier_tower.utils.Utils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;

public class CommonEvents {
    public static void register() {
        ServerWorldEvents.LOAD.register((server, level) -> onLoadLevel(level));
        ServerPlayConnectionEvents.JOIN.register((connection, packetSender, server) -> onPlayerJoin(connection.player));
        DynamicRegistryFreezeCallback.POST.register(CommonEvents::onDynamicRegistryFreeze);

        registerProgressionSound(ProgressionCallback.LEVEL, TierTowerSoundEvents.LEVEL_UP, 0.8f, 0.9f, true);
        registerProgressionSound(ProgressionCallback.TIER, TierTowerSoundEvents.TIER_UP, 1.0f, 0.9f, true);
        registerProgressionSound(ProgressionCallback.PRESTIGE, TierTowerSoundEvents.PRESTIGE_THUNDER, 1.0f, 1.0f, false);
        registerProgressionSound(ProgressionCallback.PRESTIGE, TierTowerSoundEvents.PRESTIGE_POWER_DOWN, 1.0f, 0.8f, false);
    }

    private static void registerProgressionSound(Event<ProgressionCallback> event, Holder.Reference<SoundEvent> sound, float volume, float pitch, boolean onlySelf) {
        event.register((player, $) -> {
            var level = player.level();
            var pos = player.position();
            if (onlySelf) {
                player.connection.send(new ClientboundSoundEntityPacket(sound, SoundSource.PLAYERS, player, volume, pitch, level.random.nextLong()));
            } else {
                level.playSeededSound(null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, volume, pitch, level.random.nextLong());
            }
        });
    }

    private static void onLoadLevel(LevelAccessor level) {
        TierTower.CITY.levelLoaded(level);
    }

    private static void onPlayerJoin(ServerPlayer player) {
        TierTowerPackets.PACKETS.onPlayerJoin(player);

        MinecraftServer server = player.getServer();
        if (server == null) return;

        PlayerTower tower = TierTower.CITY.getTower(player);
        if (tower != null) {
            TierTowerPackets.PACKETS.sendTo(PlayerSelection.all(), new TowerSummaryPacket(tower));
        }

        for (PlayerTower otherTower : TierTower.CITY.towers.values()) {
            // it appears like we would send the joining player's tower twice, but this fires before the player has
            // been added to the player list, so PlayerSelection.all() misses them
            TierTowerPackets.PACKETS.sendTo(PlayerSelection.of(player), new TowerSummaryPacket(otherTower));
        }
    }

    private static void onDynamicRegistryFreeze(Registry<?> registry, HolderLookup.Provider lookupProvider) {
        Registry<Sequence> sequences = Utils.castRegistry(registry, TierTowerRegistries.SEQUENCE);
        if (sequences != null) {
            for (Sequence sequence : sequences) {
                sequence.freeze(lookupProvider);
            }
        }
    }
}
