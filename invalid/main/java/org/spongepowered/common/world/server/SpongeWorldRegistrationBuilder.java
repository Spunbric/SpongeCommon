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
package org.spongepowered.common.world.server;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.ResourceKey;

import javax.annotation.Nullable;

public final class SpongeWorldRegistrationBuilder implements WorldRegistration.Builder {

    @Nullable private ResourceKey key;
    @Nullable private String directoryName;

    @Override
    public WorldRegistration.Builder key(ResourceKey key) {
        this.key = checkNotNull(key);
        return this;
    }

    @Override
    public WorldRegistration.Builder directoryName(String name) {
        this.directoryName = checkNotNull(name);
        return this;
    }

    @Override
    public WorldRegistration.Builder reset() {
        this.directoryName = "World A";
        return this;
    }

    @Override
    public WorldRegistration build() throws IllegalStateException {
        checkNotNull(this.key);
        checkNotNull(this.directoryName);
        checkState(!this.directoryName.isEmpty(), "Directory name cannot be empty!");

        return new WorldRegistration(this.key, this.directoryName);
    }
}
