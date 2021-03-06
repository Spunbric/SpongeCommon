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
package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Optional;
import java.util.function.Supplier;

public class BlockStateBooleanDataProvider extends BlockStateDataProvider<Boolean> {

    private final BooleanProperty property;

    public BlockStateBooleanDataProvider(Key<? extends Value<Boolean>> key,
            Class<? extends Block> blockType, BooleanProperty property) {
        super(key, blockType);
        this.property = property;
    }

    public BlockStateBooleanDataProvider(Supplier<? extends Key<? extends Value<Boolean>>> key,
            Class<? extends Block> blockType, BooleanProperty property) {
        this(key.get(), blockType, property);
    }

    public BlockStateBooleanDataProvider(Key<? extends Value<Boolean>> key,
            BooleanProperty property) {
        this(key, Block.class, property);
    }

    public BlockStateBooleanDataProvider(Supplier<? extends Key<? extends Value<Boolean>>> key,
            BooleanProperty property) {
        this(key.get(), Block.class, property);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.of(dataHolder.with(this.property, value));
    }
}
