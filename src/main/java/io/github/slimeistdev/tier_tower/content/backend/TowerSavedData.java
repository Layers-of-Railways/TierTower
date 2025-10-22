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

package io.github.slimeistdev.tier_tower.content.backend;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

public class TowerSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<PlayerTower> towers = new ArrayList<>();

    private TowerSavedData() {}

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        Codec.list(PlayerTower.CODEC).encodeStart(NbtOps.INSTANCE, this.towers)
            .resultOrPartial(LOGGER::error)
            .ifPresent(tag -> nbt.put("towers", tag));
        return nbt;
    }

    private static TowerSavedData load(CompoundTag nbt) {
        TowerSavedData data = new TowerSavedData();
        Codec.list(PlayerTower.CODEC).parse(NbtOps.INSTANCE, nbt.getList("towers", TAG_COMPOUND))
            .resultOrPartial(LOGGER::error)
            .ifPresent(data.towers::addAll);
        return data;
    }

    public static TowerSavedData load(MinecraftServer server) {
        return server.overworld()
            .getDataStorage()
            .computeIfAbsent(TowerSavedData::load, TowerSavedData::new,"tier_towers");
    }

    public List<PlayerTower> getTowers() {
        return towers;
    }
}
