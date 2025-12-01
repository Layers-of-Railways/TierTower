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
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.mixin_ducks.common.Style_Duck;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record ChatBadgeContents(Either<UUID, BadgeState> playerOrBadge) implements ComponentContents {
    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, @NotNull Style style) {
        return styledContentConsumer.accept(((Style_Duck) style.withFont(TierTower.BADGE_FONT)).tt$withBadge(playerOrBadge), TierTower.BADGE_PLACEHOLDER);
    }
}
