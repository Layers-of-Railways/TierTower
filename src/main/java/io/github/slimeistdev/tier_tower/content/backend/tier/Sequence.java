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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Sequence {
    private final ResourceLocation id;
    private final Tier[] tiers;
    private final Object2IntMap<ResourceLocation> idMap;
    private final @Nullable ResourceLocation nextSequence;

    public Sequence(ResourceLocation id, Tier[] tiers, @Nullable ResourceLocation nextSequence) {
        this.id = id;
        this.tiers = tiers;
        this.idMap = new Object2IntOpenHashMap<>();
        this.nextSequence = nextSequence;

        for (int i = 0; i < this.tiers.length; i++) {
            Tier tier = this.tiers[i];

            if (idMap.containsKey(tier.getId())) {
                throw new IllegalArgumentException("Duplicate tier ID: " + tier.getId());
            }
            idMap.put(tier.getId(), i);
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public @Nullable Tier getTier(ResourceLocation id) {
        int index = idMap.getOrDefault(id, -1);
        return index >= 0 ? tiers[index] : null;
    }

    public @Nullable Tier getNextTier(Tier currentTier) {
        int index = idMap.getOrDefault(currentTier.getId(), -1);
        if (index < 0 || index + 1 >= tiers.length) {
            return null; // No next tier available
        }
        return tiers[index + 1];
    }

    public @Nullable ResourceLocation getNextSequence() {
        return nextSequence;
    }
}
