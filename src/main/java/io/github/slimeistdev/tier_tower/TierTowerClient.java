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

package io.github.slimeistdev.tier_tower;

import io.github.slimeistdev.tier_tower.events.ClientEvents;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import net.fabricmc.api.ClientModInitializer;

public class TierTowerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TierTower.LOGGER.info("Tier Tower is loading on the client!");

        ClientEvents.register();

        TierTowerPackets.PACKETS.registerS2CListener();
    }
}
