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

package io.github.slimeistdev.tier_tower.registry;

import io.github.slimeistdev.tier_tower.TierTower;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class TierTowerSoundEvents {
    public static final Holder.Reference<SoundEvent> EMINENT_SNAKE_AMBIENT = registerForHolder("eminent_snake.ambient");
    public static final Holder.Reference<SoundEvent> EMINENT_SNAKE_SWOOP = registerForHolder("eminent_snake.swoop");
    public static final Holder.Reference<SoundEvent> EMINENT_SNAKE_STRIKE = registerForHolder("eminent_snake.strike");
    public static final Holder.Reference<SoundEvent> EMINENCE_PICKUP = registerForHolder("eminence.pickup");

    private static SoundEvent register(String id) {
        return register(TierTower.asResource(id));
    }

    private static SoundEvent register(ResourceLocation id) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static Holder.Reference<SoundEvent> registerForHolder(String id) {
        return registerForHolder(TierTower.asResource(id));
    }

    private static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation id) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        TierTower.LOGGER.info("Registering sound events for " + TierTower.NAME);
    }
}
