/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.item.recipe.crafting;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.common.accessor.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SpongeShapedCraftingRecipeBuilder extends SpongeCatalogBuilder<ShapedCraftingRecipe, ShapedCraftingRecipe.Builder> implements
        ShapedCraftingRecipe.Builder, ShapedCraftingRecipe.Builder.AisleStep.ResultStep,
        ShapedCraftingRecipe.Builder.RowsStep.ResultStep, ShapedCraftingRecipe.Builder.EndStep {

    private final List<String> aisle = Lists.newArrayList();
    private final Map<Character, Ingredient> ingredientMap = new Char2ObjectArrayMap<>();
    private ShapedCraftingRecipe shape;

    private ItemStack result = ItemStack.empty();
    private String groupName = "";

    @Override
    public AisleStep aisle(final String... aisle) {
        checkNotNull(aisle, "aisle");
        this.aisle.clear();
        this.ingredientMap.clear();
        Collections.addAll(this.aisle, aisle);
        return this;
    }

    @Override
    public AisleStep.ResultStep where(final char symbol, final Ingredient ingredient) throws IllegalArgumentException {
        if (this.aisle.stream().noneMatch(row -> row.indexOf(symbol) >= 0)) {
            throw new IllegalArgumentException("The symbol '" + symbol + "' is not defined in the aisle pattern.");
        }
        this.ingredientMap.put(symbol, ingredient == null ? Ingredient.empty() : ingredient);
        return this;
    }

    @Override
    public AisleStep.ResultStep where(final Map<Character, Ingredient> ingredientMap) throws IllegalArgumentException {
        for (final Map.Entry<Character, Ingredient> entry : ingredientMap.entrySet()) {
            this.where(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public RowsStep rows() {
        this.aisle.clear();
        this.ingredientMap.clear();
        return this;
    }

    @Override
    public RowsStep.ResultStep row(final int skip, final Ingredient... ingredients) {
        final int columns = ingredients.length + skip;
        if (!this.aisle.isEmpty()) {
            checkState(this.aisle.get(0).length() == columns, "The rows have an inconsistent width.");
        }
        final StringBuilder row = new StringBuilder();
        for (int i = 0; i < skip; i++) {
            row.append(" ");
        }

        int key = 'a' + columns * this.aisle.size();
        for (final Ingredient ingredient : ingredients) {
            key++;
            final char character = (char) key;
            row.append(character);
            this.ingredientMap.put(character, ingredient);
        }
        this.aisle.add(row.toString());
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder.ResultStep shapedLike(ShapedCraftingRecipe recipe) {
        this.shape = recipe; // TODO
        return this;
    }

    @Override
    public EndStep result(final ItemStack result) {
        checkNotNull(result, "result");
        this.result = result.copy();
        return this;
    }

    @Override
    public EndStep group(@Nullable final String name) {
        this.groupName = Strings.nullToEmpty(name);
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder.EndStep key(ResourceKey key) {
        super.key(key);
        return this;
    }

    @Override
    protected ShapedCraftingRecipe build(ResourceKey key) {
        checkState(!this.aisle.isEmpty(), "aisle has not been set");
        checkState(!this.ingredientMap.isEmpty(), "no ingredients set");
        checkState(!this.result.isEmpty(), "no result set");

        final Iterator<String> aisleIterator = this.aisle.iterator();
        String aisleRow = aisleIterator.next();
        final int width = aisleRow.length();
        int height = 1;

        checkState(width > 0, "The aisle cannot be empty.");

        while (aisleIterator.hasNext()) {
            height++;
            aisleRow = aisleIterator.next();
            checkState(aisleRow.length() == width, "The aisle has an inconsistent width.");
        }

        final String[] keys = this.aisle.toArray(new String[this.aisle.size()]);
        final Map<String, net.minecraft.item.crafting.Ingredient> ingredientsMap = this.ingredientMap.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey().toString(), e -> IngredientUtil.toNative(e.getValue())));

        // Default space to Empty Ingredient
        ingredientsMap.putIfAbsent(" ", net.minecraft.item.crafting.Ingredient.EMPTY);

        // Throws JsonException when pattern is not complete or defines unused Ingredients
        final NonNullList<net.minecraft.item.crafting.Ingredient> ingredients = ShapedRecipeAccessor
            .accessor$deserializeIngredients(keys, ingredientsMap, width, height);
        // TODO generate JSON
        return (ShapedCraftingRecipe) new ShapedRecipe((ResourceLocation)(Object) key, this.groupName, width, height, ingredients, ItemStackUtil.toNative(this.result));
    }

    @Override
    public ShapedCraftingRecipe.Builder reset() {
        super.reset();
        this.aisle.clear();
        this.ingredientMap.clear();
        this.result = ItemStack.empty();
        this.groupName = "";
        return this;
    }

}
