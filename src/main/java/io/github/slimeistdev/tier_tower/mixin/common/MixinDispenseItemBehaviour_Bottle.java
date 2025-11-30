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

package io.github.slimeistdev.tier_tower.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlockEntities;
import io.github.slimeistdev.tier_tower.registry.TierTowerItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

// Glass Bottle
@Mixin(targets = "net.minecraft.core.dispenser.DispenseItemBehavior$24")
public abstract class MixinDispenseItemBehaviour_Bottle extends OptionalDispenseItemBehavior {
    @Shadow protected abstract ItemStack takeLiquid(BlockSource source, ItemStack empty, ItemStack filled);

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/OptionalDispenseItemBehavior;execute(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack pickupEminence(
        @Coerce OptionalDispenseItemBehavior instance,
        BlockSource source,
        ItemStack stack,
        Operation<ItemStack> original
    ) {
        BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        var sbe$ = TierTowerBlockEntities.SUBLIMINATOR.get(source.getLevel(), pos);
        if (sbe$.isPresent() && sbe$.get().removeBottleAmount()) {
            setSuccess(true);
            return takeLiquid(source, stack, TierTowerItems.BOTTLED_EMINENCE.asStack());
        }

        return original.call(instance, source, stack);
    }
}
