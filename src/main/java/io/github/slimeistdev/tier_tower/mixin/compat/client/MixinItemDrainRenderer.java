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

package io.github.slimeistdev.tier_tower.mixin.compat.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.drain.ItemDrainRenderer;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.slimeistdev.tier_tower.annotation.mixin.ConditionalMixin;
import io.github.slimeistdev.tier_tower.compat.Mods;
import io.github.slimeistdev.tier_tower.content.eminent_items.BottledEminenceItem;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@SuppressWarnings("UnstableApiUsage")
@ConditionalMixin(mods = Mods.CREATE)
@Mixin(ItemDrainRenderer.class)
public class MixinItemDrainRenderer {
    @ModifyConstant(
        method = "renderItem",
        constant = @Constant(floatValue = 360)
    )
    private float spinnyEminence(float constant, ItemDrainBlockEntity be) {
        ItemStack stack = be.getHeldItemStack();
        if (stack.getItem() instanceof BottledEminenceItem)
            return 360*2;

        return constant;
    }

    // this injector is both broken and unnecessary in Create 6, but absolutely necessary in Create 0.5
    @SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature", "RedundantSuppression"})
    @WrapOperation(
        method = "renderFluid",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/fluid/FluidRenderer;renderFluidBox(Lio/github/fabricators_of_create/porting_lib/fluids/FluidStack;FFFFFFLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IZ)V",
            ordinal = 0
        ),
        require = 0
    )
    private void fixEminenceRender(
        FluidStack fluidStack,
        float xMin, float yMin, float zMin,
        float xMax, float yMax, float zMax,
        MultiBufferSource buffer, PoseStack ms,
        int light, boolean renderBottom,
        Operation<Void> original
    ) {
        if (FluidVariantAttributes.isLighterThanAir(fluidStack.getType())) {
            renderBottom = true;
        }

        original.call(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, renderBottom);
    }
}
