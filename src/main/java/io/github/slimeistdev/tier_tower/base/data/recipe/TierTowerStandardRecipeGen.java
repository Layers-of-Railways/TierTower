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

package io.github.slimeistdev.tier_tower.base.data.recipe;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.foundation.data.load_conditions.LoadCondition;
import io.github.slimeistdev.tier_tower.registry.TierTowerBlocks;
import io.github.slimeistdev.tier_tower.registry.TierTowerItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static io.github.slimeistdev.tier_tower.foundation.data.ConditionalFinishedRecipe.wrap;
import static io.github.slimeistdev.tier_tower.foundation.data.load_conditions.LoadCondition.modMissing;

@SuppressWarnings("unused")
public class TierTowerStandardRecipeGen extends TierTowerRecipeProvider {
    GeneratedRecipe
        START = null,

        EMINENCE_NUGGET = create(TierTowerItems.EMINENCE_NUGGET)
            .unlockedBy(I::honeycomb)
            .loadWhen(modMissing("create"))
            .viaShapeless(b -> b
                .requires(I.honeycomb())
                .requires(I.bottledEminence())),

        SUBLIMINATOR = create(TierTowerBlocks.SUBLIMINATOR)
            .unlockedBy(I::blastFurnace)
            .viaShaped(b -> b
                .pattern("@#@")
                .pattern("-X-")
                .pattern("@B@")
                .define('@', I.ironBlock())
                .define('#', I.ironBars())
                .define('-', I.netheriteIngot())
                .define('B', I.deepslateBricks())
                .define('X', I.blastFurnace())),

        OBELISK = create(TierTowerBlocks.OBELISK)
            .unlockedBy(I::bottledEminence)
            .viaShaped(b -> b
                .pattern("###")
                .pattern("-b-")
                .pattern("@b@")
                .define('#', I.deepslateTiles())
                .define('-', I.goldIngot())
                .define('b', I.bottledEminence())
                .define('@', I.eminentGlass())),

        DEEPSLATE_PILLAR = create(TierTowerBlocks.DEEPSLATE_PILLAR)
            .returns(2)
            .unlockedBy(I::deepslateBricks)
            .viaShaped(b -> b
                .pattern("#")
                .pattern("#")
                .define('#', I.deepslateBricks())),

        EMINENT_GLASS = create(TierTowerBlocks.EMINENT_GLASS)
            .returns(8)
            .unlockedBy(I::bottledEminence)
            .viaShaped(b -> b
                .pattern("#/#")
                .pattern("/b/")
                .pattern("#/#")
                .define('#', I.glass())
                .define('/', I.amethystShard())
                .define('b', I.bottledEminence())),

        EMINENT_GLASS_BRICKS = create(TierTowerBlocks.EMINENT_GLASS_BRICKS)
            .returns(4)
            .unlockedBy(I::eminentGlass)
            .viaShaped(b -> b
                .pattern("##")
                .pattern("##")
                .define('#', I.eminentGlass())),

        EMINENT_DEEPSLATE_PILLAR = eminentConversion(TierTowerBlocks.EMINENT_DEEPSLATE_PILLAR, I::deepslatePillar),
        EMINENT_DEEPSLATE_BRICKS = eminentConversion(TierTowerBlocks.EMINENT_DEEPSLATE_BRICKS, I::deepslateBricks),
        EMINENT_DEEPSLATE_TILES = eminentConversion(TierTowerBlocks.EMINENT_DEEPSLATE_TILES, I::deepslateTiles),

        END = null;

    GeneratedRecipe eminentConversion(Supplier<ItemLike> result, Supplier<ItemLike> base) {
        return create(result)
            .returns(4)
            .unlockedBy(I::bottledEminence)
            .viaShaped(b -> b
                .pattern(" # ")
                .pattern("#b#")
                .pattern(" # ")
                .define('#', base.get())
                .define('b', I.bottledEminence()));
    }

    GeneratedRecipe eminentConversion(ItemProviderEntry<? extends ItemLike> result, Supplier<ItemLike> base) {
        return eminentConversion(result::get, base);
    }

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ResourceLocation result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike> result) {
        return create(result::get);
    }

    public TierTowerStandardRecipeGen(PackOutput output) {
        super(output);
    }

    @Override
    public @NotNull String getName() {
        return "Standard Recipes";
    }

    class GeneratedRecipeBuilder {

        private final String path;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;
        private @Nullable LoadCondition loadCondition;

        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        private GeneratedRecipeBuilder(String path) {
            this.path = path;
            this.suffix = "";
            this.amount = 1;
        }

        public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
            this(path);
            this.result = result;
        }

        public GeneratedRecipeBuilder(String path, ResourceLocation result) {
            this(path);
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(item.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(tag.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipeBuilder loadWhen(LoadCondition condition) {
            this.loadCondition = condition;
            return this;
        }

        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(wrap(consumer, loadCondition), createLocation("crafting"));
            });
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(consumer -> {
                ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(wrap(consumer, loadCondition), createLocation("crafting"));
            });
        }

        private static ResourceLocation clean(ResourceLocation loc) {
            String path = loc.getPath();
            while (path.contains("//"))
                path = path.replaceAll("//", "/");
            return new ResourceLocation(loc.getNamespace(), path);
        }

        private ResourceLocation createSimpleLocation(String recipeType) {
            return clean(TierTower.asResource(recipeType + "/" + getRegistryName().getPath() + suffix));
        }

        private ResourceLocation createLocation(String recipeType) {
            return clean(TierTower.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix));
        }

        @SuppressWarnings("SameParameterValue")
        private static <V> ResourceLocation getKeyOrThrow(Registry<V> registry, V value) {
            ResourceLocation key = registry.getKey(value);
            if (key == null) {
                throw new IllegalStateException("Could not get key for value " + value + "!");
            }
            return key;
        }

        private static ResourceLocation getKeyOrThrow(Item value) {
            return getKeyOrThrow(BuiltInRegistries.ITEM, value);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? getKeyOrThrow(result.get().asItem()) : compatDatagenOutput;
        }

        GeneratedStonecuttingRecipeBuilder viaStonecutting(Supplier<? extends ItemLike> item) {
            return unlockedBy(item).viaStonecuttingIngredient(() -> Ingredient.of(item.get()));
        }

        GeneratedStonecuttingRecipeBuilder viaStonecuttingTag(Supplier<TagKey<Item>> tag) {
            return unlockedByTag(tag).viaStonecuttingIngredient(() -> Ingredient.of(tag.get()));
        }

        GeneratedStonecuttingRecipeBuilder viaStonecuttingIngredient(Supplier<Ingredient> ingredient) {
            return new GeneratedStonecuttingRecipeBuilder(ingredient);
        }

        class GeneratedStonecuttingRecipeBuilder {

            private final Supplier<Ingredient> ingredient;

            GeneratedStonecuttingRecipeBuilder(Supplier<Ingredient> ingredient) {
                this.ingredient = ingredient;
            }

            private GeneratedRecipe create(UnaryOperator<SingleItemRecipeBuilder> builder) {
                return register(consumer -> {
                    SingleItemRecipeBuilder b = builder.apply(SingleItemRecipeBuilder.stonecutting(ingredient.get(), RecipeCategory.MISC, result.get(), amount));
                    if (unlockedBy != null)
                        b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                    b.save(wrap(consumer, loadCondition), createLocation("stonecutting"));
                });
            }

            private GeneratedRecipe create() {
                return create(b -> b);
            }
        }
    }
}
