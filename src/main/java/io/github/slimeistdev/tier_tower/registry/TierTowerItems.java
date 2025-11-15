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

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.eminent_items.BottledEminenceItem;
import io.github.slimeistdev.tier_tower.content.eminent_items.EminenceNuggetItem;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

@SuppressWarnings("unused")
public class TierTowerItems {
    private static final TierTowerRegistrate REGISTRATE = TierTower.registrate();

    public static final ItemEntry<BottledEminenceItem> BOTTLED_EMINENCE = REGISTRATE.item("bottled_eminence", BottledEminenceItem::new)
        .lang("Stoppered Wispy Eminence")
        .properties(p -> p.craftRemainder(Items.GLASS_BOTTLE))
        .tag(TierTowerTags.AllItemTags.UPRIGHT_ON_BELT.tag)
        .register();

    public static final ItemEntry<EminenceNuggetItem> EMINENCE_NUGGET = REGISTRATE.item("eminence_nugget", EminenceNuggetItem::new)
        .lang("Nugget of Eminence")
        .tag(Tags.Items.NUGGETS)
        .properties(p -> p.rarity(Rarity.UNCOMMON))
        .register();

    public static void register() {
        TierTower.LOGGER.info("Registering items for " + TierTower.NAME);
    }
}
