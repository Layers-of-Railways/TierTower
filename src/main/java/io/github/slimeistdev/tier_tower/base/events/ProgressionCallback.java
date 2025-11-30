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

package io.github.slimeistdev.tier_tower.base.events;

import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface ProgressionCallback {
    Event<ProgressionCallback>
        LEVEL = create(),
        TIER = create(),
        PRESTIGE = create();

    private static Event<ProgressionCallback> create() {
        return EventFactory.createArrayBacked(
            ProgressionCallback.class,
            listeners -> (player, tower) -> {
                for (ProgressionCallback listener : listeners) {
                    listener.onLevelUp(player, tower);
                }
            }
        );
    }

    void onLevelUp(@NotNull ServerPlayer player, @NotNull PlayerTower tower);
}
