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

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.utils.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalTowerManager {
    private static final Integer LAZY_TICK_INTERVAL = Integer.getInteger("tier_tower.global_tower_manager.lazy_tick_interval", 1);

    private TowerSavedData savedData;
    public Map<UUID, PlayerTower> towers;
    private RegistryAccess registryAccess;

    public GlobalTowerManager() {
        cleanUp();
    }

    private void warnIfClient() {
        if (Thread.currentThread().getName().equals("Render thread")) {
            long start = System.currentTimeMillis();
            TierTower.LOGGER.error("Tower manager should not be accessed on the client"); // set breakpoint here when developing
            if (Utils.isDevEnv()) {
                long end = System.currentTimeMillis();
                if (end - start < 50) { // crash if breakpoint wasn't set
                    throw new RuntimeException("Illegal tower manager access performed on client, please set a breakpoint above");
                }
            } else {
                TierTower.LOGGER.error("Stacktrace: ", new RuntimeException("Illegal tower manager access performed on client"));
            }
        }
    }

    public void levelLoaded(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null || server.overworld() != level)
            return;
        cleanUp();
        savedData = null;
        registryAccess = level.registryAccess();
        loadCityData(server);
    }

    private void loadCityData(MinecraftServer server) {
        if (savedData != null)
            return;
        savedData = TowerSavedData.load(server);
        for (PlayerTower tower : savedData.getTowers()) {
            tower.registryAccess = registryAccess;
            towers.put(tower.getPlayerId(), tower);
        }
    }

    private void cleanUp() {
        towers = new HashMap<>();
        registryAccess = null;
    }

    public void markCityDirty() {
        if (savedData != null)
            savedData.setDirty();
    }

    public @NotNull PlayerTower getOrCreateTower(Player player) {
        return getOrCreateTower(player.getUUID());
    }

    public @NotNull PlayerTower getOrCreateTower(UUID uuid) {
        warnIfClient();

        if (towers.containsKey(uuid)) {
            return towers.get(uuid);
        } else {
            PlayerTower tower = new PlayerTower(uuid);
            tower.registryAccess = registryAccess;
            towers.put(uuid, tower);
            markCityDirty();
            return tower;
        }
    }

    public @Nullable PlayerTower getTower(Player player) {
        return getTower(player.getUUID());
    }

    public @Nullable PlayerTower getTower(UUID uuid) {
        warnIfClient();
        return towers.get(uuid);
    }

    public void tick(int tickCount) {
        warnIfClient();
        for (PlayerTower tower : towers.values()) {
            if (LAZY_TICK_INTERVAL == 1 || (tower.getPlayerId().hashCode() + tickCount) % LAZY_TICK_INTERVAL == 0) {
                tower.registryAccess = registryAccess;
                tower.lazyTick(LAZY_TICK_INTERVAL);
            }
        }
    }
}
