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
import io.github.slimeistdev.tier_tower.foundation.data.SimpleGenEntry;
import io.github.slimeistdev.tier_tower.foundation.data.TierBuilder;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierPackData;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
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
    private static final List<SimpleGenEntry<TierPackData>> TIERS = new ArrayList<>();

    static SimpleGenEntry<TierPackData> START = null,

    COPPER = tier("copper", b -> b
        .baseLevelingCost(16)
        .multOffsetBoostCost(1, 1, 2)),

    ZINC = tier(new ResourceLocation("create", "zinc"), b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(2, 1, 5)),

    IRON = tier("iron", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(2, 2, 6)),

    GOLD = tier("gold", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(3, 3, 7)),

    QUARTZ = tier("quartz", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(3, 5, 9)),

    AMETHYST = tier("amethyst", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(3, 8, 10)),

    REDSTONE = tier("redstone", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(4, 13, 12)),

    BRASS = tier(new ResourceLocation("create", "brass"), b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(4, 21, 16)),

    ROSE_QUARTZ = tier(new ResourceLocation("create", "rose_quartz"), b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(4, 34, 18)),

    PRISMARINE = tier("prismarine", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(4, 55, 21)),

    DIAMOND = tier("diamond", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(5, 89, 24)),

    EMERALD = tier("emerald", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(5, 144, 25)),

    ECHO = tier("echo", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(5, 233, 27)),

    FLUIX = tier(new ResourceLocation("ae2", "fluix"), b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(5, 377, 29)),

    NETHERITE = tier("netherite", b -> b
        .factoredBaseLevelingCost(0.75)
        .multOffsetBoostCost(5, 610, 32)
        .erode(1, 20)),

    END = null;

    public static void provideLang(BiConsumer<String, String> langConsumer) {
        LANG.forEach(langConsumer);
    }

    private static SimpleGenEntry<TierPackData> tier(ResourceLocation id, String langName, UnaryOperator<TierBuilder> builder) {
        ResourceKey<TierPackData> key = ResourceKey.create(TierTowerRegistries.TIER, id);
        LANG.put(id.toLanguageKey("tier_tower.tier"), langName);

        var entry = new SimpleGenEntry<TierPackData>(
            key,
            () -> builder.apply(new TierBuilder()).build()
        );
        TIERS.add(entry);
        return entry;
    }

    private static SimpleGenEntry<TierPackData> tier(String id, String langName, UnaryOperator<TierBuilder> builder) {
        return tier(new ResourceLocation(id), langName, builder);
    }

    private static SimpleGenEntry<TierPackData> tier(ResourceLocation id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id.getPath()), builder);
    }

    private static SimpleGenEntry<TierPackData> tier(String id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id), builder);
    }

    public static void init() {}

    public static void bootstrap(BootstapContext<TierPackData> bootstapContext) {
        for (var entry : TIERS) {
            bootstapContext.register(entry.getKey(), entry.get());
        }
    }

    public static void bootstrap(FabricDynamicRegistryProvider.Entries entries) {
        for (var entry : TIERS) {
            entries.add(entry.getKey(), entry.get());
        }
    }
}
