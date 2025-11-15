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

import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;
import io.github.slimeistdev.tier_tower.foundation.fluids.VirtualFluid;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class TierTowerFluids {
    private static final TierTowerRegistrate REGISTRATE = TierTower.registrate();

    public static final FluidEntry<VirtualFluid> WISPY_EMINENCE = REGISTRATE.virtualFluid("eminence")
        .lang("Wispy Eminence")
        .fluidAttributes(() -> new TierTowerAttributeHandler("fluid.tier_tower.eminence", FluidConstants.WATER_VISCOSITY, -100))
        .bucket()
        .lang("Bucket of Wispy Eminence")
        .build()
        .onRegisterAfter(Registries.ITEM, eminence -> {
            Fluid still = eminence.getSource();
            FluidStorage.combinedItemApiProvider(TierTowerItems.BOTTLED_EMINENCE.get())
                .register(ctx -> new FullItemFluidStorage(
                    ctx,
                    bottle -> ItemVariant.of(Items.GLASS_BOTTLE),
                    FluidVariant.of(still),
                    FluidConstants.BOTTLE
                ));
            FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE)
                .register(ctx -> new EmptyItemFluidStorage(
                    ctx,
                    bottle -> ItemVariant.of(TierTowerItems.BOTTLED_EMINENCE.get()),
                    still,
                    FluidConstants.BOTTLE
                ));

            FluidStorage.combinedItemApiProvider(still.getBucket())
                .register(ctx -> new FullItemFluidStorage(
                    ctx,
                    bottle -> ItemVariant.of(Items.BUCKET),
                    FluidVariant.of(still),
                    FluidConstants.BUCKET
                ));
            FluidStorage.combinedItemApiProvider(Items.BUCKET)
                .register(ctx -> new EmptyItemFluidStorage(
                    ctx,
                    bottle -> ItemVariant.of(still.getBucket()),
                    still,
                    FluidConstants.BUCKET
                ));
        })
        .register();

    public static void register() {
        TierTower.LOGGER.info("Registering fluids for " + TierTower.NAME);
    }

    private record TierTowerAttributeHandler(Component name, int viscosity, boolean lighterThanAir) implements FluidVariantAttributeHandler {
        private TierTowerAttributeHandler(String key, int viscosity, int density) {
            this(Component.translatable(key), viscosity, density <= 0);
        }

        public TierTowerAttributeHandler(String key) {
            this(key, FluidConstants.WATER_VISCOSITY, 1000);
        }

        @Override
        public Component getName(FluidVariant fluidVariant) {
            return name.copy();
        }

        @Override
        public int getViscosity(FluidVariant variant, @Nullable Level world) {
            return viscosity;
        }

        @Override
        public boolean isLighterThanAir(FluidVariant variant) {
            return lighterThanAir;
        }
    }
}
