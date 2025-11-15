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

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.foundation.TierTowerRegistrate;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TierTowerCreativeModeTabs {
    @SuppressWarnings("Convert2MethodRef")
    public static final TabInfo BASE = register("base", () -> FabricItemGroup.builder()
        .title(Component.translatable("itemGroup.tier_tower.base"))
        .icon(() -> TierTowerBlocks.ITEM_SINK.asStack())
        .displayItems(new RegistrateDisplayItemsGenerator(true, () -> TierTowerCreativeModeTabs.BASE))
        .build());

    @SuppressWarnings("SameParameterValue")
    private static TabInfo register(String name, Supplier<CreativeModeTab> supplier) {
        ResourceKey<CreativeModeTab> key = TierTower.asKey(Registries.CREATIVE_MODE_TAB, name);
        CreativeModeTab tab = supplier.get();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
        return new TabInfo(key, tab);
    }

    public static void register() {
        TierTower.LOGGER.info("Registering creative tabs for " + TierTower.NAME);
        TierTower.registrate().setCreativeTab(BASE.key());
    }

    @SuppressWarnings("RedundantOperationOnEmptyContainer")
    private record RegistrateDisplayItemsGenerator(boolean addItems, Supplier<TabInfo> tabFilter) implements CreativeModeTab.DisplayItemsGenerator {
        private static final Predicate<Item> IS_ITEM_3D_PREDICATE;

        static {
            MutableObject<Predicate<Item>> isItem3d = new MutableObject<>(item -> false);
            EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
                isItem3d.setValue(item -> {
                    ItemRenderer itemRenderer = Minecraft.getInstance()
                        .getItemRenderer();
                    BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
                    return model.isGui3d();
                });
            });
            IS_ITEM_3D_PREDICATE = isItem3d.getValue();
        }

        private static Predicate<Item> makeExclusionPredicate() {
            Set<Item> exclusions = new ReferenceOpenHashSet<>();

            List<ItemProviderEntry<?>> simpleExclusions = List.of();

            for (ItemProviderEntry<?> entry : simpleExclusions) {
                exclusions.add(entry.asItem());
            }

            return exclusions::contains;
        }


        private static List<ItemOrdering> makeOrderings() {
                List<ItemOrdering> orderings = new ReferenceArrayList<>();

                Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleBeforeOrderings = Map.of();

                Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleAfterOrderings = Map.of();

                simpleBeforeOrderings.forEach((entry, otherEntry) -> {
                    orderings.add(ItemOrdering.before(entry.asItem(), otherEntry.asItem()));
                });

                simpleAfterOrderings.forEach((entry, otherEntry) -> {
                    orderings.add(ItemOrdering.after(entry.asItem(), otherEntry.asItem()));
                });

                return orderings;
            }

            private static Function<Item, ItemStack> makeStackFunc() {
                Map<Item, Function<Item, ItemStack>> factories = new Reference2ReferenceOpenHashMap<>();

                Map<ItemProviderEntry<?>, Function<Item, ItemStack>> simpleFactories = Map.of();

                simpleFactories.forEach((entry, factory) -> {
                    factories.put(entry.asItem(), factory);
                });

                return item -> {
                    Function<Item, ItemStack> factory = factories.get(item);
                    if (factory != null) {
                        return factory.apply(item);
                    }
                    return new ItemStack(item);
                };
            }

            private static Function<Item, CreativeModeTab.TabVisibility> makeVisibilityFunc() {
                Map<Item, CreativeModeTab.TabVisibility> visibilities = new Reference2ObjectOpenHashMap<>();

                Map<ItemProviderEntry<?>, CreativeModeTab.TabVisibility> simpleVisibilities = Map.of();

                simpleVisibilities.forEach((entry, factory) -> {
                    visibilities.put(entry.asItem(), factory);
                });

                return item -> Objects.requireNonNullElse(
                    visibilities.get(item),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                );
            }

            @Override
            public void accept(CreativeModeTab.@NotNull ItemDisplayParameters parameters, CreativeModeTab.@NotNull Output output) {
                Predicate<Item> exclusionPredicate = makeExclusionPredicate();
                List<ItemOrdering> orderings = makeOrderings();
                Function<Item, ItemStack> stackFunc = makeStackFunc();
                Function<Item, CreativeModeTab.TabVisibility> visibilityFunc = makeVisibilityFunc();

                List<Item> items = new LinkedList<>();
                if (addItems) {
                    items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE.negate())));
                }
                items.addAll(collectBlocks(exclusionPredicate));
                if (addItems) {
                    items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE)));
                }

                applyOrderings(items, orderings);
                outputAll(output, items, stackFunc, visibilityFunc);
            }

            private List<Item> collectBlocks(Predicate<Item> exclusionPredicate) {
                List<Item> items = new ReferenceArrayList<>();
                for (RegistryEntry<Block> entry : TierTower.registrate().getAll(Registries.BLOCK)) {
                    if (!TierTowerRegistrate.isInCreativeTab(entry, tabFilter.get().key()))
                        continue;
                    Item item = entry.get()
                        .asItem();
                    if (item == Items.AIR)
                        continue;
                    if (!exclusionPredicate.test(item))
                        items.add(item);
                }
                items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
                return items;
            }

            private List<Item> collectItems(Predicate<Item> exclusionPredicate) {
                List<Item> items = new ReferenceArrayList<>();
                for (RegistryEntry<Item> entry : TierTower.registrate().getAll(Registries.ITEM)) {
                    if (!TierTowerRegistrate.isInCreativeTab(entry, tabFilter.get().key()))
                        continue;
                    Item item = entry.get();
                    if (item instanceof BlockItem)
                        continue;
                    if (!exclusionPredicate.test(item))
                        items.add(item);
                }
                return items;
            }

            private static void applyOrderings(List<Item> items, List<ItemOrdering> orderings) {
                for (ItemOrdering ordering : orderings) {
                    int anchorIndex = items.indexOf(ordering.anchor());
                    if (anchorIndex != -1) {
                        Item item = ordering.item();
                        int itemIndex = items.indexOf(item);
                        if (itemIndex != -1) {
                            items.remove(itemIndex);
                            if (itemIndex < anchorIndex) {
                                anchorIndex--;
                            }
                        }
                        if (ordering.type() == ItemOrdering.Type.AFTER) {
                            items.add(anchorIndex + 1, item);
                        } else {
                            items.add(anchorIndex, item);
                        }
                    }
                }
            }

            private static void outputAll(CreativeModeTab.Output output, List<Item> items, Function<Item, ItemStack> stackFunc, Function<Item, CreativeModeTab.TabVisibility> visibilityFunc) {
                for (Item item : items) {
                    output.accept(stackFunc.apply(item), visibilityFunc.apply(item));
                }
            }

            private record ItemOrdering(Item item, Item anchor, Type type) {
                public static ItemOrdering before(Item item, Item anchor) {
                    return new ItemOrdering(item, anchor, Type.BEFORE);
                }

                public static ItemOrdering after(Item item, Item anchor) {
                    return new ItemOrdering(item, anchor, Type.AFTER);
                }

                public enum Type {
                    BEFORE,
                    AFTER;
                }
            }
        }

    public record TabInfo(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {}
}
