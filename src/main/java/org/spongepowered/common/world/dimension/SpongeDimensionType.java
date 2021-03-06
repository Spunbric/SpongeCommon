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
package org.spongepowered.common.world.dimension;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class SpongeDimensionType implements DimensionType {

    private final ResourceKey key;
    private final Path configPath;
    private final SpongeConfig<DimensionConfig> config;
    private final Context context;
    private final Supplier<BiFunction<World, net.minecraft.world.dimension.DimensionType, ? extends Dimension>> dimensionFactory;
    private final BooleanSupplier hasSkyLight;

    public SpongeDimensionType(String id, Supplier<BiFunction<World, net.minecraft.world.dimension.DimensionType, ? extends Dimension>> dimensionFactory, BooleanSupplier hasSkyLight) {
        checkNotNull(id);
        checkNotNull(dimensionFactory);
        checkNotNull(hasSkyLight);

        final String modId = SpongeImplHooks.getActiveModContainer().getMetadata().getId();
        final String dimName = id.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");

        this.key = ResourceKey.of(modId, dimName);
        this.configPath = SpongeCommon.getSpongeConfigDirectory().resolve("worlds").resolve(modId).resolve(dimName);
        this.config = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, this.configPath.resolve("dimension.conf"),
            SpongeCommon.ECOSYSTEM_ID, SpongeCommon.getGlobalConfigAdapter(), false);
        this.context = new Context(Context.DIMENSION_KEY, modId + "." + dimName);
        this.dimensionFactory = dimensionFactory;
        this.hasSkyLight = hasSkyLight;
    }

    @Override
    public ResourceKey getKey() {
        return this.key;
    }

    @Override
    public boolean hasSkylight() {
        return this.hasSkyLight.getAsBoolean();
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public SpongeConfig<DimensionConfig> getConfigAdapter() {
        return this.config;
    }

    public BiFunction<World, net.minecraft.world.dimension.DimensionType, ? extends Dimension> getDimensionFactory() {
        return this.dimensionFactory.get();
    }
}
