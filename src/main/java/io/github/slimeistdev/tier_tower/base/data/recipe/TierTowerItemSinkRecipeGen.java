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

    GeneratedRecipe
        START = null,

        DIAMOND = create("diamond", b -> b
            .require(I.diamond())
            .points(5)),

        NETHER_STAR = create("nether_star", b -> b
            .require(I.netherStar())
            .points(10)),

        TOTEM_OF_UNDYING = create("totem_of_undying", b -> b
            .require(I.totemOfUndying())
            .points(20)),

        ECHO_SHARD = create("echo_shard", b -> b
            .require(I.echoShard())
            .points(25)),

        HEART_OF_THE_SEA = create("heart_of_the_sea", b -> b
            .require(I.heartOfTheSea())
            .points(50)),

        DRAGON_HEAD = create("dragon_head", b -> b
            .require(I.dragonHead())
            .points(100)),

        ELYTRA = create("elytra", b -> b
            .require(I.elytra())
            .points(100)),

        END = null;

    @Override
    public @NotNull String getName() {
        return "Item Sink Recipes";
    }
}
