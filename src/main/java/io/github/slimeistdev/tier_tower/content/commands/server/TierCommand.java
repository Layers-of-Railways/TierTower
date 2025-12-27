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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.backend.PlayerTower;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import io.github.slimeistdev.tier_tower.content.backend.tier.TowerSummary;
import io.github.slimeistdev.tier_tower.content.cosmetics.BadgeState;
import io.github.slimeistdev.tier_tower.content.cosmetics.ChatBadgeUtil;
import io.github.slimeistdev.tier_tower.registry.TierTowerRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TierCommand {
    private static final Dynamic2CommandExceptionType ERROR_SET_INVALID_TIER = new Dynamic2CommandExceptionType((tier, sequence) ->
        Component.translatable("commands.tier_tower.tier.set.invalid_tier", tier, sequence)
    );
    private static final DynamicCommandExceptionType ERROR_SET_INVALID_LEVEL = new DynamicCommandExceptionType(
        maxLevel -> Component.translatable("commands.tier_tower.tier.set.invalid_level", maxLevel)
    );
    private static final SimpleCommandExceptionType ERROR_SET_INVALID_LEVEL_NEGATIVE = new SimpleCommandExceptionType(
        Component.translatable("commands.tier_tower.tier.set.invalid_level.negative")
    );
    private static final SimpleCommandExceptionType ERROR_SET_INVALID_POINTS = new SimpleCommandExceptionType(
        Component.translatable("commands.tier_tower.tier.set.invalid_points")
    );
    private static final SimpleCommandExceptionType ERROR_SET_INVALID_POINTS_NEGATIVE = new SimpleCommandExceptionType(
        Component.translatable("commands.tier_tower.tier.set.invalid_points.negative")
    );

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return literal("tier")
            .requires(cs -> cs.hasPermission(2))
            .then(set(context))
            .then(modify(ModifyMode.ADD))
            .then(modify(ModifyMode.REMOVE))
            .then(query());
    }

    // region set

    private static ArgumentBuilder<CommandSourceStack, ?> set(CommandBuildContext context) {
        return literal("set")
            .then(argument("target", EntityArgument.player())
                .then(argument("tier", ResourceArgument.resource(context, TierTowerRegistries.TIER))
                    .executes(ctx -> $set(
                        ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target"),
                        ResourceArgument.getResource(ctx, "tier", TierTowerRegistries.TIER)
                    ))
                    .then(argument("level", IntegerArgumentType.integer(0))
                        .executes(ctx -> $set(
                            ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "target"),
                            ResourceArgument.getResource(ctx, "tier", TierTowerRegistries.TIER),
                            IntegerArgumentType.getInteger(ctx, "level")
                        ))
                        .then(argument("points", IntegerArgumentType.integer(0))
                            .executes(ctx -> $set(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "target"),
                                ResourceArgument.getResource(ctx, "tier", TierTowerRegistries.TIER),
                                IntegerArgumentType.getInteger(ctx, "level"),
                                IntegerArgumentType.getInteger(ctx, "points")
                            ))
                        )
                    )
                ));
    }

    private static int $set(CommandSourceStack source, ServerPlayer target, Holder.Reference<TierPackData> tier) throws CommandSyntaxException {
        return $set(source, target, tier, 0, 0);
    }

    private static int $set(CommandSourceStack source, ServerPlayer target, Holder.Reference<TierPackData> tier, int level) throws CommandSyntaxException {
        return $set(source, target, tier, level, 0);
    }

    private static int $set(CommandSourceStack source, ServerPlayer target, Holder.Reference<TierPackData> tier, int level, int points) throws CommandSyntaxException {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        Sequence sequence = tower.getSequence();
        TowerSummary summary = tower.summarize();

        int tierIndex = sequence.indexOf(tier.key().location());
        if (tierIndex == -1) {
            throw ERROR_SET_INVALID_TIER.create(tier.key().location(), summary.sequenceId().location());
        }

        int levelCount = tier.value().levelCount();
        if (level >= levelCount) {
            throw ERROR_SET_INVALID_LEVEL.create(levelCount - 1);
        }

        TowerSummary newSummary = tower.setTierLevelAndPoints(tierIndex, level, points);
        if (newSummary == null) {
            throw ERROR_SET_INVALID_POINTS.create();
        } else {
            source.sendSuccess(() -> Component.translatable(
                "commands.tier_tower.tier.set.success",
                target.getDisplayName(),
                ChatBadgeUtil.staticBadge(new BadgeState(
                    sequence.getTier(tierIndex).getId(),
                    newSummary.levelingState().levelIndex()
                )),
                newSummary.levelingState().levelPoints() + newSummary.levelingState().surplusPoints()
            ), true);
        }

        return 1;
    }

    // endregion set

    // region modify

    private static ArgumentBuilder<CommandSourceStack, ?> modify(ModifyMode mode) {
        return literal(mode.name)
            .then(argument("target", EntityArgument.player())
                .then(argument("amount", IntegerArgumentType.integer(0))
                    .executes(ctx -> $modify(
                        ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "target"),
                        IntegerArgumentType.getInteger(ctx, "amount"),
                        ModifyAspect.POINTS,
                        mode
                    ))
                    .then(literal("points")
                        .executes(ctx -> $modify(
                            ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "target"),
                            IntegerArgumentType.getInteger(ctx, "amount"),
                            ModifyAspect.POINTS,
                            mode
                        ))
                    )
                    .then(literal("levels")
                        .executes(ctx -> $modify(
                            ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "target"),
                            IntegerArgumentType.getInteger(ctx, "amount"),
                            ModifyAspect.LEVELS,
                            mode
                        ))
                    )
                ));
    }

    private static int $modify(CommandSourceStack source, ServerPlayer target, int amount, ModifyAspect aspect, ModifyMode mode) throws CommandSyntaxException {
        PlayerTower tower = TierTower.CITY.getOrCreateTower(target);
        mode.apply(aspect, tower, amount, target);

        source.sendSuccess(() -> Component.translatable(
            "commands.tier_tower.tier."+mode.name+"." + aspect.name + ".success",
            amount,
            target.getDisplayName()
        ), true);

        return 0;
    }

    private enum ModifyMode {
        ADD("add", m -> m.add),
        REMOVE("remove", m -> m.remove);

        public final String name;
        public final Function<ModifyAspect, ModifyAspect.Modifier> modifier;

        ModifyMode(String name, Function<ModifyAspect, ModifyAspect.Modifier> modifier) {
            this.name = name;
            this.modifier = modifier;
        }

        public void apply(ModifyAspect aspect, PlayerTower tower, int amount, ServerPlayer player) throws CommandSyntaxException {
            modifier.apply(aspect).apply(tower, amount, player);
        }
    }

    private enum ModifyAspect {
        POINTS("points", PlayerTower::addPoints, (SimpleModifier) (tower, amount) -> {
            if (amount <= 0) return;

            if (!tower.removePoints(amount)) {
                throw ERROR_SET_INVALID_POINTS_NEGATIVE.create();
            }
        }),
        LEVELS("levels", (SimpleModifier) (tower, amount) -> {
            if (amount <= 0) return;

            Sequence sequence = tower.getSequence();
            TowerSummary summary = tower.summarize();
            Sequence.LevelingState levelingState = summary.levelingState();

            int targetLevel = levelingState.levelIndex() + amount;
            int levelCount = sequence.getTier(levelingState.tierIndex()).getLevelCount();
            if (targetLevel >= levelCount) {
                throw ERROR_SET_INVALID_LEVEL.create(levelCount - 1);
            }

            tower.setTierLevelAndPoints(levelingState.tierIndex(), targetLevel, 0);
        }, (SimpleModifier) (tower, amount) -> {
            if (amount <= 0) return;

            TowerSummary summary = tower.summarize();
            Sequence.LevelingState levelingState = summary.levelingState();

            int targetLevel = levelingState.levelIndex() - amount;
            if (targetLevel < 0) {
                throw ERROR_SET_INVALID_LEVEL_NEGATIVE.create();
            }

            tower.setTierLevelAndPoints(levelingState.tierIndex(), targetLevel, 0);
        });

        public final String name;
        public final Modifier add;
        public final Modifier remove;

        ModifyAspect(String name, Modifier add, Modifier remove) {
            this.name = name;
            this.add = add;
            this.remove = remove;
        }

        public interface Modifier {
            void apply(PlayerTower tower, int amount, ServerPlayer player) throws CommandSyntaxException;
        }

        public interface SimpleModifier extends Modifier {
            void apply(PlayerTower tower, int amount) throws CommandSyntaxException;

            @Override
            default void apply(PlayerTower tower, int amount, ServerPlayer player) throws CommandSyntaxException {
                apply(tower, amount);
            }
        }
    }

    // endregion modify

    // region query

    private static ArgumentBuilder<CommandSourceStack, ?> query() {
        return literal("query")
            .then(argument("target", EntityArgument.player())
                .executes(ctx -> $query(
                    ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "target"),
                    QueryAspect.TIER
                ))
                .then(query$aspect(QueryAspect.TIER))
                .then(query$aspect(QueryAspect.LEVEL))
                .then(query$aspect(QueryAspect.POINTS))
                .then(query$aspect(QueryAspect.TOTAL_POINTS))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> query$aspect(QueryAspect aspect) {
        return literal(aspect.name)
            .executes(ctx -> $query(
                ctx.getSource(),
                EntityArgument.getPlayer(ctx, "target"),
                aspect
            ));
    }

    private static int $query(CommandSourceStack source, ServerPlayer target, QueryAspect aspect) {
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

    private enum QueryAspect {
        TIER("tier"),
        LEVEL("level"),
        POINTS("points"),
        TOTAL_POINTS("total_points");

        public final String name;
        QueryAspect(String name) {
            this.name = name;
        }
    }

    // endregion query
}
