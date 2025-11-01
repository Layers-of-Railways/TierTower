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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class Utils {
    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isEnvVarTrue(String name) {
        try {
            String result = System.getenv(name);
            return result != null && result.toLowerCase(Locale.ROOT).equals("true");
        } catch (SecurityException e) {
            TierTower.LOGGER.warn("Caught a security exception while trying to access environment variable `{}`.", name);
            return false;
        }
    }

    public static <T> @Nullable Registry<T> castRegistry(Registry<?> registry, ResourceKey<? extends Registry<T>> key) {
        if (registry.key().equals(key)) {
            @SuppressWarnings("unchecked")
            Registry<T> casted = (Registry<T>) registry;
            return casted;
        }
        return null;
    }
}
