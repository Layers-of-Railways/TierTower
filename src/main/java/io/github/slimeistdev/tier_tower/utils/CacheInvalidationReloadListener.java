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

package io.github.slimeistdev.tier_tower.utils;

import io.github.slimeistdev.tier_tower.TierTower;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CacheInvalidationReloadListener extends SimplePreparableReloadListener<Unit> implements IdentifiableResourceReloadListener {
    public static final CacheInvalidationReloadListener CLIENT_RESOURCES = new CacheInvalidationReloadListener("assets");
    public static final CacheInvalidationReloadListener SERVER_DATA = new CacheInvalidationReloadListener("data");

    private final Set<Runnable> callbacks = new HashSet<>();
    private final ResourceLocation id;

    private CacheInvalidationReloadListener(String type) {
        this.id = TierTower.asResource(type + "_cache_invalidation_listener");
    }

    public void registerCallback(Runnable callback) {
        this.callbacks.add(callback);
    }

    public void unregisterCallback(Runnable callback) {
        this.callbacks.remove(callback);
    }

    @Override
    protected @NotNull Unit prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        return Unit.INSTANCE;
    }

    @Override
    protected void apply(@NotNull Unit object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        callbacks.forEach(Runnable::run);
    }

    @Override
    public ResourceLocation getFabricId() {
        return id;
    }
}
