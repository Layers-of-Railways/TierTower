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
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TierCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("tier")
            .requires(cs -> cs.hasPermission(2))
            //.then(set())
            .then(query());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> set() {
        return literal("set")
            /*.then(argument("target", EntityArgument.player())
                .then(argument("tier", null) // TODO tier argument
                    .executes(ctx -> $set(
                        ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target"),
                        (ResourceLocation) null, // TODO
                        null,
                        null
                    ))
                    // TODO level and points arguments
                ))*/;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> query() {
        return literal("query")
            .then(argument("target", EntityArgument.player())
                .executes(ctx -> $query(
                    ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "target"),
                    Aspect.TIER
                ))
                .then(query$aspect(Aspect.TIER))
                .then(query$aspect(Aspect.LEVEL))
                .then(query$aspect(Aspect.POINTS))
                .then(query$aspect(Aspect.TOTAL_POINTS))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> query$aspect(Aspect aspect) {
        return literal(aspect.name)
            .executes(ctx -> $query(
                ctx.getSource(),
                EntityArgument.getPlayer(ctx, "target"),
                aspect
            ));
    }

    private static int $query(CommandSourceStack source, ServerPlayer target, Aspect aspect) {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        TowerSummary summary = tower.summarize();
        Sequence.LevelingState levelingState = summary.levelingState();

        return switch (aspect) {
            case TIER -> {
                Sequence sequence = tower.getSequence();
                String tierKey = sequence.getTier(levelingState.tierIndex()).getTranslationKey();

                source.sendSuccess(() -> Component.translatable(
                    "commands.tier_tower.tier.query.tier",
                    target.getDisplayName(),
                    Component.translatable(tierKey)
                ), false);

                yield levelingState.tierIndex();
            }
            case LEVEL -> {
                source.sendSuccess(() -> Component.translatable(
                    "commands.tier_tower.tier.query.level",
                    target.getDisplayName(),
                    levelingState.levelIndex() + 1
                ), false);
                yield levelingState.levelIndex();
            }
            case POINTS -> {
                source.sendSuccess(() -> Component.translatable(
                    "commands.tier_tower.tier.query.points",
                    target.getDisplayName(),
                    levelingState.levelPoints() + levelingState.surplusPoints()
                ), false);
                yield levelingState.levelPoints() + levelingState.surplusPoints();
            }
            case TOTAL_POINTS -> {
                source.sendSuccess(() -> Component.translatable(
                    "commands.tier_tower.tier.query.total_points",
                    target.getDisplayName(),
                    tower.totalPoints()
                ), false);
                yield tower.totalPoints();
            }
        };
    }

    private enum Aspect {
        TIER("tier"),
        LEVEL("level"),
        POINTS("points"),
        TOTAL_POINTS("total_points");

        public final String name;
        Aspect(String name) {
            this.name = name;
        }
    }
}
