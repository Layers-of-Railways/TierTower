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

package io.github.slimeistdev.tier_tower.content.commands.server;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class LockCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(boolean lock) {
        return literal(lock ? "lock" : "unlock")
            .requires(cs -> cs.hasPermission(2))
            .then(argument("targets", EntityArgument.players())
                .executes(ctx -> $lock(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    lock
                ))
            );
    }

    private static int $lock(CommandSourceStack source, Collection<ServerPlayer> targets, boolean lock) {
        ServerPlayer singleSuccess = null;
        int modifiedCount = 0;
        for (ServerPlayer target : targets) {
            PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
            if (tower.isLocked() != lock) {
                tower.setLocked(lock);
                modifiedCount++;
                singleSuccess = target;
            }
        }

        String cmd = lock ? "lock" : "unlock";
        if (modifiedCount == 0) {
            if (targets.size() == 1) {
                Component displayName = targets.iterator().next().getDisplayName();
                source.sendFailure(Component.translatable(
                    "commands.tier_tower." + cmd + ".no_change.single",
                    displayName
                ));
            } else {
                source.sendFailure(Component.translatable("commands.tier_tower." + cmd + ".no_change.multiple"));
            }
        } else if (modifiedCount == 1) {
            Component displayName = singleSuccess.getDisplayName();
            source.sendSuccess(() -> Component.translatable(
                "commands.tier_tower." + cmd + ".success.single",
                displayName
            ), true);
        } else {
            final int finalModifiedCount = modifiedCount;
            source.sendSuccess(() -> Component.translatable(
                "commands.tier_tower." + cmd + ".success.multiple",
                finalModifiedCount
            ), true);
        }

        return modifiedCount;
    }
}
