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

package io.github.slimeistdev.tier_tower.content.subliminator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.registry.TierTowerFluids;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class SubliminatorScreen extends AbstractContainerScreen<SubliminatorMenu> {
    private static final ResourceLocation TEXTURE = TierTower.asResource("textures/gui/subliminator.png");
    private final FluidVariant eminenceVariant = FluidVariant.of(TierTowerFluids.WISPY_EMINENCE.getSource());

    public SubliminatorScreen(SubliminatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int fuelProgress = this.menu.getLitProgress();
            guiGraphics.blit(TEXTURE, x + 56, y + 36 + 12 - fuelProgress, 176, 12 - fuelProgress, 14, fuelProgress + 1);
        }

        int subliminationProgress = this.menu.getSubliminationProgress();
        guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 14, subliminationProgress + 1, 16);

        // render tank
        renderEminence(guiGraphics);
        guiGraphics.blit(TEXTURE, x + 114, y + 8, 2, 176, 30, 36, 68, 256, 256);
    }

    private void renderEminence(@NotNull GuiGraphics guiGraphics) {
        long amount = this.menu.getStoredFluid();
        if (amount <= 0) return;

        int storedFluidHeight = (int) (amount * 64 / SubliminatorBlockEntity.MAX_STORED_FLUID);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(eminenceVariant);
        if (sprite == null) return;

        int color = FluidVariantRendering.getColor(eminenceVariant);
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        blitRepeating(
            guiGraphics,
            this.leftPos + 116, this.topPos + 10,
            1,
            32, storedFluidHeight,
            16, 16,
            sprite,
            r, g, b
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitRepeating(
        @NotNull GuiGraphics graphics,
        int x, int y,
        int blitOffset,
        int width, int height,
        int sourceHeight, int sourceWidth,
        @NotNull TextureAtlasSprite sprite,
        float red,
        float green,
        float blue
    ) {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = graphics.pose().last().pose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        int ix = x;
        IntIterator xSlices = slices(width, sourceWidth);

        while (xSlices.hasNext()) {
            int sliceWidth = xSlices.nextInt();
            int iy = y;
            IntIterator ySlices = slices(height, sourceHeight);

            while (ySlices.hasNext()) {
                int sliceHeight = ySlices.nextInt();
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(16f * sliceWidth / (float) sourceWidth);
                float v1 = sprite.getV(16f * sliceHeight / (float) sourceHeight);
                quad(
                    bufferBuilder,
                    matrix4f,
                    ix, iy,
                    ix + sliceWidth, iy + sliceHeight,
                    blitOffset,
                    u0, v0,
                    u1, v1,
                    red, green, blue
                );
                iy += sliceHeight;
            }

            ix += sliceWidth;
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static void quad(BufferBuilder bufferBuilder, Matrix4f model, float x0, float y0, float x1, float y1, float z, float u0, float v0, float u1, float v1, float r, float g, float b) {
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).uv(u0, v1).endVertex();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).uv(u1, v1).endVertex();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).uv(u1, v0).endVertex();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).uv(u0, v0).endVertex();
    }

    /**
     * Returns an iterator for dividing a value into slices of a specified size.
     * <p>
     * @return An iterator for iterating over the slices.
     *
     * @param target the value to be divided.
     * @param total the size of each slice.
     */
    private static IntIterator slices(int target, int total) {
        int i = Mth.positiveCeilDiv(target, total);
        return new Divisor(target, i);
    }
}
