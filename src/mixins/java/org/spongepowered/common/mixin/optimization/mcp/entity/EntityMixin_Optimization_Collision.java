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
package org.spongepowered.common.mixin.optimization.mcp.entity;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

@Mixin(value = Entity.class, priority = 1500)
public abstract class EntityMixin_Optimization_Collision {

    @Inject(method = "doBlockCollisions", at = @At("HEAD"), cancellable = true)
    private void activeCollision$checkForNeighboringChunkIfAvailable(final CallbackInfo ci) {
        final ChunkBridge activeChunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
        if (activeChunk == null || !activeChunk.bridge$areNeighborsLoaded() || activeChunk.bridge$isQueuedForUnload()) {
            ci.cancel();
        }
    }

    @Redirect(method = "doBlockCollisions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean activeCollision$ignoreWorldIsAreaLoaded(final World world, final BlockPos from, final BlockPos to) {
        return true;
    }

    @Inject(method = "handleFluidAcceleration", at = @At("HEAD"), cancellable = true)
    private void activeCollision$BailIfNeighborsAreInactive(final Tag<Fluid> p_210500_1_, final CallbackInfoReturnable<Boolean> cir) {
        final ChunkBridge activeChunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
        if (activeChunk == null || activeChunk.bridge$isQueuedForUnload() || !activeChunk.bridge$areNeighborsLoaded()) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings("deprecation")
    @Redirect(method = "handleFluidAcceleration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(IIIIII)Z"))
    private boolean activeCollision$IgnoreAreaIsLoaded(final World world, final int xStart, final int yStart, final int zStart,
            final int xEnd, final int yEnd, final int zEnd) {
        if (((WorldBridge) world).bridge$isFake()) {
            return world.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd);
        }
        return true;
    }


}
