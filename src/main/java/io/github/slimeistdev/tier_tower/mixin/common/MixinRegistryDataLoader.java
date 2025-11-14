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

package io.github.slimeistdev.tier_tower.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import io.github.slimeistdev.tier_tower.base.events.DynamicRegistryFreezeCallback;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(RegistryDataLoader.class)
public class MixinRegistryDataLoader {
    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 1))
    private static void onRegistryFreeze(
        List<Pair<WritableRegistry<?>, ?>> instance,
        Consumer<Pair<WritableRegistry<?>, ?>> consumer,
        Operation<Void> original,
        ResourceManager resourceManager,
        RegistryAccess registryAccess
    ) {
        original.call(instance, consumer);

        Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = instance.stream()
            .map(Pair::getFirst)
            .collect(Collectors.toMap(Registry::key, r -> r));

        for (Pair<WritableRegistry<?>, ?> pair : instance) {
            DynamicRegistryFreezeCallback.POST.invoker().onFreeze(pair.getFirst(), new HolderLookup.Provider() {
                @Override
                public <T> @NotNull Optional<HolderLookup.RegistryLookup<T>> lookup(@NotNull ResourceKey<? extends Registry<? extends T>> registryKey) {
                    @SuppressWarnings("unchecked")
                    Registry<T> registry = (Registry<T>) registries.get(registryKey);
                    if (registry != null) {
                        return Optional.of(registry.asLookup());
                    }
                    return registryAccess.lookup(registryKey);
                }
            });
        }
    }
}
