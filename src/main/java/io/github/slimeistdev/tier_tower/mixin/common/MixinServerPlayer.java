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

import com.mojang.authlib.GameProfile;
import io.github.slimeistdev.tier_tower.content.item_sink.ItemSinkBlock;
import io.github.slimeistdev.tier_tower.mixin_ducks.common.ServerPlayer_Duck;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ServerPlayer_Duck {
    @Unique
    private @Nullable BlockPos tt$lastSafePos;

    private MixinServerPlayer(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public @Nullable BlockPos tt$getLastSafePos() {
        return tt$lastSafePos;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void storeSafePos(CallbackInfo ci) {
        if (!onGround()) return;
        if (getBlockStateOn().getBlock() instanceof ItemSinkBlock) return;
        tt$lastSafePos = blockPosition();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readLastSafePos(CompoundTag compound, CallbackInfo ci) {
        tt$lastSafePos = null;
        if (compound.contains("TierTowerLastSafePos", CompoundTag.TAG_LONG)) {
            tt$lastSafePos = BlockPos.of(compound.getLong("TierTowerLastSafePos"));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeLastSafePos(CompoundTag compound, CallbackInfo ci) {
        if (tt$lastSafePos != null) {
            compound.putLong("TierTowerLastSafePos", tt$lastSafePos.asLong());
        }
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void clearSafePos(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        tt$lastSafePos = null;
    }
}
