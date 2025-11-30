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

package io.github.slimeistdev.tier_tower.content.eminent_items;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.utils.EminenceConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EminenceNuggetItem extends Item {
    public EminenceNuggetItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS,
                .5f, 1);
            return InteractionResultHolder.consume(stack);
        }

        int amountUsed = player.isShiftKeyDown() ? 1 : stack.getCount();

        TierTower.CITY.getOrCreateTower(player).addPoints(amountUsed * EminenceConstants.EMINENCE_PER_BOTTLE, serverPlayer);

        stack.shrink(amountUsed);
        if (!stack.isEmpty())
            return InteractionResultHolder.success(stack);

        player.setItemInHand(usedHand, ItemStack.EMPTY);
        return InteractionResultHolder.consume(stack);
    }
}
