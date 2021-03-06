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
package org.spongepowered.common.data.value;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.key.BoundedKey;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ImmutableSpongeBoundedValue<E> extends AbstractImmutableSpongeValue<E> implements BoundedValue.Immutable<E> {

    private final Supplier<E> minValue;
    private final Supplier<E> maxValue;

    public ImmutableSpongeBoundedValue(Key<? extends BoundedValue<E>> key, E element, Supplier<E> minValue, Supplier<E> maxValue) {
        super(key, element);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BoundedKey<? extends BoundedValue<E>, E> getKey() {
        return (BoundedKey<? extends BoundedValue<E>, E>) super.getKey();
    }

    @Override
    public E getMinValue() {
        return this.minValue.get();
    }

    @Override
    public E getMaxValue() {
        return this.maxValue.get();
    }

    @Override
    public Comparator<? super E> getComparator() {
        return this.key.getElementComparator();
    }

    @Override
    public BoundedValue.Immutable<E> with(E value) {
        return this.getKey().getValueConstructor().getImmutable(value, this.minValue, this.maxValue).asImmutable();
    }

    @Override
    public BoundedValue.Immutable<E> transform(Function<E, E> function) {
        return this.with(function.apply(this.get()));
    }

    @Override
    public BoundedValue.Mutable<E> asMutable() {
        return new MutableSpongeBoundedValue<>(this.getKey(), this.get(), this.minValue, this.maxValue);
    }
}
