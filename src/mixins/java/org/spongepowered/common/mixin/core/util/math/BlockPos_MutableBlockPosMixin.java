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
package org.spongepowered.common.mixin.core.util.math;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;

@Mixin(BlockPos.MutableBlockPos.class)
@Implements(@Interface(iface = BlockPosBridge.class, prefix = "inline$", unique = true))
public abstract class BlockPos_MutableBlockPosMixin extends BlockPos implements BlockPosBridge {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int z;

    // NOT USED
    public BlockPos_MutableBlockPosMixin(final int xIn, final int yIn, final int zIn) {
        super(xIn, yIn, zIn);
    }

    @Override
    public boolean bridge$isValidPosition() {
        return this.x >= -30000000 && this.z >= -30000000 && this.x < 30000000 && this.z < 30000000 && this.y >= 0 && this.y < 256;
    }

    @Override
    public boolean bridge$isValidXZPosition() {
        return this.x >= -30000000 && this.z >= -30000000 && this.x < 30000000 && this.z < 30000000;
    }

    @Override
    public boolean bridge$isInvalidYPosition() {
        return this.y < 0 || this.y >= 256;
    }

}
