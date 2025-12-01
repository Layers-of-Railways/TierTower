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

package io.github.slimeistdev.tier_tower.registry;

import io.github.slimeistdev.tier_tower.TierTower;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static io.github.slimeistdev.tier_tower.registry.TierTowerTags.NameSpace.*;

public class TierTowerTags {
    public enum NameSpace {
        MOD(TierTower.MOD_ID),
        FORGE("c"),
        CREATE("create");

        public final String id;

        NameSpace(String id) {
            this.id = id;
        }

        public ResourceLocation id(String path) {
            return new ResourceLocation(this.id, path);
        }

        public ResourceLocation id(Enum<?> entry, @Nullable String pathOverride) {
            return this.id(pathOverride != null ? pathOverride : entry.name().toLowerCase(Locale.ROOT));
        }
    }

    public enum AllBlockTags {
        NON_MOVABLE(CREATE),
        RELOCATION_NOT_SUPPORTED(FORGE),
        ;

        public final TagKey<Block> tag;

        AllBlockTags() {
            this(MOD);
        }

        AllBlockTags(NameSpace namespace) {
            this(namespace, null);
        }

        AllBlockTags(NameSpace namespace, @Nullable String pathOverride) {
            this.tag = TagKey.create(Registries.BLOCK, namespace.id(this, pathOverride));
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Block block) {
            return block.builtInRegistryHolder()
                .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack != null && stack.getItem() instanceof BlockItem blockItem && matches(blockItem.getBlock());
        }

        public boolean matches(BlockState state) {
            return state.is(tag);
        }

        private static void init() {}

    }

    public enum AllItemTags {
        UPRIGHT_ON_BELT(CREATE);

        public final TagKey<Item> tag;

        AllItemTags() {
            this(MOD);
        }

        AllItemTags(NameSpace namespace) {
            this(namespace, null);
        }

        AllItemTags(NameSpace namespace, @Nullable String pathOverride) {
            this.tag = TagKey.create(Registries.ITEM, namespace.id(this, pathOverride));
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Item item) {
            return item.builtInRegistryHolder()
                .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }

        public static void init() {}
    }

    public static void init() {
        AllBlockTags.init();
        AllItemTags.init();
    }
}
