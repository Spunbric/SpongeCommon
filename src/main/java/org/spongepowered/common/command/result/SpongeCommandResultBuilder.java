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
package org.spongepowered.common.command.result;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.text.Text;

public class SpongeCommandResultBuilder implements CommandResult.Builder {

    private int result;
    @Nullable private Text errorMessage;

    @Override
    public CommandResult.@NonNull Builder setResult(final int result) {
        Preconditions.checkArgument(result >= 0, "Result must be non-negative!");
        this.result = result;
        return this;
    }

    @Override
    public CommandResult.@NonNull Builder error(@Nullable final Text errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    @NonNull
    public CommandResult build() {
        return new SpongeCommandResult(this.errorMessage == null && this.result > 0, this.result, this.errorMessage);
    }

    @Override
    public CommandResult.@NonNull Builder reset() {
        this.result = 0;
        this.errorMessage = null;
        return this;
    }

}
