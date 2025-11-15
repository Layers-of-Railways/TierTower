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

package io.github.slimeistdev.tier_tower.foundation;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import io.github.slimeistdev.tier_tower.foundation.data.VirtualFluidBuilder;
import io.github.slimeistdev.tier_tower.foundation.fluids.VirtualFluid;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class TierTowerRegistrate extends AbstractRegistrate<TierTowerRegistrate> {
    private static final Map<RegistryEntry<?>, ResourceKey<CreativeModeTab>> TAB_LOOKUP = Collections.synchronizedMap(new IdentityHashMap<>());

    @Nullable
    protected ResourceKey<CreativeModeTab> currentTab;

    protected TierTowerRegistrate(String modid) {
        super(modid);
    }

    public static TierTowerRegistrate create(String modid) {
        return new TierTowerRegistrate(modid);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TierTowerRegistrate setCreativeTab(ResourceKey<CreativeModeTab> tab) {
        this.currentTab = tab;
        return this;
    }

    @Override
    protected <R, T extends R> @NotNull RegistryEntry<T> accept(
        String name,
        ResourceKey<? extends Registry<R>> type,
        Builder<R, T, ?, ?> builder,
        NonNullSupplier<? extends T> creator,
        NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory
    ) {
        RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);
        if (currentTab != null)
            TAB_LOOKUP.put(entry, currentTab);

        return entry;
    }

    public FluidBuilder<VirtualFluid, TierTowerRegistrate> virtualFluid(String name) {
        return entry(name,
            c -> new VirtualFluidBuilder<>(self(), self(), name, c, new ResourceLocation(getModid(), "fluid/" + name + "_still"),
                new ResourceLocation(getModid(), "fluid/" + name + "_flow"), /*null, */VirtualFluid::createSource, VirtualFluid::createFlowing));
    }
}
