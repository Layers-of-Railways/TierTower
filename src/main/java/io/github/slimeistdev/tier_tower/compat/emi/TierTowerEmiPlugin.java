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

package io.github.slimeistdev.tier_tower.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlocks;
import io.github.slimeistdev.tier_tower.registry.TierTowerRecipeTypes;

public class TierTowerEmiPlugin implements EmiPlugin {
    public static final EmiStack ITEM_SINK = EmiStack.of(TierTowerBlocks.ITEM_SINK);
    public static final EmiStack SUBLIMINATOR = EmiStack.of(TierTowerBlocks.SUBLIMINATOR);
    public static final EmiRecipeCategory CATEGORY_ITEM_SINK = new EmiRecipeCategory(
        TierTower.asResource("item_sink"),
        ITEM_SINK
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY_ITEM_SINK);
        registry.addWorkstation(CATEGORY_ITEM_SINK, ITEM_SINK);
        registry.addWorkstation(CATEGORY_ITEM_SINK, SUBLIMINATOR);

        for (ItemSinkRecipe recipe : registry.getRecipeManager().getAllRecipesFor(TierTowerRecipeTypes.ITEM_SINK))
            registry.addRecipe(new ItemSinkEmiRecipe(recipe));
    }
}
