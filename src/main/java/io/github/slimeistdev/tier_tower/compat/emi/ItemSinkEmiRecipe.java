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

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.content.item_sink.recipe.ItemSinkRecipe;
import io.github.slimeistdev.tier_tower.content.subliminator.SubliminatorBlockEntity;
import io.github.slimeistdev.tier_tower.registry.TierTowerFluids;
import io.github.slimeistdev.tier_tower.utils.EminenceConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemSinkEmiRecipe extends BasicEmiRecipe {
    private static final EmiTexture FULL_FLAME = new EmiTexture(TierTower.asResource("textures/gui/subliminator.png"), 176, 0, 14, 14);
    private final int points;

    public ItemSinkEmiRecipe(ItemSinkRecipe recipe) {
        super(TierTowerEmiPlugin.CATEGORY_ITEM_SINK, recipe.id(), 82, 38);
        this.points = recipe.points();
        this.inputs.add(EmiIngredient.of(recipe.ingredient()));
        this.outputs.add(EmiStack.of(TierTowerFluids.WISPY_EMINENCE.get(), (long) recipe.points() * EminenceConstants.FLUID_PER_EMINENCE));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int cookingTime = points * SubliminatorBlockEntity.COOKING_TIME_MULTIPLIER;
        widgets.addFillingArrow(24, 5, 50 * cookingTime)
            .tooltip((mx, my) -> List.of(
                ClientTooltipComponent.create(Component.translatable("emi.cooking.time", cookingTime / 20f)
                    .getVisualOrderText())
            ));

        widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24);
        widgets.addAnimatedTexture(FULL_FLAME, 1, 24, 4000, false, true, true);

        widgets.addText(Component.translatable("emi.recipe.tier_tower.item_sink.points", points), 26, 28, -1, true);

        widgets.addSlot(this.inputs.get(0), 0, 4);
        widgets.add(new TankWidget(this.outputs.get(0), 56, 0, 26, 26, FluidConstants.BLOCK))
            .large(true)
            .recipeContext(this);
    }
}
