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

package io.github.slimeistdev.tier_tower.mixin_ducks.common;

import com.mojang.datafixers.util.Either;
import io.github.slimeistdev.tier_tower.content.cosmetics.BadgeState;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Style_Duck {
    /* WARN: the badge style is very ephemeral, and should only be set immediately prior to rendering. */
    Style tt$withBadge(Either<UUID, BadgeState> playerOrBadge);

    @Nullable Either<UUID, BadgeState> tt$getBadge();
}
