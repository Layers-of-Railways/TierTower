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

import com.mojang.serialization.Codec;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class TierTowerRegistries {
    public static final ResourceKey<Registry<TierPackData>> TIER = key("tier");
    public static final ResourceKey<Registry<Sequence>> SEQUENCE = key("sequence");

    public static final Codec<ResourceKey<TierPackData>> TIER_CODEC = ResourceKey.codec(TIER);
    public static final Codec<ResourceKey<Sequence>> SEQUENCE_CODEC = ResourceKey.codec(SEQUENCE);

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(TierTower.asResource(name));
    }

    public static void init() {
        DynamicRegistries.registerSynced(TIER, TierPackData.CODEC);
        DynamicRegistries.registerSynced(SEQUENCE, Sequence.CODEC);
    }
}
