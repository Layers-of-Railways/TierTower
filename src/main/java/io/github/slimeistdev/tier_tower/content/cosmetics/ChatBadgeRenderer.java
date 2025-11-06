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

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.utils.CacheInvalidationReloadListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
@SuppressWarnings("SameParameterValue")
public class ChatBadgeRenderer {
    private record BadgeRenderData(GlyphRenderTypes renderTypes, ChatBadgeMetaDataSection meta) {}

    private static final Style BADGE_STYLE = Style.EMPTY.withFont(TierTower.BADGE_FONT);
    private static final Map<ResourceLocation, BadgeRenderData> RENDER_CACHE = new HashMap<>();

    static {
        CacheInvalidationReloadListener.CLIENT_RESOURCES.registerCallback(RENDER_CACHE::clear);
    }

    private static BadgeRenderData getBadgeRenderData(BadgeState badge) {
        return RENDER_CACHE.computeIfAbsent(badge.tierId(), id -> {
            ResourceLocation texture = new ResourceLocation(
                id.getNamespace(),
                "textures/tier_tower/badge/" + id.getPath() + ".png"
            );
            GlyphRenderTypes renderTypes = GlyphRenderTypes.createForColorTexture(texture);

            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            ChatBadgeMetaDataSection meta = resourceManager.getResource(texture)
                .flatMap(r -> {
                    try {
                        return r.metadata().getSection(ChatBadgeMetaDataSection.TYPE);
                    } catch (IOException e) {
                        return Optional.empty();
                    }
                })
                .orElse(ChatBadgeMetaDataSection.DEFAULT);

            return new BadgeRenderData(renderTypes, meta);
        });
    }

    private static int dim(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = (r * 3) / 5;
        g = (g * 3) / 5;
        b = (b * 3) / 5;

        return (r << 16) | (g << 8) | b;
    }

    public static void renderBadge(float x, float y, int packedLight, Matrix4f pose, MultiBufferSource bufferSource,
                                   Font.DisplayMode mode, BadgeState badge, Font font) {
        BadgeRenderData badgeRenderData = ChatBadgeRenderer.getBadgeRenderData(badge);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(badgeRenderData.renderTypes().select(mode));
        quad(
            x, y - 1,
            x + 18f, y + 9f,
            0f, 0f,
            18 / 32f, 9 / 16f,
            packedLight,
            pose,
            vertexConsumer
        );

        MutableComponent levelComponent = Component.literal(String.valueOf(badge.levelIndex() + 1))
            .withStyle(BADGE_STYLE);

        int width = font.width(levelComponent);

        Matrix4f pose2 = new Matrix4f(pose);
        pose2.translateLocal(0, 0, 0.001f);

        int color = badgeRenderData.meta().textColor();
        font.drawInBatch(levelComponent, x + (18 - width)/2f, y - 2, color, false, pose2, bufferSource, mode, 0, packedLight);

        if (badgeRenderData.meta().shadow()) {
            pose2.translateLocal(0.5f, 0.5f, -0.0005f);
            font.drawInBatch(levelComponent, x + (18 - width) / 2f, y - 2, dim(color), false, pose2, bufferSource, mode, 0, packedLight);
        }
    }

    static void quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1,
                             int packedLight, Matrix4f pose, VertexConsumer vertexConsumer) {
        quad(x0, y0, x1, y1, u0, v0, u1, v1, packedLight, pose, vertexConsumer, 0f);
    }

    static void quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1,
                             int packedLight, Matrix4f pose, VertexConsumer vertexConsumer, float z) {
        vertexConsumer.vertex(pose, x0, y0, z).color(-1).uv(u0, v0).uv2(packedLight).endVertex();
        vertexConsumer.vertex(pose, x0, y1, z).color(-1).uv(u0, v1).uv2(packedLight).endVertex();
        vertexConsumer.vertex(pose, x1, y1, z).color(-1).uv(u1, v1).uv2(packedLight).endVertex();
        vertexConsumer.vertex(pose, x1, y0, z).color(-1).uv(u1, v0).uv2(packedLight).endVertex();
    }
}
