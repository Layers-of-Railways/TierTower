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

import com.tterrag.registrate.providers.RegistrateLangProvider;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.BootstapLookup;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.HolderLookupWrapper;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.TierBuilder;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

@SuppressWarnings({"SameParameterValue", "unused"})
public class TierTowerTiers {
    private static final HashMap<String, String> LANG = new HashMap<>();
    private static final List<TierGen.GenEntry<TierPackData>> TIERS = new ArrayList<>();

    static TierGen.GenEntry<TierPackData> START = null,

    QUARTZ = tier("quartz", b -> b
        .additiveLevelingCost(1)),

    IRON = tier("iron", b -> b
        .additiveLevelingCost(2)),

    COPPER = tier("copper", b -> b
        .levelInterpolatedMultiplicativeCost(2)
        .baseLevelingCost(1000)
        .levelCount(20)),

    END = null;

    public static void provideLang(BiConsumer<String, String> langConsumer) {
        LANG.forEach(langConsumer);
    }

    private static TierGen.GenEntry<TierPackData> tier(ResourceLocation id, String langName, UnaryOperator<TierBuilder> builder) {
        ResourceKey<TierPackData> key = ResourceKey.create(TierTowerRegistries.TIER, id);
        LANG.put(id.toLanguageKey("tier_tower.tier"), langName);

        var entry = new TierGen.GenEntry<TierPackData>(
            key,
            () -> builder.apply(new TierBuilder()).build()
        );
        TIERS.add(entry);
        return entry;
    }

    private static TierGen.GenEntry<TierPackData> tier(String id, String langName, UnaryOperator<TierBuilder> builder) {
        return tier(new ResourceLocation(id), langName, builder);
    }

    private static TierGen.GenEntry<TierPackData> tier(ResourceLocation id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id.getPath()), builder);
    }

    private static TierGen.GenEntry<TierPackData> tier(String id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id), builder);
    }

    public static void init() {}

    public static void bootstrap(BootstapContext<TierPackData> bootstapContext) {
        var lookup = new BootstapLookup<>(bootstapContext);

        for (var entry : TIERS) {
            bootstapContext.register(entry.getKey(), entry.apply(lookup));
        }
    }

    public static void bootstrap(FabricDynamicRegistryProvider.Entries entries, HolderLookup.Provider registries) {
        var lookup = new HolderLookupWrapper(registries);

        for (var entry : TIERS) {
            entries.add(entry.getKey(), entry.apply(lookup));
        }
    }
}
