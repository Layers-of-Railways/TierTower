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

package io.github.slimeistdev.tier_tower.content.eminent_items;

import io.github.slimeistdev.tier_tower.content.item_sink.EminentSnake;
import io.github.slimeistdev.tier_tower.content.item_sink.EminentSnakeParticleOptions;
import io.github.slimeistdev.tier_tower.registry.TierTowerEntityTypes;
import io.github.slimeistdev.tier_tower.registry.TierTowerItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ThrownBottledEminence extends ThrowableItemProjectile {
    public ThrownBottledEminence(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownBottledEminence(Level level, LivingEntity shooter) {
        super(TierTowerEntityTypes.BOTTLED_EMINENCE.get(), shooter, level);
    }

    public ThrownBottledEminence(Level level, double x, double y, double z) {
        super(TierTowerEntityTypes.BOTTLED_EMINENCE.get(), x, y, z, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return TierTowerItems.BOTTLED_EMINENCE.get();
    }

    @Override
    protected float getGravity() {
        return 0.07f;
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            int colorIdx = level.random.nextInt(EminentSnake.COLORS.length);
            int color = 0xFF000000 | EminentSnake.COLORS[colorIdx];
            level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), color);

            Vector3f vectorColor = new Vector3f(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
            );

            int value = 3 + level.random.nextInt(5) + this.level().random.nextInt(5);
            Vec3 pos = position();
            level.sendParticles(new EminentSnakeParticleOptions(vectorColor, 2), pos.x, pos.y, pos.z, value * 2, 0.25, 0.25, 0.25, 0.5);
            level.sendParticles(new DustParticleOptions(vectorColor, 1.5f), pos.x, pos.y, pos.z, value, 0.25, 0.25, 0.25, 0.5);

            this.discard();
        }
    }
}
