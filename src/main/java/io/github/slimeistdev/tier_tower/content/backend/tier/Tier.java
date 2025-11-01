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

package io.github.slimeistdev.tier_tower.content.backend.tier;

import com.mojang.datafixers.util.Pair;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.utils.SearchUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public final class Tier {
    private final Holder<TierPackData> definition;
    private final ResourceLocation id;
    private final int levelCount;
    private final int[] levelingCosts;
    /** How many points, within this tier, are needed to get to a level */
    private final int[] levelBaseCosts;
    private final int totalCost;

    public Tier(Holder<TierPackData> definition, int[] levelingCosts) {
        assert definition.value().levelCount() == levelingCosts.length :
            "Level count mismatch between definition and provided leveling costs";

        this.definition = definition;
        this.id = definition.unwrapKey().orElseThrow().location();
        this.levelCount = levelingCosts.length;
        this.levelingCosts = levelingCosts;

        this.levelBaseCosts = new int[levelCount];

        for (int i = 1; i < levelCount; i++) {
            levelBaseCosts[i] = levelBaseCosts[i - 1] + levelingCosts[i - 1];
        }
        this.totalCost = levelBaseCosts[levelCount - 1] + levelingCosts[levelCount - 1];
    }

    /**
     * Create a Tier from its definition and a base leveling cost.
     * @param definition the tier's definition
     * @param baseLevelingCost the previous tier's next leveling cost, or the sequence's base for the first tier
     * @return (the constructed tier, the base leveling cost for the next tier)
     */
    public static Pair<Tier, Integer> constructFrom(final Holder<TierPackData> definition, final int baseLevelingCost) {
        final TierPackData data = definition.value();
        final int levelCount = data.levelCount();
        final Node costFunction = data.levelingCostFunction();

        final int levelingCost = data.baseLevelingCost().orElse(baseLevelingCost);
        int[] costs = new int[levelCount];
        costs[0] = levelingCost;

        double cost = levelingCost;
        EvaluationContext ctx = new EvaluationContext()
            .setFinal("levels", costs.length)
            .setFinal("base", levelingCost)
            .set("prev", cost)
            .set("level", 0);

        // each iteration computes the cost for the NEXT level
        for (int level = 0; level < levelCount; level++) {
            ctx.set("level", level + 1);

            try {
                cost = costFunction.evaluate(ctx);
            } catch (EvaluationException e) {
                TierTower.LOGGER.error("Failed to evaluate leveling cost for level {} of tier {}. It will inherit the cost of level {}", level + 1, definition.unwrapKey().orElseThrow().location(), level, e);
            }
            ctx.set("prev", cost);

            // the loop computes one more cost than is needed for this tier, to provide the next tier with a base cost
            if (level + 1 < costs.length) {
                costs[level + 1] = (int) Math.max(1, Math.round(cost));
            }
        }

        int nextBase = (int) Math.max(1, Math.round(cost));
        return Pair.of(new Tier(definition, costs), nextBase);
    }

    public Holder<TierPackData> getDefinition() {
        return definition;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return id.toLanguageKey("tier_tower.tier");
    }

    public int getLevelCount() {
        return levelCount;
    }

    public int getLevelingCost(int level) {
        if (level < 0 || level >= levelCount) {
            throw new IndexOutOfBoundsException("Level must be between 0 and " + (levelCount - 1));
        }
        return levelingCosts[level];
    }

    public int getCostUpTo(int level) {
        if (level < 0 || level >= levelCount) {
            throw new IndexOutOfBoundsException("Level must be between 0 and " + (levelCount - 1));
        }

        return levelBaseCosts[level];
    }

    public int getLevel(int pointsWithinTier) {
        if (pointsWithinTier < 0 || pointsWithinTier >= totalCost) {
            throw new IndexOutOfBoundsException("Points within tier must be between 0 and " + (totalCost - 1));
        }

        // we know that this will never return -1, because lbc[0] == 0, and pwt >= 0
        return SearchUtils.binarySearchLE(levelBaseCosts, pointsWithinTier);
    }

    public int getTotalLevelingCost() {
        return totalCost;
    }

    /*public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeVarInt(levelCount);
        for (int cost : levelingCosts) {
            buf.writeInt(cost);
        }
    }

    public static Tier read(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        int levelCount = buf.readVarInt();
        int[] levelingCosts = new int[levelCount];
        for (int i = 0; i < levelCount; i++) {
            levelingCosts[i] = buf.readInt();
        }
        return new Tier(id, levelingCosts);
    }*/
}
