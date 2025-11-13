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
import io.github.slimeistdev.tier_tower.content.item_sink.EminentSnakeParticleOptions;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class TierTowerParticleTypes {
    public static final ParticleType<EminentSnakeParticleOptions> EMINENT_SNAKE = register(
        "eminent_snake",
        false,
        EminentSnakeParticleOptions.DESERIALIZER,
        type -> EminentSnakeParticleOptions.CODEC
    );

    @SuppressWarnings({"SameParameterValue", "deprecation"})
    private static <T extends ParticleOptions> ParticleType<T> register(
        String id,
        boolean overrideLimiter,
        ParticleOptions.Deserializer<T> deserializer,
        Function<ParticleType<T>, Codec<T>> codecFactory
    ) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, TierTower.asResource(id), new ParticleType<T>(overrideLimiter, deserializer) {
            @Override
            public @NotNull Codec<T> codec() {
                return codecFactory.apply(this);
            }
        });
    }

    public static void register() {
        TierTower.LOGGER.info("Registering particle types for " + TierTower.NAME);
    }
}
