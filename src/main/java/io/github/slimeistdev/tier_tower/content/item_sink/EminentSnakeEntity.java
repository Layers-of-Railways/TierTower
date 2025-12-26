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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EminentSnakeEntity extends Entity {
    private BlockPos spawnPos;
    private EminentSnake snake;

    public EminentSnakeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public void init(@NotNull EminentSnake snake, @NotNull BlockPos spawnPos) {
        this.snake = snake;
        this.spawnPos = spawnPos;
    }

    @Override
    public void tick() {
        super.tick();

        if (!(level() instanceof ServerLevel level))
            return;

        if (snake == null) {
            discard();
            return;
        }

        if (spawnPos == null) {
            spawnPos = BlockPos.containing(this.position());
        }

        boolean alive = snake.tick(level, spawnPos);
        if (!alive) {
            kill();
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        if (!nbt.contains("SnakeData", Tag.TAG_COMPOUND)) {
            discard();
            return;
        }

        if (nbt.contains("SpawnPos", Tag.TAG_LIST)) {
            var posList = nbt.getList("SpawnPos", Tag.TAG_INT);
            this.spawnPos = new BlockPos(
                posList.getInt(0),
                posList.getInt(1),
                posList.getInt(2)
            );
        } else {
            this.spawnPos = BlockPos.containing(this.position());
        }

        snake = EminentSnake.load(nbt.getCompound("SnakeData"), spawnPos);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        if (snake != null) {
            var snakeTag = new CompoundTag();
            snake.save(snakeTag);
            nbt.put("SnakeData", snakeTag);
        }
        if (spawnPos != null) {
            var posList = new ListTag();
            posList.add(IntTag.valueOf(spawnPos.getX()));
            posList.add(IntTag.valueOf(spawnPos.getY()));
            posList.add(IntTag.valueOf(spawnPos.getZ()));
            nbt.put("SpawnPos", posList);
        }
    }
}
