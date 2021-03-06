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
package org.spongepowered.common.event.lifecycle;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;

public final class StoppingEngineEventImpl<E extends Engine> implements StoppingEngineEvent<E> {

    private final Cause cause;
    private final TypeToken<E> genericType;
    private final E engine;
    private final Game game;

    public StoppingEngineEventImpl(final Cause cause, final TypeToken<E> genericType, final Game game, final E engine) {
        this.cause = cause;
        this.genericType = genericType;
        this.game = game;
        this.engine = engine;
    }

    @Override
    public E getEngine() {
        return this.engine;
    }

    @Override
    public TypeToken<E> getGenericType() {
        return this.genericType;
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
