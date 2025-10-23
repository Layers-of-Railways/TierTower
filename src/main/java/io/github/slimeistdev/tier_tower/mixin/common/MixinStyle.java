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
import io.github.slimeistdev.tier_tower.mixin_ducks.common.Style_Duck;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.Objects;
import java.util.UUID;

@Mixin(Style.class)
public abstract class MixinStyle implements Style_Duck {
    @Shadow public abstract Style withBold(@Nullable Boolean bold);

    @Shadow @Final
    @Nullable Boolean bold;

    @Unique
    private @Nullable UUID tt$badgePlayer = null;

    @Override
    public Style tt$withBadge(UUID player) {
        return ((MixinStyle) (Object) withBold(bold)).tt$setBadge(player);
    }

    @Unique
    private Style tt$setBadge(UUID player) {
        tt$badgePlayer = player;
        return (Style)(Object)this;
    }

    @Override
    public @Nullable UUID tt$getBadgePlayer() {
        return tt$badgePlayer;
    }

    @WrapOperation(method = "applyTo", at = @At(value = "NEW", target = "(Lnet/minecraft/network/chat/TextColor;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Lnet/minecraft/network/chat/ClickEvent;Lnet/minecraft/network/chat/HoverEvent;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/chat/Style;"))
    private Style preserveBadgePlayer(TextColor color, Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough, Boolean obfuscated, ClickEvent clickEvent, HoverEvent hoverEvent, String insertion, ResourceLocation font, Operation<Style> original) {
        Style out = original.call(color, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion, font);
        ((MixinStyle) (Object) out).tt$setBadge(tt$badgePlayer);
        return out;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @WrapOperation(method = "toString", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style$1Collector;addValueString(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 4))
    private void appendBadgePlayer(@Coerce Object instance, String s, Object o, Operation<Void> original) {
        original.call(instance, s, o);
        original.call(instance, "tt$badgePlayer", tt$badgePlayer);
    }

    @WrapOperation(method = "equals", at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 0))
    private boolean compareBadgePlayer(Object a, Object b, Operation<Boolean> original, Object other) {
        if (!Objects.equals(tt$badgePlayer, ((MixinStyle) other).tt$badgePlayer)) {
            return false;
        }
        return original.call(a, b);
    }

    @WrapOperation(method = "hashCode", at = @At(value = "INVOKE", target = "Ljava/util/Objects;hash([Ljava/lang/Object;)I"))
    private int hashBadgePlayer(Object[] values, Operation<Integer> original) {
        Object[] extended = new Object[values.length + 1];
        System.arraycopy(values, 0, extended, 0, values.length);
        extended[values.length] = tt$badgePlayer;
        return original.call(new Object[]{extended}); // yay java varargs
    }
}
