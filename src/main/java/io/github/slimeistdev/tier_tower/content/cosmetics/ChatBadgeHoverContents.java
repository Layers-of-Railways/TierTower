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
import io.github.slimeistdev.tier_tower.TierTowerClient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ChatBadgeHoverContents implements ComponentContents {
    private final Either<UUID, BadgeState> playerOrBadge;

    private @Nullable BadgeState cachedBadge;
    private @Nullable TranslatableContents delegate;

    public ChatBadgeHoverContents(Either<UUID, BadgeState> playerOrBadge) {
        this.playerOrBadge = playerOrBadge;
    }

    private void refreshDelegate() {
        BadgeState badge = playerOrBadge.map(TierTowerClient.SUBURB::getBadgeState, b -> b);
        if (delegate == null || !badge.equals(cachedBadge)) {
            cachedBadge = badge;
            delegate = new TranslatableContents("tier_tower.badge.hover", null, new Object[]{
                Component.translatable(badge.tierId().toLanguageKey("tier_tower.tier")),
                badge.levelIndex() + 1
            });
        }
    }

    @Override
    public <T> @NotNull Optional<T> visit(@NotNull FormattedText.StyledContentConsumer<T> styledContentConsumer, @NotNull Style style) {
        refreshDelegate();
        return delegate != null ? delegate.visit(styledContentConsumer, style)
                                : ComponentContents.super.visit(styledContentConsumer, style);
    }
}
