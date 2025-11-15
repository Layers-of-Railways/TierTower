package io.github.slimeistdev.tier_tower.foundation.fluids;

import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class VirtualFluid extends SimpleFlowableFluid {
    public static VirtualFluid createSource(Properties properties) {
        return new VirtualFluid(properties, true);
    }

    public static VirtualFluid createFlowing(Properties properties) {
        return new VirtualFluid(properties, false);
    }


    private final boolean source;

    public VirtualFluid(Properties properties, boolean source) {
        super(properties);
        this.source = source;
    }

    @Override
    public @NotNull Fluid getSource() {
        if (source) {
            return this;
        }
        return super.getSource();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        if (source) {
            return super.getFlowing();
        }
        return this;
    }

    @Override
    protected @NotNull BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(@NotNull FluidState state) {
        return source;
    }

    @Override
    public int getAmount(@NotNull FluidState state) {
        return 0;
    }
}
