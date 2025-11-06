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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.UUID;

public class ChatBadgeUtil {
    public static MutableComponent decorateAutomatically(MutableComponent component) {
        component.getSiblings().replaceAll(c -> decorateAutomatically(c.copy()));

        HoverEvent hover = component.getStyle().getHoverEvent();
        if (hover != null) {
            HoverEvent.EntityTooltipInfo info = hover.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (info != null) {
                var style = Style.EMPTY.withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.translatable("tier_tower.special.badge.hover", info.id.toString())
                ));
                component.append(Component.translatable("tier_tower.special.badge", info.id.toString())
                    .withStyle(style));
            }
        }

        return component;
    }

    public static Component decorateAppend(Component original, UUID id) {
        MutableComponent out = original instanceof MutableComponent mutable ? mutable : original.copy();
        out.append(Component.translatable("tier_tower.special.badge", id.toString()));
        return out;
    }

    public static MutableComponent staticBadge(BadgeState badge) {
        Style style = Style.EMPTY.withHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            Component.translatable("tier_tower.special.badge.hover.static", badge.tierId().toString(), "" + badge.levelIndex())
        ));
        return Component.translatable("tier_tower.special.badge.static", badge.tierId().toString(), "" + badge.levelIndex())
            .withStyle(style);
    }
}
