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
package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.BlockStateBoundedDataProvider;

import java.util.Optional;

public class CropsBlockGrowthStageProvider extends BlockStateBoundedDataProvider<Integer> {

    public CropsBlockGrowthStageProvider() {
        super(Keys.GROWTH_STAGE, CropsBlock.class);
    }

    @Override
    protected BoundedValue<Integer> constructValue(BlockState dataHolder, Integer element) {
        final CropsBlock block = (CropsBlock) dataHolder.getBlock();
        return BoundedValue.immutableOf(this.getKey(), element, 0, block.getMaxAge());
    }

    @Override
    protected Optional<Integer> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(((CropsBlock) dataHolder.getBlock()).getAgeProperty()));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Integer value) {
        final CropsBlock block = (CropsBlock) dataHolder.getBlock();
        return Optional.of(dataHolder.with(block.getAgeProperty(), MathHelper.clamp(value, 0, block.getMaxAge())));
    }
}
