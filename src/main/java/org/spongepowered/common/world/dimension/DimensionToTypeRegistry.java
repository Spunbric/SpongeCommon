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

import net.minecraft.world.dimension.Dimension;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class DimensionToTypeRegistry {

    private static final DimensionToTypeRegistry instance = new DimensionToTypeRegistry();

    private final Map<Class<Dimension>, SpongeDimensionType> dimensionTypeMappings;

    public DimensionToTypeRegistry() {
        this.dimensionTypeMappings = new IdentityHashMap<>();
    }

    public void registerTypeMapping(Class<? extends Dimension> dimensionClass, SpongeDimensionType logicType) {
        checkNotNull(dimensionClass);
        checkNotNull(logicType);

        this.dimensionTypeMappings.put((Class<Dimension>) dimensionClass, logicType);
    }

    @Nullable
    public SpongeDimensionType getLogicType(Class<? extends Dimension> dimensionClass) {
        return this.dimensionTypeMappings.get(dimensionClass);
    }

    public static DimensionToTypeRegistry getInstance() {
        return instance;
    }
}