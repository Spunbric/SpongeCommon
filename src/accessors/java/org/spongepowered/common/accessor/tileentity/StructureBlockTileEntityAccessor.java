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
package org.spongepowered.common.accessor.tileentity;

import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureBlockTileEntity.class)
public interface StructureBlockTileEntityAccessor {

    @Accessor("author") String accessor$getAuthor();

    @Accessor("author") void accessor$setAuthor(String author);

    @Accessor("ignoreEntities") boolean accessor$getIgnoreEntities();

    @Accessor("integrity") float accessor$getIntegrity();

    @Accessor("mode") StructureMode accessor$getMode();

    @Accessor("mode") void accessor$setMode(StructureMode mode);

    @Accessor("position") BlockPos accessor$getPosition();

    @Accessor("position") void accessor$setPosition(BlockPos position);

    @Accessor("showAir") boolean accessor$getShowAir();

    @Accessor("showBoundingBox") boolean accessor$getShowBoundingBox();

    @Accessor("seed") long accessor$getSeed();

    @Accessor("size") BlockPos accessor$getSize();

    @Accessor("size") void accessor$setSize(BlockPos size);
    
}
