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

package io.github.slimeistdev.tier_tower.base.data.recipe;

import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import io.github.slimeistdev.tier_tower.TierTower;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TierTowerItemSinkRecipeGen extends TierTowerRecipeProvider {
    public TierTowerItemSinkRecipeGen(PackOutput output) {
        super(output);
    }

    protected GeneratedRecipe create(String name, NonNullUnaryOperator<ItemSinkRecipeBuilder> transform) {
        return register(c ->
            transform.apply(new ItemSinkRecipeBuilder(TierTower.asResource("item_sink/"+name))).build(c));
    }

    GeneratedRecipe DIAMOND = create("diamond", b -> b
        .require(I.diamond())
        .points(5));

    @Override
    public @NotNull String getName() {
        return "Item Sink Recipes";
    }
}
