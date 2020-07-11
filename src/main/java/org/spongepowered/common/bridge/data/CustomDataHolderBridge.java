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
package org.spongepowered.common.bridge.data;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomDataHolderBridge {

    DataTransactionResult bridge$offerCustom(DataManipulator.Mutable manipulator, MergeFunction function);

    <T extends DataManipulator.Mutable> Optional<T> bridge$getCustom(Class<T> customClass);

    DataTransactionResult bridge$removeCustom(Class<? extends DataManipulator.Mutable> customClass);

    boolean bridge$hasManipulators();

    boolean bridge$supportsCustom(Key<?> key);

    <E> Optional<E> bridge$getCustom(Key<? extends Value<E>> key);

    <E, V extends Value<E>> Optional<V> bridge$getCustomValue(Key<V> key);

    Collection<DataManipulator.Mutable> bridge$getCustomManipulators();

    <E> DataTransactionResult bridge$offerCustom(Key<? extends Value<E>> key, E value);

    DataTransactionResult bridge$removeCustom(Key<?> key);

    void bridge$removeCustomFromNBT(DataManipulator.Mutable manipulator);

    void bridge$addFailedData(ImmutableList<DataView> failedData);

    List<DataView> bridge$getFailedData();
}
