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
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeUtil;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatType.class)
public class MixinChatType {
    @WrapOperation(
        method = "bind(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/ChatType$Bound;",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/network/chat/ChatType;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/ChatType$Bound;"
        )
    )
    private ChatType.Bound addBadge(ChatType chatType, Component name, Operation<ChatType.Bound> original) {
        return original.call(chatType, ChatBadgeUtil.decorateAutomatically(name.copy()));
    }
}
