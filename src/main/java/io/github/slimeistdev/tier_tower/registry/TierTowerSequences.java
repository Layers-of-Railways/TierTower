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
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.BootstapLookup;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.GenEntry;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.HolderLookupWrapper;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen.SequenceBuilder;
import io.github.slimeistdev.tier_tower.content.backend.tier.Sequence;
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

import static io.github.slimeistdev.tier_tower.registry.TierTowerTiers.*;

@SuppressWarnings({"SameParameterValue", "unused"})
public class TierTowerSequences {
    private static final HashMap<String, String> LANG = new HashMap<>();
    private static final List<GenEntry<Sequence>> SEQUENCES = new ArrayList<>();

    static GenEntry<Sequence> MAIN_SEQUENCE = sequence(TierTower.MAIN_SEQUENCE.location(), b -> b
        .defaultBaseLevelingCost(1)
        .tiers(
            COPPER,
            ZINC,
            IRON,
            GOLD,
            QUARTZ,
            AMETHYST,
            REDSTONE,
            BRASS,
            ROSE_QUARTZ,
            PRISMARINE,
            DIAMOND,
            EMERALD,
            ECHO,
            FLUIX,
            NETHERITE
        ));

    public static void provideLang(BiConsumer<String, String> langConsumer) {
        LANG.forEach(langConsumer);
    }

    private static GenEntry<Sequence> sequence(ResourceLocation id, String langName, UnaryOperator<SequenceBuilder> builder) {
        ResourceKey<Sequence> key = ResourceKey.create(TierTowerRegistries.SEQUENCE, id);
        LANG.put(id.toLanguageKey("tier_tower.sequence"), langName);

        var entry = new GenEntry<Sequence>(
            key,
            lookup -> builder.apply(new SequenceBuilder()).build(lookup)
        );
        SEQUENCES.add(entry);
        return entry;
    }

    private static GenEntry<Sequence> sequence(String id, String langName, UnaryOperator<SequenceBuilder> builder) {
        return sequence(TierTower.asResource(id), langName, builder);
    }

    private static GenEntry<Sequence> sequence(ResourceLocation id, UnaryOperator<SequenceBuilder> builder) {
        return sequence(id, RegistrateLangProvider.toEnglishName(id.getPath()), builder);
    }

    private static GenEntry<Sequence> sequence(String id, UnaryOperator<SequenceBuilder> builder) {
        return sequence(id, RegistrateLangProvider.toEnglishName(id), builder);
    }

    public static void init() {}

    public static void bootstrap(BootstapContext<Sequence> bootstapContext) {
        var lookup = new BootstapLookup<>(bootstapContext);

        for (var entry : SEQUENCES) {
            bootstapContext.register(entry.getKey(), entry.apply(lookup));
        }
    }

    public static void bootstrap(FabricDynamicRegistryProvider.Entries entries, HolderLookup.Provider registries) {
        var lookup = new HolderLookupWrapper(registries);

        for (var entry : SEQUENCES) {
            entries.add(entry.getKey(), entry.apply(lookup));
        }
    }
}
