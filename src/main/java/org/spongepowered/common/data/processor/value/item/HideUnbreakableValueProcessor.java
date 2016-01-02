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
package org.spongepowered.common.data.processor.value.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class HideUnbreakableValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Boolean, Value<Boolean>> {

    public HideUnbreakableValueProcessor() {
        super(ItemStack.class, Keys.HIDE_UNBREAKABLE);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_UNBREAKABLE, actualValue, false);
    }

    @Override
    protected boolean set(ItemStack container, Boolean value) {
        if (!container.hasTagCompound()) {
            container.setTagCompound(new NBTTagCompound());
        }
        if (container.getTagCompound().hasKey(NbtDataUtil.ITEM_HIDE_FLAGS, NbtDataUtil.TAG_INT)) {
            int flag = container.getTagCompound().getInteger(NbtDataUtil.ITEM_HIDE_FLAGS);
            if (value) {
                container.getTagCompound()
                        .setInteger(NbtDataUtil.ITEM_HIDE_FLAGS, flag | DataConstants.HIDE_UNBREAKABLE_FLAG);
            } else {
                container.getTagCompound()
                        .setInteger(NbtDataUtil.ITEM_HIDE_FLAGS,
                                flag - DataConstants.HIDE_UNBREAKABLE_FLAG >= 0 ? flag - DataConstants.HIDE_UNBREAKABLE_FLAG : 0);
            }
        } else {
            if (value) {
                container.getTagCompound().setInteger(NbtDataUtil.ITEM_HIDE_FLAGS, 1);
            }
        }
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(ItemStack container) {
        if (container.hasTagCompound() && container.getTagCompound().hasKey(NbtDataUtil.ITEM_HIDE_FLAGS, NbtDataUtil.TAG_INT)) {
            int flag = container.getTagCompound().getInteger(NbtDataUtil.ITEM_HIDE_FLAGS);
            if (flag - DataConstants.HIDE_UNBREAKABLE_FLAG >= 0) {
                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        }
        return Optional.of(false);
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
