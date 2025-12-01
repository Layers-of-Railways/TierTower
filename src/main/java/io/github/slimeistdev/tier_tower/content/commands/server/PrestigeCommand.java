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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.utils.FluidFormatter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PrestigeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("prestige")
            .requires(cs -> cs.hasPermission(2))
            .then(literal("query")
                .then(argument("target", EntityArgument.player())
                    .executes(ctx -> $query(
                        ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target")
                    ))
                )
            )
            .then(literal("set")
                .then(argument("target", EntityArgument.player())
                    .then(argument("points", IntegerArgumentType.integer(0))
                        .executes(ctx -> $set(
                            ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "target"),
                            IntegerArgumentType.getInteger(ctx, "points")
                        ))
                    )
                )
            );
    }

    private static int $query(CommandSourceStack source, ServerPlayer target) {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        int points = tower.getPrestigePoints();
        double multiplier = tower.getPrestigeMultiplier();

        source.sendSuccess(() -> Component.translatable(
            "commands.tier_tower.prestige.query.success",
            target.getDisplayName(),
            FluidFormatter.NUMBER_FORMAT.get().format(points),
            multiplier
        ), true);

        return points;
    }

    private static int $set(CommandSourceStack source, ServerPlayer target, int points) {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        tower.setPrestigePoints(points);

        source.sendSuccess(() -> Component.translatable(
            "commands.tier_tower.prestige.set.success",
            target.getDisplayName(),
            FluidFormatter.NUMBER_FORMAT.get().format(points),
            tower.getPrestigeMultiplier()
        ), true);

        return points;
    }
}
