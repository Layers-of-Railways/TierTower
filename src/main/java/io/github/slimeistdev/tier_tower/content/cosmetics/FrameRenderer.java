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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.slimeistdev.tier_tower.TierTowerClient;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.Tier;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

import static io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeRenderer.quad;

@Environment(EnvType.CLIENT)
public class FrameRenderer {
    private static final Map<ResourceLocation, GlyphRenderTypes> RENDER_CACHE = new HashMap<>();

    private static GlyphRenderTypes getFrameRenderTypes(Tier tier) {
        return RENDER_CACHE.computeIfAbsent(tier.getId(), id -> GlyphRenderTypes.createForColorTexture(new ResourceLocation(
            id.getNamespace(),
            "textures/tier_tower/frame/" + id.getPath() + ".png"
        )));
    }

    public static void renderFrame(
        AbstractClientPlayer player,
        Component displayName,
        PoseStack ms,
        MultiBufferSource buffer,
        int packedLight,
        EntityRenderDispatcher entityRenderDispatcher,
        Font font
    ) {
        TowerSummary summary = TierTowerClient.SUBURB.getSummary(player.getUUID());
        Sequence sequence = TierTowerClient.SUBURB.getSequence(summary.sequenceId(), player.level().registryAccess());
        if (sequence == null) return;

        Tier tier = sequence.getTier(summary.levelingState().tierIndex());

        double d = entityRenderDispatcher.distanceToSqr(player);
        if (!(d > 4096.0)) {
            float globalY = player.getNameTagOffsetY();
            int localY = "deadmau5".equals(displayName.getString()) ? -10 : 0;

            ms.pushPose();

            ms.translate(0.0F, globalY, 0.0F);
            ms.mulPose(entityRenderDispatcher.cameraOrientation());
            ms.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = ms.last().pose();
            float halfWidth = (float)(font.width(displayName) / 2);

            Font.DisplayMode mode = Font.DisplayMode.NORMAL;
            VertexConsumer vc = buffer.getBuffer(getFrameRenderTypes(tier).select(mode));

            int padding = 5;

            // left
            quad(
                -halfWidth - padding, localY - 4,
                -halfWidth - padding + 16f, localY + 12,
                0f, 0f,
                16 / 48f, 16 / 16f,
                packedLight,
                matrix4f,
                vc
            );

            // center
            quad(
                -8, localY - 4,
                8, localY + 12,
                16 / 48f, 0f,
                32 / 48f, 16 / 16f,
                packedLight,
                matrix4f,
                vc
            );

            // right
            quad(
                halfWidth + padding - 16f, localY - 4,
                halfWidth + padding, localY + 12,
                32 / 48f, 0f,
                48 / 48f, 16 / 16f,
                packedLight,
                matrix4f,
                vc
            );

            ms.popPose();
        }
    }
}
