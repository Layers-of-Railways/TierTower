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

package io.github.slimeistdev.tier_tower.mixin.client;

import io.github.slimeistdev.tier_tower.TierTowerClient;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeRenderer;
import io.github.slimeistdev.tier_tower.mixin_ducks.common.Style_Duck;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class MixinStringRenderOutput {
    @Shadow(aliases = "field_24240")
    Font this$0;

    @Shadow
    float x;

    @Shadow
    float y;

    @Shadow @Final
    private boolean dropShadow;

    @Shadow @Final
    MultiBufferSource bufferSource;

    @Shadow @Final
    private Font.DisplayMode mode;

    @Shadow @Final
    private Matrix4f pose;

    @Shadow @Final private int packedLightCoords;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void renderBadge(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        UUID playerId = ((Style_Duck) style).tt$getBadgePlayer();
        if (playerId == null) return;

        // no default rendering
        cir.setReturnValue(true);

        TowerSummary summary = TierTowerClient.SUBURB.getSummary(playerId);
        Sequence sequence = TierTowerClient.SUBURB.getSequence(summary.sequenceId());
        if (sequence == null) return;

        if (!dropShadow) {
            ChatBadgeRenderer.renderBadge(x + 1, y, packedLightCoords, pose, bufferSource, mode, sequence, summary, this$0);
        }

        x += 20;
    }
}
