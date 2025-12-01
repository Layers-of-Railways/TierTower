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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.cosmetics.BadgeState;
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeMetaDataSection;
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeUtil;
import io.github.slimeistdev.tier_tower.content.obelisk.ObeliskBlock.DisplayFace;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ObeliskBlockEntityRenderer implements BlockEntityRenderer<ObeliskBlockEntity> {
    private static final ResourceLocation BACKGROUND = TierTower.asResource("textures/gui/obelisk_background.png");
    private static final ResourceLocation EMPTY_FRAME = TierTower.asResource("textures/gui/obelisk_frame_empty.png");

    private static final ResourceLocation TOWER_TOP = TierTower.asResource("textures/gui/tower/tower_top.png");
    private static final ResourceLocation TOWER_EMPTY = TierTower.asResource("textures/gui/tower/tower_empty.png");
    private static final ResourceLocation TOWER_BOTTOM = TierTower.asResource("textures/gui/tower/tower_bottom.png");
    private static final int TOWER_SECTION_WIDTH = 128;
    private static final int TOWER_SECTION_HEIGHT = 16;

    private static final ResourceLocation PROGRESS_BG = TierTower.asResource("textures/gui/progress/progress_background.png");
    private static final ResourceLocation PROGRESS_FG = TierTower.asResource("textures/gui/progress/progress_foreground.png");

    private final Font font;

    public ObeliskBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(@NotNull ObeliskBlockEntity be, float partialTick, @NotNull PoseStack ms, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(ObeliskBlock.FACING);
        DisplayFace displayFace = state.getValue(ObeliskBlock.DISPLAY_FACE);
        ObeliskRenderState ors = be.renderState;

        if (displayFace == DisplayFace.NONE || mc.level == null) {
            return;
        }

        ms.pushPose();

        int width = 3;
        int height = 4;
        float rot = facing.toYRot();

        // setup basic transforms
        ms.translate(0.5f, 0.5f, 0.5f);
        ms.mulPose(Axis.YP.rotationDegrees(-rot));
        ms.translate(-0.5f, -0.5f, -0.5f);
        ms.scale(1/16f, 1/16f, 1/16f);

        // setup per-DisplayFace transforms
        float xOffset = switch (displayFace) {
            case UP, DOWN -> 8 - (width * 8f);
            case LEFT -> -width * 16f;
            case RIGHT -> 16f;
            case NONE -> throw new IllegalStateException();
        };
        float yOffset = switch (displayFace) {
            case LEFT, RIGHT -> -24f;
            case UP -> 16f;
            case DOWN -> -height * 16f;
            case NONE -> throw new IllegalStateException();
        };
        ms.translate(xOffset, yOffset, 16.25f);

        BlockPos frontPos = be.getBlockPos().offset(facing.getNormal());
        int frontPackedLight = LightTexture.pack(
            mc.level.getBrightness(LightLayer.BLOCK, frontPos),
            mc.level.getBrightness(LightLayer.SKY, frontPos)
        );

        ResourceLocation background = ors.valid ? ors.tier.getTexture("obelisk") : EMPTY_FRAME;
        renderBackground(ms, buffer, frontPackedLight, background, width, height, 0f);
        if (!ors.valid) {
            ms.popPose();
            return;
        }

        int textColor = ChatBadgeMetaDataSection.get(ors.tier.getTexture("badge")).textColor();

        // render player faces and name
        {
            ms.pushPose();

            FormattedCharSequence name = ors.player.getDisplayName().getVisualOrderText();
            int nameWidth = font.width(name);
            ms.translate(width * 8f, height * 16f - 2f, 0.125f);
            ms.scale(1/2f, -1/2f, 1/2f);

            //noinspection IntegerDivisionInFloatingPointContext
            font.drawInBatch(
                name,
                -nameWidth / 2, 0f,
                0xFFFFFF, true,
                ms.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                frontPackedLight
            );
            GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
            guiGraphics.pose().last().pose().set(ms.last().pose());
            guiGraphics.pose().last().normal().set(ms.last().normal());
            boolean drawHat = ors.player.isModelPartShown(PlayerModelPart.HAT);
            boolean upsideDown = LivingEntityRenderer.isEntityUpsideDown(ors.player);
            for (int sign = -1; sign <= 1; sign += 2) {
                RenderSystem.enableDepthTest();
                PlayerFaceRenderer.draw(
                    guiGraphics,
                    ors.player.getSkinTextureLocation(),
                    sign * (width * 16 - 4 - (sign == 1 ? 8 : 0)), 0,
                    8,
                    drawHat,
                    upsideDown
                );
                RenderSystem.disableDepthTest();
            }

            ms.popPose();
        }

        // render tower and progress text
        {
            ms.pushPose();
            ms.translate((width - 1) * 8 - 8, height * 16 - 10, 0);
            ms.scale(32f / TOWER_SECTION_WIDTH, 32f / TOWER_SECTION_WIDTH, 32f / TOWER_SECTION_WIDTH);

            int offsetSteps = renderTower(ms, buffer, frontPackedLight, ors.sequence, ors.summary.levelingState().tierIndex(), 9, 0.5f);

            // render progress bars
            int progressHeight = 9;

            renderProgressBg(ms, buffer, frontPackedLight, progressHeight, -16, 0, 0.5f);
            renderProgressFg(ms, buffer, frontPackedLight, progressHeight, -16, 0, 0.625f,
                ors.nextTierProgress);
            renderProgressBg(ms, buffer, frontPackedLight, progressHeight, TOWER_SECTION_WIDTH, 0, 0.5f);
            renderProgressFg(ms, buffer, frontPackedLight, progressHeight, TOWER_SECTION_WIDTH, 0, 0.625f,
                ors.nextLevelProgress);

            // render badges
            ms.translate(0, 0, 0.75f);
            ms.scale(1, -1, 1);

            int[] x$ = new int[] { -16, TOWER_SECTION_WIDTH };
            int[] y$ = new int[] { (progressHeight * 16) - 4, -3 };
            BadgeState[] badge$ = new BadgeState[] { ors.nextTier, ors.nextLevel };

            for (int i = 0; i < 2; i++) {
                BadgeState[] badge$$ = new BadgeState[] { ors.current, badge$[i] };
                for (int j = 0; j < 2; j++) {
                    int x = x$[i];
                    int y = y$[j];
                    BadgeState badge = badge$$[j];
                    if (badge == null) continue;

                    font.drawInBatch(
                        ChatBadgeUtil.staticBadge(badge),
                        x - 2, y,
                        -1, false,
                        ms.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        frontPackedLight
                    );
                }
            }

            // render total points
            ms.translate(0, TOWER_SECTION_HEIGHT * offsetSteps, 0);

            int textWidth = font.width(ors.progressText);
            //noinspection IntegerDivisionInFloatingPointContext
            font.drawInBatch(
                ors.progressText,
                (TOWER_SECTION_WIDTH - textWidth) / 2, 3,
                textColor, false,
                ms.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                frontPackedLight
            );

            ms.popPose();
        }

        // render prestige state
        {
            ms.pushPose();
            ms.translate(width * 16f / 2f, 12f, 0.125f);
            ms.scale(3/8f, -3/8f, 3/8f);

            int textWidth1 = font.width(ors.prestigeText);
            //noinspection IntegerDivisionInFloatingPointContext
            font.drawInBatch(
                ors.prestigeText,
                -textWidth1 / 2, 0f,
                0xFFFFFF, true,
                ms.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                frontPackedLight
            );

            ms.scale(3/4f, 3/4f, 3/4f);
            int textWidth2 = font.width(ors.prestigeMultiplierText);
            //noinspection IntegerDivisionInFloatingPointContext
            font.drawInBatch(
                ors.prestigeMultiplierText,
                -textWidth2 / 2, 16f,
                0xFFFFFF, true,
                ms.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                frontPackedLight
            );

            ms.popPose();
        }

        ms.popPose();
    }

    @SuppressWarnings("SameParameterValue")
    private void renderProgressFg(PoseStack ms, MultiBufferSource buffer, int packedLight, int height, float x, float y, float z, float progress) {
        assert height >= 2;
        VertexConsumer vc = buffer.getBuffer(RenderType.text(PROGRESS_FG));

        int padding = 6;
        int endcapHeight = 16 - padding;

        int barHeight = height * 16 - (padding * 2);
        int filledHeight = Math.round(barHeight * progress);

        int topAmount;
        int middleAmount;
        int bottomAmount;

        if (filledHeight <= endcapHeight * 2) {
            middleAmount = 0;
            bottomAmount = filledHeight / 2;
            topAmount = filledHeight - bottomAmount;
        } else {
            topAmount = endcapHeight;
            bottomAmount = endcapHeight;
            middleAmount = filledHeight - (endcapHeight * 2);
        }

        // bottom endcap
        float y1 = y - height * 16;
        quad(ms, vc,
            x + 0, y1,
            x + 16, y1 + bottomAmount + padding,
            z,
            0, 3 / 3f,
            1, (3 - ((bottomAmount + padding) / 16f)) / 3f,
            packedLight,
            -1
        );
        y1 += bottomAmount + padding;

        // middle
        for (int i = 0; i < middleAmount / 16; i++) {
            quad(ms, vc,
                x + 0, y1,
                x + 16, y1 + 16,
                z,
                0, 2 / 3f,
                1, 1 / 3f,
                packedLight,
                -1
            );
            y1 += 16;
        }
        int remainingMiddle = middleAmount % 16;
        if (remainingMiddle > 0) {
            quad(ms, vc,
                x + 0, y1,
                x + 16, y1 + remainingMiddle,
                z,
                0, 2 / 3f,
                1, (2 - (remainingMiddle / 16f)) / 3f,
                packedLight,
                -1
            );
            y1 += remainingMiddle;
        }

        // top endcap
        quad(ms, vc,
            x + 0, y1,
            x + 16, y1 + topAmount + padding,
            z,
            0, ((topAmount + padding) / 16f) / 3f,
            1, 0 / 3f,
            packedLight,
            -1
        );
    }

    private void renderProgressBg(PoseStack ms, MultiBufferSource buffer, int packedLight, int height, float x, float y, float z) {
        assert height >= 2;
        VertexConsumer vc = buffer.getBuffer(RenderType.text(PROGRESS_BG));

        for (int y1 = 0; y1 < height; y1++) {
            int tile = (y1 == 0) ? 0 : (y1 == height - 1) ? 2 : 1;
            quad(ms, vc,
                x + 0, y + -y1 * 16 - 16,
                x + 16, y + -y1 * 16,
                z,
                0, (tile + 1) / 3f,
                1, tile / 3f,
                packedLight,
                -1
            );
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void renderBackground(PoseStack ms, MultiBufferSource buffer, int packedLight, ResourceLocation texture, int width, int height, float z) {
        VertexConsumer vc = buffer.getBuffer(RenderType.text(texture));

        // top and bottom edges
        for (int x = 0; x < width; x++) {
            bgTile(ms, vc, x, -1, z, 1, 2, packedLight);
            bgTile(ms, vc, x, height, z, 1, 0, packedLight);
        }

        // left and right edges
        for (int y = 0; y < height; y++) {
            bgTile(ms, vc, -1, y, z, 0, 1, packedLight);
            bgTile(ms, vc, width, y, z, 2, 1, packedLight);
        }

        // corners
        bgTile(ms, vc, -1, -1, z, 0, 2, packedLight);
        bgTile(ms, vc, width, -1, z, 2, 2, packedLight);
        bgTile(ms, vc, -1, height, z, 0, 0, packedLight);
        bgTile(ms, vc, width, height, z, 2, 0, packedLight);

        // inner tiles
        vc = buffer.getBuffer(RenderType.text(BACKGROUND));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                quad(ms, vc,
                    x * 16, y * 16,
                    x * 16 + 16, y * 16 + 16,
                    z,
                    0, 1,
                    1, 0,
                    packedLight,
                    -1
                );
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int renderTower(PoseStack ms, MultiBufferSource buffer, int packedLight, Sequence sequence, int currentTier, int maxTiers, float z) {
        int tiers = Math.min(sequence.getTierCount(), maxTiers);

        int dimming = 180;

        //                            prevent empty tiers near top                                        and near bottom
        int highlightIndex = Math.max(tiers - (sequence.getTierCount() - currentTier), Math.min(currentTier, maxTiers / 2));
        int offset = currentTier - highlightIndex;

        int belowDivisor = Math.max((tiers + 1) / 2, highlightIndex + 2);
        int aboveDivisor = Math.max((tiers + 1) / 2, tiers - highlightIndex + 1);

        for (int i = -1; i <= tiers; i++) {
            float progress = i < highlightIndex
                ? ((float) (highlightIndex - i + 1) / belowDivisor)
                : ((float) (i - highlightIndex + 1) / aboveDivisor);
            int v = (i == highlightIndex) ? 255 : 255 - (int) (progress * dimming);
            int color = (i == highlightIndex) ? -1 : FastColor.ARGB32.color(255, v, v, v);

            int tierIndex = i + offset;
            ResourceLocation texture;
            if (i == -1) {
                texture = TOWER_BOTTOM;
                if (highlightIndex == 0) {
                    color = -1;
                }
            } else if (i == tiers) {
                texture = TOWER_TOP;
                if (highlightIndex == tiers - 1) {
                    color = -1;
                }
            } else {
                texture = tierIndex >= sequence.getTierCount()
                    ? TOWER_EMPTY
                    : sequence.getTier(tierIndex).getTexture("tower");
            }

            blit(
                ms, buffer,
                packedLight, color,
                0, -(tiers - i) * TOWER_SECTION_HEIGHT, z,
                TOWER_SECTION_WIDTH, TOWER_SECTION_HEIGHT,
                texture,
                0, 0,
                1, 1
            );
        }

        return tiers - highlightIndex - 1;
    }

    @SuppressWarnings("SameParameterValue")
    private void blit(
        PoseStack ms,
        MultiBufferSource buffer,
        int packedLight,
        int color,
        float x, float y,
        float z, float width, float height,
        ResourceLocation texture,
        float u0, float v0,
        float u1, float v1
    ) {
        VertexConsumer vc = buffer.getBuffer(RenderType.text(texture));
        quad(ms, vc,
            x, y,
            x + width, y + height,
            z,
            u0, v1,
            u1, v0,
            packedLight,
            color
        );
    }

    private void bgTile(PoseStack ms, VertexConsumer vc, int x, int y, float z, int tileX, int tileY, int packedLight) {
        float u0 = tileX / 3f;
        float v0 = (tileY + 1) / 3f;
        float u1 = (tileX + 1) / 3f;
        float v1 = tileY / 3f;

        quad(ms, vc,
            x * 16, y * 16,
            x * 16 + 16, y * 16 + 16,
            z,
            u0, v0,
            u1, v1,
            packedLight,
            -1
        );
    }

    private void quad(PoseStack ms, VertexConsumer vc, float x0, float y0, float x1, float y1, float z, float u0, float v0, float u1, float v1, int packedLight, int color) {
        int a = FastColor.ARGB32.alpha(color);
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);

        vc.vertex(ms.last().pose(), x0, y0, z).color(r, g, b, a).uv(u0, v0).uv2(packedLight).endVertex();
        vc.vertex(ms.last().pose(), x1, y0, z).color(r, g, b, a).uv(u1, v0).uv2(packedLight).endVertex();
        vc.vertex(ms.last().pose(), x1, y1, z).color(r, g, b, a).uv(u1, v1).uv2(packedLight).endVertex();
        vc.vertex(ms.last().pose(), x0, y1, z).color(r, g, b, a).uv(u0, v1).uv2(packedLight).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull ObeliskBlockEntity be) {
        return true;
    }
}
