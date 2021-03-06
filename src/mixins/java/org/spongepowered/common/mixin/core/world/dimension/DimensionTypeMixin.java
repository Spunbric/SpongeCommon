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
package org.spongepowered.common.mixin.core.world.dimension;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.dimension.DimensionTypeAccessor;
import org.spongepowered.common.world.dimension.DimensionToTypeRegistry;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import javax.annotation.Nullable;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    @Nullable private SpongeDimensionType impl$spongeDimensionType;

    @Inject(method = "register", at = @At("RETURN"))
    private static void impl$setupBridgeFields(String id, final DimensionType dimensionType, CallbackInfoReturnable<DimensionType> cir) {
        // Commence hackery to get the dimension class this type is meant to make
        final MinecraftServer server = SpongeCommon.getServer();
        final WorldSettings worldSettings = new WorldSettings(0L, GameType.ADVENTURE, false, false, WorldType.DEFAULT);
        final IChunkStatusListener iChunkStatusListener = ((MinecraftServerAccessor) server).accessor$getChunkStatusListenerFactory().create(11);
        final ServerWorld fakeWorld = new ServerWorld(server, server.getBackgroundExecutor(),
            server.getActiveAnvilConverter().getSaveLoader("fake", server), new WorldInfo(worldSettings, "fake"), dimensionType,
            server.getProfiler(), iChunkStatusListener);

        final Dimension dimension = dimensionType.create(fakeWorld);
        final Class<? extends Dimension> dimensionClass = dimension.getClass();
        @Nullable SpongeDimensionType logicType = DimensionToTypeRegistry.getInstance().getLogicType(dimensionClass);

        if (logicType == null) {
            logicType = new SpongeDimensionType(id, ((DimensionTypeAccessor) dimensionType)::accessor$getFactory, dimensionType::hasSkyLight);
            DimensionToTypeRegistry.getInstance().registerTypeMapping(dimensionClass, SpongeCommon.getRegistry().getCatalogRegistry().registerCatalog(logicType));
        }
    }

    @Override
    public SpongeDimensionType bridge$getSpongeDimensionType() {
        return this.impl$spongeDimensionType;
    }

    @Override
    public void bridge$setSpongeDimensionType(SpongeDimensionType dimensionType) {
        this.impl$spongeDimensionType = dimensionType;
    }
}
