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

import io.github.slimeistdev.tier_tower.registry.TierTowerBlockEntities;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlocks;
import io.github.slimeistdev.tier_tower.registry.TierTowerCommands;
import io.github.slimeistdev.tier_tower.registry.TierTowerCreativeModeTabs;
import io.github.slimeistdev.tier_tower.registry.TierTowerEntityTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerFluids;
import io.github.slimeistdev.tier_tower.registry.TierTowerItems;
import io.github.slimeistdev.tier_tower.registry.TierTowerMenuTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerParticleTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeSerializers;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import io.github.slimeistdev.tier_tower.registry.TierTowerSequences;
import io.github.slimeistdev.tier_tower.registry.TierTowerSoundEvents;
import io.github.slimeistdev.tier_tower.registry.TierTowerTiers;

public class ModSetup {
    public static void init() {
        TierTowerRegistries.init();
        TierTowerCommands.init();
        TierTowerTiers.init();
        TierTowerSequences.init();

        TierTowerCreativeModeTabs.register();
        TierTowerFluids.register();
        TierTowerBlocks.register();
        TierTowerItems.register();
        TierTowerEntityTypes.register();
        TierTowerMenuTypes.register();
        TierTowerBlockEntities.register();
        TierTowerSoundEvents.register();
        TierTowerParticleTypes.register();

        TierTowerRecipeTypes.register();
        TierTowerRecipeSerializers.register();
    }
}
