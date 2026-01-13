/*
 * Tier Tower
 * Copyright (c) 2026 The Tier Tower Team
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

import io.github.slimeistdev.tier_tower.annotation.mixin.ConditionalMixin;
import io.github.slimeistdev.tier_tower.annotation.mixin.DevEnvMixin;
import io.github.slimeistdev.tier_tower.compat.Mods;
import io.github.slimeistdev.tier_tower.mixin.TierTowerMixinPlugin;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;

public class ConditionalMixinManager {
    public static boolean shouldApply(String className) {
        try {
            List<AnnotationNode> annotationNodes = MixinService.getService().getBytecodeProvider().getClassNode(className).visibleAnnotations;
            if (annotationNodes == null) return true;

            boolean shouldApply = true;
            for (AnnotationNode node : annotationNodes) {
                if (node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
                    List<Mods> mods = Annotations.getValue(node, "mods", true, Mods.class);
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    boolean anyModsLoaded = anyModsLoaded(mods);
                    shouldApply = anyModsLoaded == applyIfPresent;
                    TierTowerMixinPlugin.LOGGER.debug("{} is{}being applied because the mod(s) {} are{}loaded", className, shouldApply ? " " : " not ", mods, anyModsLoaded ? " " : " not ");
                }
                if (node.desc.equals(Type.getDescriptor(DevEnvMixin.class))) {
                    shouldApply &= Utils.isDevEnv();
                }
            }
            return shouldApply;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean anyModsLoaded(List<Mods> mods) {
        for (Mods mod : mods) {
            if (mod.isLoaded) return true;
        }
        return false;
    }
}
