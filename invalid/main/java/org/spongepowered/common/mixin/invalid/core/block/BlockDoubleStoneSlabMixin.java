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
package org.spongepowered.common.mixin.invalid.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockDoubleStoneSlab;
import net.minecraft.block.BlockDoubleStoneSlabNew;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSeamlessData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSlabData;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.SlabTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSeamlessData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSlabData;

@Mixin(value = {BlockDoubleStoneSlabNew.class, BlockDoubleStoneSlab.class})
public abstract class BlockDoubleStoneSlabMixin extends BlockStoneSlabMixin {

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public ImmutableList<Immutable<?, ?>> bridge$getManipulators(final BlockState blockState) {
        return ImmutableList.<Immutable<?, ?>>of(this.impl$getSlabTypeFor(blockState), this.impl$getIsSeamlessFor(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends Immutable<?, ?>> immutable) {
        return ImmutableSlabData.class.isAssignableFrom(immutable) || ImmutableSeamlessData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableSlabData impl$getSlabTypeFor(final BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeSlabData.class,
                blockState.getBlock() instanceof BlockStoneSlab
                        ? (SlabType) (Object) blockState.get(BlockStoneSlab.VARIANT)
                        : blockState.getBlock() instanceof BlockStoneSlabNew
                                ? (SlabType) (Object) blockState.get(BlockStoneSlabNew.VARIANT)
                                : SlabTypes.COBBLESTONE);
    }

    private ImmutableSeamlessData impl$getIsSeamlessFor(final BlockState blockState) {
        if (blockState.getBlock() instanceof BlockStoneSlab) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeSeamlessData.class, blockState.get(BlockStoneSlab.SEAMLESS));
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeSeamlessData.class, blockState.get(BlockStoneSlabNew.SEAMLESS));
    }

}
