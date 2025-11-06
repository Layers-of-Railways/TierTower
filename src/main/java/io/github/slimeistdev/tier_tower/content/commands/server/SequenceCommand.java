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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SequenceCommand {
    private static final Dynamic2CommandExceptionType ERROR_SET_INVALID_SEQUENCE = new Dynamic2CommandExceptionType((target, id) ->
        Component.translatable("commands.tier_tower.sequence.set.invalid_sequence", target, id)
    );

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return literal("sequence")
            .requires(cs -> cs.hasPermission(2))
            .then(set(context))
            .then(query());
    }

    // region set

    private static ArgumentBuilder<CommandSourceStack, ?> set(CommandBuildContext context) {
        return literal("set")
            .then(argument("target", EntityArgument.player())
                .then(argument("sequence", ResourceArgument.resource(context, TierTowerRegistries.SEQUENCE))
                    .executes(ctx -> $set(
                        ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target"),
                        ResourceArgument.getResource(ctx, "sequence", TierTowerRegistries.SEQUENCE)
                    ))
                ));
    }

    private static int $set(CommandSourceStack source, ServerPlayer target, Holder.Reference<Sequence> sequence) throws CommandSyntaxException {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        if (tower.trySwitchToSequence(sequence.key())) {
            source.sendSuccess(() -> Component.translatable(
                "commands.tier_tower.sequence.set.success",
                target.getDisplayName(),
                sequence.key().location()
            ), true);
            return 1;
        } else {
            throw ERROR_SET_INVALID_SEQUENCE.create(target.getDisplayName(), sequence.key().location());
        }
    }

    // endregion set

    // region query

    private static ArgumentBuilder<CommandSourceStack, ?> query() {
        return literal("query")
            .then(argument("target", EntityArgument.player())
                .executes(ctx -> $query(
                    ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "target")
                )));
    }

    private static int $query(CommandSourceStack source, ServerPlayer target) {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        TowerSummary summary = tower.summarize();

        source.sendSuccess(() -> Component.translatable(
            "commands.tier_tower.sequence.query.success",
            target.getDisplayName(),
            summary.sequenceId().location()
        ), false);

        return 1;
    }

    // endregion query
}
