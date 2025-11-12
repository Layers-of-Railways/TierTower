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

package io.github.slimeistdev.tier_tower.content.item_sink;

import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.registry.TierTowerSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

public class EminentSnake {
    public static final int DEBOUNCE = 10;
    private static final boolean DEBUG_PATHFINDING = Boolean.getBoolean("tier_tower.debug.eminent_snake.pathfinding");
    private static final int[] COLORS = {
        0x7a000a,
        0xa8002c,
        0xc10033,
        0xdb0054,
        0xf2005c,
        0xff2374
    };

    final @NotNull UUID ownerId;
    int points;

    Vec3 pos;
    Vec3 vel;
    @Nullable Vec3 intermediateTarget;

    PathfindingState pathfindingState = PathfindingState.RETURN_TO_SINK;
    int age = 0;

    @Nullable ServerPlayer owner;

    public EminentSnake(@NotNull UUID ownerId, int points) {
        this.points = points;
        this.ownerId = ownerId;
    }

    private void updateOwner(@NotNull ServerLevel level) {
        if (owner != null && owner.isAlive() && owner.level() == level) {
            return;
        }
        owner = level.getPlayerByUUID(ownerId) instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    private @NotNull Vec3 ensureIntermediateTarget(@NotNull Vec3 center, double radius, @NotNull RandomSource random) {
        while (intermediateTarget == null) {
            double u = random.nextDouble();
            double v = random.nextDouble();
            double theta = u * 2.0 * Math.PI;
            double phi = Math.acos(2.0 * (1.0 - v * v * v) - 1.0);
            double sinPhi = Math.sin(phi);
            double x = radius * sinPhi * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * sinPhi * Math.sin(theta);
            intermediateTarget = center.add(x, y, z);
        }

        return intermediateTarget;
    }

    private Vector3f getColor() {
        int idx = (age / 10) % COLORS.length;
        int nextIdx = (idx + 1) % COLORS.length;
        float t = (age % 10) / 10.0f;

        int c1 = COLORS[idx];
        int c2 = COLORS[nextIdx];
        float r = ((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t;
        float g = ((c1 >> 8) & 0xFF) * (1 - t) + ((c2 >> 8) & 0xFF) * t;
        float b = (c1 & 0xFF) * (1 - t) + (c2 & 0xFF) * t;
        return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    /**
     * Step the snake
     * @param level the level of the sink
     * @param sinkPos the position of the sink
     * @return whether the snake is still alive
     */
    public boolean tick(@NotNull ServerLevel level, @NotNull BlockPos sinkPos) {
        if (age < 0) return false;

        updateOwner(level);
        age++;

        if (pos == null) {
            pos = sinkPos.getCenter().add(0, 0.75f, 0);
        }

        if (vel == null) {
            vel = Vec3.ZERO;
        }

        if (owner == null || age <= DEBOUNCE || owner.isSpectator() || owner.getEyePosition().distanceToSqr(pos) > 128*128) {
            pathfindingState = PathfindingState.RETURN_TO_SINK;
        } else if (pathfindingState == PathfindingState.RETURN_TO_SINK) {
            pathfindingState = PathfindingState.SEEK_FAR;
            intermediateTarget = null;
        }

        Vec3 target = switch (pathfindingState) {
            case RETURN_TO_SINK -> sinkPos.getCenter().add(0, 0.75f, 0);
            case SEEK_FAR -> ensureIntermediateTarget(owner.getEyePosition(), 8.0, owner.getRandom());
            case SEEK_CLOSE -> ensureIntermediateTarget(owner.getEyePosition(), 3.0, owner.getRandom());
            case SEEK_TERMINAL -> owner.getEyePosition();
        };

        if (DEBUG_PATHFINDING) {
            level.sendParticles(new DustParticleOptions(new Vector3f(0, 1, 0), 1), target.x, target.y, target.z, 1, 0, 0, 0, 0);
        }

        Vec3 toTarget = target.subtract(pos);
        double distanceToTarget = toTarget.length();
        Vec3 directionToTarget = toTarget.normalize();

        Vec3 targetVel = directionToTarget.scale(0.1 + Math.min(distanceToTarget / 10.0, 0.2));
        vel = vel.add(targetVel.subtract(vel).scale(0.1));
        pos = pos.add(vel);

        if (distanceToTarget > 0.125 || pathfindingState != PathfindingState.RETURN_TO_SINK || age <= DEBOUNCE) {
            level.sendParticles(new DustParticleOptions(getColor(), 2), pos.x, pos.y, pos.z, 2, 0.125, 0.125, 0.125, 0);
        }

        if (distanceToTarget < 0.5) {
            return switch (pathfindingState) {
                case RETURN_TO_SINK -> true;
                case SEEK_FAR -> {
                    intermediateTarget = null;
                    if (level.random.nextInt(3) == 0)
                        pathfindingState = PathfindingState.SEEK_CLOSE;
                    level.playSeededSound(null, pos.x, pos.y, pos.z, TierTowerSoundEvents.EMINENT_SNAKE_AMBIENT, SoundSource.BLOCKS, 0.5f, 1.0f, level.random.nextLong());
                    yield true;
                }
                case SEEK_CLOSE -> {
                    intermediateTarget = null;
                    if (level.random.nextInt(3) != 0)
                        pathfindingState = PathfindingState.SEEK_TERMINAL;
                    level.playSeededSound(null, pos.x, pos.y, pos.z, TierTowerSoundEvents.EMINENT_SNAKE_SWOOP, SoundSource.BLOCKS, 0.5f, 1.0f, level.random.nextLong());
                    yield true;
                }
                case SEEK_TERMINAL -> {
                    TierTower.CITY.getOrCreateTower(ownerId).addPoints(points);
                    intermediateTarget = null;
                    age = -1;
                    level.playSeededSound(null, pos.x, pos.y, pos.z, TierTowerSoundEvents.EMINENT_SNAKE_STRIKE, SoundSource.BLOCKS, 0.5f, 1.0f, level.random.nextLong());
                    level.playSeededSound(null, pos.x, pos.y, pos.z, TierTowerSoundEvents.EMINENCE_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f, level.random.nextLong());
                    yield false;
                }
            };
        }

        return true;
    }

    public static @NotNull EminentSnake load(@NotNull CompoundTag nbt, @NotNull BlockPos sinkPos) {
        int points = nbt.getInt("Points");
        UUID ownerId = nbt.getUUID("Owner");
        EminentSnake snake = new EminentSnake(ownerId, points);
        if (nbt.contains("Pos", CompoundTag.TAG_LIST)) {
            var pos = nbt.getList("Pos", CompoundTag.TAG_DOUBLE);
            snake.pos = new Vec3(
                pos.getDouble(0),
                pos.getDouble(1),
                pos.getDouble(2)
            );

            if (nbt.contains("Velocity", CompoundTag.TAG_LIST)) {
                var vel = nbt.getList("Velocity", CompoundTag.TAG_DOUBLE);
                snake.vel = new Vec3(
                    vel.getDouble(0),
                    vel.getDouble(1),
                    vel.getDouble(2)
                );
            }

            if (nbt.contains("IntermediateTarget", CompoundTag.TAG_LIST)) {
                var target = nbt.getList("IntermediateTarget", CompoundTag.TAG_DOUBLE);
                snake.intermediateTarget = new Vec3(
                    target.getDouble(0),
                    target.getDouble(1),
                    target.getDouble(2)
                );
            }
        } else {
            snake.pos = sinkPos.getCenter().add(0, 0.75f, 0);
            snake.vel = Vec3.ZERO;
            snake.intermediateTarget = null;
        }

        if (nbt.contains("PathfindingState", CompoundTag.TAG_STRING)) {
            String stateName = nbt.getString("PathfindingState");
            PathfindingState state = PathfindingState.fromName(stateName);
            if (state != null) {
                snake.pathfindingState = state;
            }
        }
        snake.age = nbt.getInt("Age");

        return snake;
    }

    private static ListTag newDoubleList(double... numbers) {
        ListTag listTag = new ListTag();

        for (double d : numbers) {
            listTag.add(DoubleTag.valueOf(d));
        }

        return listTag;
    }

    private static void putVec3(@NotNull CompoundTag nbt, String key, @NotNull Vec3 vec) {
        nbt.put(key, newDoubleList(vec.x, vec.y, vec.z));
    }

    public void save(@NotNull CompoundTag nbt) {
        nbt.putInt("Points", points);
        nbt.putUUID("Owner", ownerId);
        putVec3(nbt, "Pos", pos);
        putVec3(nbt, "Velocity", vel);
        if (intermediateTarget != null) {
            putVec3(nbt, "IntermediateTarget", intermediateTarget);
        }
        nbt.putString("PathfindingState", pathfindingState.getSerializedName());
        nbt.putInt("Age", age);
    }

    enum PathfindingState implements StringRepresentable {
        RETURN_TO_SINK("return_to_sink"),
        SEEK_FAR("seek_far"),
        SEEK_CLOSE("seek_close"),
        SEEK_TERMINAL("seek_terminal");

        private final String name;

        PathfindingState(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public static @Nullable PathfindingState fromName(String name) {
            for (PathfindingState state : values()) {
                if (state.name.equals(name)) {
                    return state;
                }
            }
            return null;
        }
    }
}
