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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.slimeistdev.tier_tower.content.commands.client.ReloadCommandsCommand;
import io.github.slimeistdev.tier_tower.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import java.util.Collections;

import static io.github.slimeistdev.tier_tower.registry.TierTowerCommands.buildRedirect;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class TierTowerCommandsClient {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        var towerCommand = literal("tier_towerc");

        if (Utils.isDevEnv()) {
            towerCommand = towerCommand
                .then(ReloadCommandsCommand.register(dispatcher, context, TierTowerCommandsClient::register));
        }

        LiteralCommandNode<FabricClientCommandSource> towerRoot = dispatcher.register(towerCommand);

        CommandNode<FabricClientCommandSource> twrc = dispatcher.findNode(Collections.singleton("twrc"));
        if (twrc != null)
            return;

        dispatcher.getRoot().addChild(buildRedirect("twrc", towerRoot));
    }
}
