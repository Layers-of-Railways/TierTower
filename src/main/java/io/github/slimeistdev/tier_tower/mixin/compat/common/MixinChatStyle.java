/*
 * Tier Tower
 * Copyright (c) 2026 The Tier Tower Team
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

package io.github.slimeistdev.tier_tower.mixin.compat.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.styledchat.config.ChatStyle;
import io.github.slimeistdev.tier_tower.annotation.mixin.ConditionalMixin;
import io.github.slimeistdev.tier_tower.compat.Mods;
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ConditionalMixin(mods = Mods.STYLED_CHAT)
@Mixin(ChatStyle.class)
public class MixinChatStyle {
    @WrapOperation(
        method = "getChat",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component decoratePlayerDisplayName(ServerPlayer instance, Operation<Component> original) {
        return ChatBadgeUtil.decorateAutomatically(original.call(instance).copy());
    }

    @WrapOperation(
        method = {
            "getSayCommand",
            "getMeCommand"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/CommandSourceStack;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component decorateCommandSourceDisplayName(CommandSourceStack instance, Operation<Component> original) {
        return ChatBadgeUtil.decorateAutomatically(original.call(instance).copy());
    }

    @SuppressWarnings("LocalMayUseName")
    @Inject(method = "getPrivateMessageReceived", at = @At("HEAD"))
    private void decoratePrivateMessageSender(Component sender, Component receiver, Component message,
                                              @Coerce Object context, CallbackInfoReturnable<Component> cir,
                                              @Local(ordinal = 0, argsOnly = true) LocalRef<Component> senderRef) {
        senderRef.set(ChatBadgeUtil.decorateAutomatically(senderRef.get().copy()));
    }

    @SuppressWarnings("LocalMayUseName")
    @Inject(
        method = {
            "getTeamChatSent",
            "getTeamChatReceived"
        },
        at = @At("HEAD")
    )
    private void decorateTeamMessage(Component team, Component displayName, Component message,
                                     CommandSourceStack context, CallbackInfoReturnable<Component> cir,
                                     @Local(ordinal = 1, argsOnly = true) LocalRef<Component> displayNameRef) {
        displayNameRef.set(ChatBadgeUtil.decorateAutomatically(displayNameRef.get().copy()));
    }
}
