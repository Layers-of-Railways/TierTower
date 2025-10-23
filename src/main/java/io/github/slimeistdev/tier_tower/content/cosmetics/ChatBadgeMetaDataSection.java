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

package io.github.slimeistdev.tier_tower.content.cosmetics;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record ChatBadgeMetaDataSection(int textColor, boolean shadow) {
    public static final ChatBadgeMetaDataSection DEFAULT = new ChatBadgeMetaDataSection(0xFFFFFF, false);

    public ChatBadgeMetaDataSection(int textColor, boolean shadow) {
        this.textColor = textColor & 0xFFFFFF;
        this.shadow = shadow;
    }

    private static final Codec<Integer> COLOR_CODEC = Codec.either(
        Codec.INT,
        RecordCodecBuilder.<Integer>create(i -> i.group(
            Codec.INT.fieldOf("r").forGetter(c -> (c >> 16) & 0xFF),
            Codec.INT.fieldOf("g").forGetter(c -> (c >> 8) & 0xFF),
            Codec.INT.fieldOf("b").forGetter(c -> c & 0xFF)
        ).apply(i, (r, g, b) -> (r << 16) | (g << 8) | b))
    ).xmap(
        e -> e.left().orElseGet(() -> e.right().orElseThrow()),
        Either::right
    );

    public static final Codec<ChatBadgeMetaDataSection> CODEC = RecordCodecBuilder.create(i -> i.group(
        COLOR_CODEC.optionalFieldOf("text_color", DEFAULT.textColor()).forGetter(ChatBadgeMetaDataSection::textColor),
        Codec.BOOL.optionalFieldOf("shadow", DEFAULT.shadow()).forGetter(ChatBadgeMetaDataSection::shadow)
    ).apply(i, ChatBadgeMetaDataSection::new));

    public static final MetadataSectionType<ChatBadgeMetaDataSection> TYPE = MetadataSectionType.fromCodec("tier_tower:chat_badge", CODEC);
}
