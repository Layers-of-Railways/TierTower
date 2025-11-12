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

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import io.github.slimeistdev.tier_tower.registry.TierTowerSequences;
import io.github.slimeistdev.tier_tower.registry.TierTowerTiers;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class TierTowerDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
        FabricDataGenerator.Pack pack = generator.createPack();
        TierTower.gatherData(pack, helper);
        TierTower.registrate().setupDatagen(pack, helper);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(TierTowerRegistries.TIER, TierTowerTiers::bootstrap);
        registryBuilder.add(TierTowerRegistries.SEQUENCE, TierTowerSequences::bootstrap);
    }
}
