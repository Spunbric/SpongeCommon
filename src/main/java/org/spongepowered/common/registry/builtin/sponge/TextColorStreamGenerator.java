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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.accessor.util.text.TextFormattingAccessor;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public final class TextColorStreamGenerator {

    private TextColorStreamGenerator() {
    }

    public static Stream<Tuple<TextColor, TextFormatting>> stream() {
        return Stream.of(
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("aqua"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.AQUA).accessor$getColor())), TextFormatting.AQUA),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("black"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.BLACK).accessor$getColor())), TextFormatting.BLACK),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("blue"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.BLUE).accessor$getColor())), TextFormatting.BLUE),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_aqua"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_AQUA).accessor$getColor())), TextFormatting.DARK_AQUA),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_blue"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_BLUE).accessor$getColor())), TextFormatting.DARK_BLUE),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_gray"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_GRAY).accessor$getColor())), TextFormatting.DARK_GRAY),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_green"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_GREEN).accessor$getColor())), TextFormatting.DARK_GREEN),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_purple"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_PURPLE).accessor$getColor())), TextFormatting.DARK_PURPLE),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("dark_red"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.DARK_RED).accessor$getColor())), TextFormatting.DARK_RED),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("gold"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.GOLD).accessor$getColor())), TextFormatting.GOLD),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("gray"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.GRAY).accessor$getColor())), TextFormatting.GRAY),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("green"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.GREEN).accessor$getColor())), TextFormatting.GREEN),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("light_purple"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.LIGHT_PURPLE).accessor$getColor())), TextFormatting.LIGHT_PURPLE),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("red"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.RED).accessor$getColor())), TextFormatting.RED),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("yellow"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.YELLOW).accessor$getColor())), TextFormatting.YELLOW),
            Tuple.of(new SpongeTextColor(ResourceKey.minecraft("white"), Color.ofRgb(((TextFormattingAccessor) (Object) TextFormatting.WHITE).accessor$getColor())), TextFormatting.WHITE)
        );
    }
}
