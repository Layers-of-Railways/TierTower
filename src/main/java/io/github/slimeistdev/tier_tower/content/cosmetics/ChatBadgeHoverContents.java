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

import io.github.slimeistdev.tier_tower.TierTowerClient;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.Tier;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ChatBadgeHoverContents implements ComponentContents {
    private record Key(ResourceLocation tier, int level) {}

    private final UUID player;

    private @Nullable Key cacheKey;
    private @Nullable TranslatableContents delegate;

    public ChatBadgeHoverContents(UUID player) {
        this.player = player;
    }

    private void refreshDelegate() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        RegistryAccess registryAccess = mc.level.registryAccess();

        TowerSummary summary = TierTowerClient.SUBURB.getSummary(player);
        Sequence sequence = TierTowerClient.SUBURB.getSequence(summary.sequenceId(), registryAccess);
        if (sequence == null) return;

        Tier tier = sequence.getTier(summary.levelingState().tierIndex());
        int level = summary.levelingState().levelIndex();
        Key newKey = new Key(tier.getId(), level);
        if (delegate == null || !newKey.equals(cacheKey)) {
            cacheKey = newKey;
            delegate = new TranslatableContents("tier_tower.badge.hover", null, new Object[]{
                Component.translatable(tier.getTranslationKey()),
                level + 1
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
