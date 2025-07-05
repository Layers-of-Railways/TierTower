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

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import io.github.slimeistdev.tier_tower.base.data.TierTowerTierGen;
import io.github.slimeistdev.tier_tower.base.data.lang.LangGen;
import io.github.slimeistdev.tier_tower.events.CommonEvents;
import io.github.slimeistdev.tier_tower.network.TierTowerPackets;
import io.github.slimeistdev.tier_tower.registry.ModSetup;
import io.github.slimeistdev.tier_tower.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class TierTower implements ModInitializer {
	public static final String MOD_ID = "tier_tower";
	public static final String NAME = "Tier Tower";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	private static final Registrate REGISTRATE = Registrate.create(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("{} v{} is initializing! Commit hash: {}", NAME, TierTowerBuildInfo.VERSION, TierTowerBuildInfo.GIT_COMMIT);

		ModSetup.register();
		REGISTRATE.register();

		CommonEvents.register();
		TierTowerPackets.PACKETS.registerC2SListener();

		if (Utils.isDevEnv() && !Boolean.getBoolean("DATAGEN")) {
			MixinEnvironment.getCurrentEnvironment().audit();
		}
	}

	public static void gatherData(DataGenerator.PackGenerator gen) {
		REGISTRATE.addDataGenerator(ProviderType.LANG, LangGen::generate);
		gen.addProvider(TierTowerTierGen::new);
	}

	public static AbstractRegistrate<? extends AbstractRegistrate<?>> registrate() {
		return REGISTRATE;
	}

	public static ResourceLocation asResource(String id) {
		return new ResourceLocation(MOD_ID, id);
	}
}
