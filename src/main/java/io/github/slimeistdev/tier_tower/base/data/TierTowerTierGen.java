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

package io.github.slimeistdev.tier_tower.base.data;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.base.data.api.TierGen;
import io.github.slimeistdev.tier_tower.content.backend.tier.SequenceSavedData;
import io.github.slimeistdev.tier_tower.content.backend.tier.TierSavedData;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@SuppressWarnings({"SameParameterValue", "unused"})
public class TierTowerTierGen extends TierGen {
    private static final HashMap<String, String> LANG = new HashMap<>();
    private static final List<GenEntry<TierSavedData>> TIERS = new ArrayList<>();
    private static final List<GenEntry<SequenceSavedData>> SEQUENCES = new ArrayList<>();

    public TierTowerTierGen(PackOutput output) {
        super(output);
    }

    static GenEntry<TierSavedData> START = null,

    QUARTZ = tier("quartz", b -> b
        .additiveLevelingCost(1)),

    IRON = tier("iron", b -> b
        .additiveLevelingCost(2)),

    COPPER = tier("copper", b -> b
        .levelInterpolatedMultiplicativeCost(2)
        .baseLevelingCost(1000)
        .levelCount(20)),

    END = null;

    static GenEntry<SequenceSavedData> MAIN_SEQUENCE = sequence("main", b -> b
        .defaultBaseLevelingCost(1)
        .tier(QUARTZ)
        .tier(IRON)
        .tier(COPPER));

    public static void provideLang(BiConsumer<String, String> langConsumer) {
        LANG.forEach(langConsumer);
    }

    private static GenEntry<TierSavedData> tier(ResourceLocation id, String langName, UnaryOperator<TierBuilder> builder) {
        LANG.put(id.toLanguageKey("tier_tower.tier"), langName);

        var entry = new GenEntry<>(
            id,
            builder.apply(new TierBuilder()).build()
        );
        TIERS.add(entry);
        return entry;
    }

    private static GenEntry<TierSavedData> tier(String id, String langName, UnaryOperator<TierBuilder> builder) {
        return tier(TierTower.asResource(id), langName, builder);
    }

    private static GenEntry<TierSavedData> tier(ResourceLocation id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id.getPath()), builder);
    }

    private static GenEntry<TierSavedData> tier(String id, UnaryOperator<TierBuilder> builder) {
        return tier(id, RegistrateLangProvider.toEnglishName(id), builder);
    }

    @Override
    protected void registerTiers(Consumer<GenEntry<TierSavedData>> provider) {
        TIERS.forEach(provider);
    }

    private static GenEntry<SequenceSavedData> sequence(ResourceLocation id, UnaryOperator<SequenceBuilder> builder) {
        var entry = new GenEntry<>(
            id,
            builder.apply(new SequenceBuilder()).build()
        );
        SEQUENCES.add(entry);
        return entry;
    }

    private static GenEntry<SequenceSavedData> sequence(String id, UnaryOperator<SequenceBuilder> builder) {
        return sequence(TierTower.asResource(id), builder);
    }

    @Override
    protected void registerSequences(Consumer<GenEntry<SequenceSavedData>> provider) {
        SEQUENCES.forEach(provider);
    }

    @Override
    public @NotNull String getName() {
        return "Tier Tower Tiers";
    }
}
