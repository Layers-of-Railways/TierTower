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

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.data.BuilderTransformers;
import io.github.slimeistdev.tier_tower.content.item_sink.ItemSinkBlockEntity;
import io.github.slimeistdev.tier_tower.content.obelisk.ObeliskBlockEntity;
import io.github.slimeistdev.tier_tower.content.obelisk.ObeliskBlockEntityRenderer;
import io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;

@SuppressWarnings("unused")
public class TierTowerBlockEntities {
    private static final TierTowerRegistrate REGISTRATE = TierTower.registrate();

    public static final BlockEntityEntry<ItemSinkBlockEntity> ITEM_SINK = REGISTRATE.blockEntity("item_sink", ItemSinkBlockEntity::new)
        .validBlocks(TierTowerBlocks.ITEM_SINK)
        .register();

    public static final BlockEntityEntry<SubliminatorBlockEntity> SUBLIMINATOR = REGISTRATE.blockEntity("subliminator", SubliminatorBlockEntity::new)
        .validBlocks(TierTowerBlocks.SUBLIMINATOR)
        .transform(BuilderTransformers.itemStorage())
        .transform(BuilderTransformers.fluidStorage())
        .register();

    public static final BlockEntityEntry<ObeliskBlockEntity> OBELISK = REGISTRATE.blockEntity("obelisk", ObeliskBlockEntity::new)
        .renderer(() -> ObeliskBlockEntityRenderer::new)
        .validBlocks(TierTowerBlocks.OBELISK)
        .register();

    public static void register() {
        TierTower.LOGGER.info("Registering block entities for " + TierTower.NAME);
    }
}
