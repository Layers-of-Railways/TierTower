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

import com.tterrag.registrate.util.entry.BlockEntry;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.item_sink.ItemSinkBlock;
import io.github.slimeistdev.tier_tower.content.obelisk.ObeliskBlock;
import io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlock;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;
import io.github.slimeistdev.tier_tower.registry.TierTowerTags.AllBlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;

import static io.github.slimeistdev.tier_tower.base.data.BuilderTransformers.*;

@SuppressWarnings("unused")
public class TierTowerBlocks {
    private static final TierTowerRegistrate REGISTRATE = TierTower.registrate();

    public static final BlockEntry<RotatedPillarBlock> DEEPSLATE_PILLAR = REGISTRATE.block("deepslate_pillar", RotatedPillarBlock::new)
        .initialProperties(() -> Blocks.POLISHED_DEEPSLATE)
        .transform(pickaxeOnly())
        .transform(pillarState())
        .simpleItem()
        .register();

    public static final BlockEntry<RotatedPillarBlock> EMINENT_DEEPSLATE_PILLAR = REGISTRATE.block("eminent_deepslate_pillar", RotatedPillarBlock::new)
        .initialProperties(() -> Blocks.POLISHED_DEEPSLATE)
        .transform(pickaxeOnly())
        .transform(eminentBlock())
        .transform(pillarState())
        .simpleItem()
        .register();

    public static final BlockEntry<ObeliskBlock> OBELISK = REGISTRATE.block("obelisk", ObeliskBlock::new)
        .initialProperties(() -> Blocks.POLISHED_DEEPSLATE)
        .transform(pickaxeOnly())
        .transform(eminentBlock())
        .blockstate((c, p) -> p.horizontalBlock(
            c.get(),
            p.modLoc("block/eminent_deepslate_pillar_side"),
            p.modLoc("block/obelisk_front"),
            p.modLoc("block/eminent_deepslate_pillar_top")
        ))
        .simpleItem()
        .register();

    public static final BlockEntry<Block> EMINENT_DEEPSLATE_BRICKS = REGISTRATE.block("eminent_deepslate_bricks", Block::new)
        .initialProperties(() -> Blocks.DEEPSLATE_BRICKS)
        .transform(pickaxeOnly())
        .transform(eminentBlock())
        .simpleItem()
        .register();

    public static final BlockEntry<Block> EMINENT_DEEPSLATE_TILES = REGISTRATE.block("eminent_deepslate_tiles", Block::new)
        .initialProperties(() -> Blocks.DEEPSLATE_TILES)
        .transform(pickaxeOnly())
        .transform(eminentBlock())
        .simpleItem()
        .register();

    public static final BlockEntry<Block> EMINENT_GLASS = REGISTRATE.block("eminent_glass", Block::new)
        .initialProperties(() -> Blocks.SEA_LANTERN)
        .transform(lightLevel(10))
        .simpleItem()
        .register();

    public static final BlockEntry<Block> EMINENT_GLASS_BRICKS = REGISTRATE.block("eminent_glass_bricks", Block::new)
        .initialProperties(() -> Blocks.SEA_LANTERN)
        .transform(lightLevel(10))
        .simpleItem()
        .register();

    public static final BlockEntry<ItemSinkBlock> ITEM_SINK = REGISTRATE.block("item_sink", ItemSinkBlock::new)
        .lang("Vortex")
        .initialProperties(() -> Blocks.DEEPSLATE_BRICKS)
        .transform(pickaxeOnly())
        .transform(lightLevel(15))
        .tag(AllBlockTags.RELOCATION_NOT_SUPPORTED.tag)
        .tag(AllBlockTags.NON_MOVABLE.tag)
        .blockstate((c, p) -> p.simpleBlock(
            c.get(),
            p.models().cubeBottomTop(
                c.getName(),
                p.modLoc("block/item_sink_side"),
                p.mcLoc("block/deepslate_bricks"),
                p.modLoc("block/item_sink_top")
            )
        ))
        .simpleItem()
        .register();

    public static final BlockEntry<SubliminatorBlock> SUBLIMINATOR = REGISTRATE.block("subliminator", SubliminatorBlock::new)
        .lang("Subliminator")
        .initialProperties(() -> Blocks.DEEPSLATE_BRICKS)
        .transform(pickaxeOnly())
        .transform(lightLevel(SubliminatorBlock::getLightLevel))
        .blockstate((c, p) -> p.getVariantBuilder(c.get())
            .forAllStatesExcept(state -> {
                boolean lit = state.getValue(SubliminatorBlock.LIT);
                return ConfiguredModel.builder()
                    .modelFile(p.models().cubeBottomTop(
                        c.getName() + (lit ? "_lit" : ""),
                        p.modLoc("block/subliminator_side" + (lit ? "_on": "")),
                        p.modLoc("block/subliminator_bottom"),
                        p.modLoc("block/subliminator_top" + (lit ? "_on": "_off"))
                    )).build();
            }, SubliminatorBlock.FACING)
        )
        .simpleItem()
        .register();

    public static void register() {
        TierTower.LOGGER.info("Registering blocks for " + TierTower.NAME);
    }
}
