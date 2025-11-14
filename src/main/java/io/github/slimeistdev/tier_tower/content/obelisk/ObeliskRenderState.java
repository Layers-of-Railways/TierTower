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

package io.github.slimeistdev.tier_tower.content.obelisk;

import io.github.slimeistdev.tier_tower.TierTowerClient;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.Tier;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.content.cosmetics.BadgeState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.text.NumberFormat;
import java.util.Locale;

@Environment(EnvType.CLIENT)
class ObeliskRenderState {
    boolean valid;

    AbstractClientPlayer player;
    TowerSummary summary;
    Sequence sequence;
    Tier tier;

    float nextTierProgress;
    float nextLevelProgress;

    BadgeState current;
    BadgeState nextTier;
    BadgeState nextLevel;

    FormattedCharSequence progressText;

    ObeliskRenderState() {
        valid = false;
    }

    void update(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        valid = mc.level != null;
        if (!valid) return;

        Vec3 center = pos.getCenter();
        player = mc.level.getNearestPlayer(center.x, center.y, center.z, 32, false) instanceof AbstractClientPlayer p ? p : null;
        valid = player != null;
        if (!valid) return;

        // basic meta

        summary = TierTowerClient.SUBURB.getSummary(player.getUUID());
        sequence = TierTowerClient.SUBURB.getSequence(summary.sequenceId(), mc.level.registryAccess());
        valid = sequence != null;
        if (!valid) return;

        int currentTier = summary.levelingState().tierIndex();
        int currentLevel = summary.levelingState().levelIndex();
        tier = sequence.getTier(currentTier);

        // progress bars

        int maxLevel = tier.getLevelCount() - 1;
        int nextTierTotalPoints = tier.getCostUpTo(maxLevel) + tier.getLevelingCost(maxLevel);
        int nextTierProgressPoints = tier.getCostUpTo(currentLevel) + summary.levelingState().levelPoints();
        int nextLevelTotalPoints = tier.getLevelingCost(currentLevel);
        int nextLevelProgressPoints = summary.levelingState().levelPoints();

        nextTierProgress = (float) nextTierProgressPoints / nextTierTotalPoints;
        nextLevelProgress = (float) nextLevelProgressPoints / nextLevelTotalPoints;

        current = new BadgeState(tier.getId(), currentLevel);

        if (currentTier + 1 < sequence.getTierCount()) {
            nextTier = new BadgeState(sequence.getTier(currentTier + 1).getId(), 0);
        } else {
            nextTier = null;
        }

        if (current.levelIndex() + 1 < tier.getLevelCount()) {
            nextLevel = new BadgeState(current.tierId(), current.levelIndex() + 1);
        } else if (currentTier + 1 < sequence.getTierCount()) {
            nextLevel = new BadgeState(sequence.getTier(currentTier + 1).getId(), 0);
        } else {
            nextLevel = null;
        }

        // progress text

        Locale locale = Locale.forLanguageTag(mc.getLanguageManager().getSelected());
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        int totalPoints = sequence.getCostUpTo(currentTier) + tier.getCostUpTo(currentLevel)
            + summary.levelingState().levelPoints() + summary.levelingState().surplusPoints();

        Component progressComponent = Component.literal(numberFormat.format(totalPoints));
        progressText = progressComponent.getVisualOrderText();
    }
}
