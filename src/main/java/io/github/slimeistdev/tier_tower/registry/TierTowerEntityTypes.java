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

import com.tterrag.registrate.util.entry.EntityEntry;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.eminent_items.ThrownBottledEminence;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;

@SuppressWarnings("unused")
public class TierTowerEntityTypes {
    private static final TierTowerRegistrate REGISTRATE = TierTower.registrate();

    public static final EntityEntry<ThrownBottledEminence> BOTTLED_EMINENCE = REGISTRATE.<ThrownBottledEminence>entity(
        "bottled_eminence",
            ThrownBottledEminence::new,
            MobCategory.MISC
        )
        .lang("Thrown Stoppered Wispy Eminence")
        .properties(b -> b
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)))
        .renderer(() -> ThrownItemRenderer::new)
        .register();

    public static void register() {
        TierTower.LOGGER.info("Registering entity types for " + TierTower.NAME);
    }
}
