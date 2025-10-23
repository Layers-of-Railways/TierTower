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

import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(TranslatableContents.class)
public class MixinTranslatableContents {
    @Shadow @Final private String key;

    @Shadow @Final private Object[] args;

    @Inject(
        method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T> void visitBadge(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style, CallbackInfoReturnable<Optional<T>> cir) {
        if ("tier_tower.special.badge".equals(key) && args.length == 1 && args[0] instanceof String uuid$) {
            try {
                ChatBadgeContents badgeContents = new ChatBadgeContents(UUID.fromString(uuid$));
                cir.setReturnValue(badgeContents.visit(styledContentConsumer, style));
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
