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

package io.github.slimeistdev.tier_tower.content.item_sink;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.tier_tower.registry.TierTowerParticleTypes;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class EminentSnakeParticleOptions extends DustParticleOptionsBase {
    public static final Codec<EminentSnakeParticleOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
        ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(options -> options.color),
        Codec.FLOAT.fieldOf("scale").forGetter(options -> options.scale)
    ).apply(i, EminentSnakeParticleOptions::new));

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<EminentSnakeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull EminentSnakeParticleOptions fromCommand(@NotNull ParticleType<EminentSnakeParticleOptions> particleType, @NotNull StringReader reader) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(reader);
            reader.expect(' ');
            float f = reader.readFloat();
            return new EminentSnakeParticleOptions(vector3f, f);
        }

        @Override
        public @NotNull EminentSnakeParticleOptions fromNetwork(@NotNull ParticleType<EminentSnakeParticleOptions> particleType, @NotNull FriendlyByteBuf buffer) {
            return new EminentSnakeParticleOptions(DustParticleOptionsBase.readVector3f(buffer), buffer.readFloat());
        }
    };

    public EminentSnakeParticleOptions(Vector3f color, float scale) {
        super(color, scale);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return TierTowerParticleTypes.EMINENT_SNAKE;
    }
}
