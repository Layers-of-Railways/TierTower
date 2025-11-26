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

package io.github.slimeistdev.tier_tower.foundation.fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class SingleFluidTypeTank extends SingleFluidStorage {
    protected final @NotNull Fluid fluidType;
    protected final long capacity;
    protected final @Nullable Runnable onChanged;

    public SingleFluidTypeTank(@NotNull Fluid fluidType, long capacity) {
        this(fluidType, capacity, null);
    }

    public SingleFluidTypeTank(@NotNull Fluid fluidType, long capacity, @Nullable Runnable onChanged) {
        this.fluidType = fluidType;
        this.capacity = capacity;
        this.onChanged = onChanged;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return variant.isOf(fluidType);
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return capacity;
    }

    @Override
    public void readNbt(@Nullable CompoundTag nbt) {
        if (nbt != null)
            super.readNbt(nbt);

        if (nbt == null || !variant.isBlank() && !canInsert(variant)) {
            this.variant = FluidVariant.blank();
            this.amount = 0;
            setChanged();
        }
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        setChanged();
    }

    protected void setChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    public FluidVariant getNonBlankVariant() {
        return variant.isBlank() ? FluidVariant.of(fluidType) : variant;
    }
}
