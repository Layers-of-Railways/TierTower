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

package io.github.slimeistdev.tier_tower.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.slimeistdev.tier_tower.content.commands.server.ReloadCommandsCommand;
import io.github.slimeistdev.tier_tower.utils.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Collections;

import static net.minecraft.commands.Commands.literal;

public class TierTowerCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        var towerCommand = literal("tier_tower");

        if (Utils.isDevEnv()) {
            towerCommand = towerCommand
                .then(ReloadCommandsCommand.register(dispatcher, context, selection, TierTowerCommands::register));
        }

        LiteralCommandNode<CommandSourceStack> towerRoot = dispatcher.register(towerCommand);

        CommandNode<CommandSourceStack> twr = dispatcher.findNode(Collections.singleton("twr"));
        if (twr != null)
            return;

        dispatcher.getRoot().addChild(buildRedirect("twr", towerRoot));
    }

    /**
     * Copied from Create's AllCommands, with generics weakened.
     * Original source:
     * <a href="https://github.com/VelocityPowered/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38">https://github.com/VelocityPowered/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38</a>
     *
     * <p>
     * Returns a literal node that redirects its execution to
     * the given destination node.
     *
     * @param alias       the command alias
     * @param destination the destination node
     * @return the built node
     */
    public static <T> LiteralCommandNode<T> buildRedirect(final String alias, final LiteralCommandNode<T> destination) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
        LiteralArgumentBuilder<T> builder = LiteralArgumentBuilder
            .<T>literal(alias)
            .requires(destination.getRequirement())
            .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
            .executes(destination.getCommand());
        for (CommandNode<T> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder.build();
    }
}
