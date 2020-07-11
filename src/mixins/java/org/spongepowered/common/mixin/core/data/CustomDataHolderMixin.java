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
package org.spongepowered.common.mixin.core.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin({TileEntity.class, Entity.class, SpongeUser.class, ItemStack.class})
public abstract class CustomDataHolderMixin implements CustomDataHolderBridge {

    // TODO data is never added
    private List<Mutable> impl$manipulators = Lists.newArrayList();
    private List<DataView> impl$failedData = Lists.newArrayList();

    @Override
    public boolean bridge$hasManipulators() {
        return !this.impl$manipulators.isEmpty();
    }

    @Override
    public boolean bridge$supportsCustom(final Key<?> key) {
        return this.impl$manipulators.stream()
                .anyMatch(manipulator -> manipulator.supports(key));
    }

    @Override
    public <E> Optional<E> bridge$getCustom(final Key<? extends Value<E>> key) {
        return this.impl$manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.get(key));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> bridge$getCustomValue(final Key<V> key) {
        return this.impl$manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.getValue(key));
    }

    @Override
    public Collection<Mutable> bridge$getCustomManipulators() {
        return this.impl$manipulators.stream().map(Mutable::copy).collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <E> DataTransactionResult bridge$offerCustom(final Key<? extends Value<E>> key, final E value) {
        for (final Mutable manipulator : this.impl$manipulators) {
            if (manipulator.supports(key)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(((org.spongepowered.api.data.value.Value.Mutable) manipulator.getValue((Key) key).get()).asImmutable());
                manipulator.set(key, value);
                builder.success(((org.spongepowered.api.data.value.Value.Mutable) manipulator.getValue((Key) key).get()).asImmutable());
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult bridge$removeCustom(final Key<?> key) {
        final Iterator<Mutable> iterator = this.impl$manipulators.iterator();
        while (iterator.hasNext()) {
            final Mutable manipulator = iterator.next();
            if (manipulator.getKeys().size() == 1 && manipulator.supports(key)) {
                iterator.remove();
                this.bridge$removeCustomFromNBT(manipulator);
                return DataTransactionResult.builder()
                    .replace(manipulator.getValues())
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public void bridge$addFailedData(final ImmutableList<DataView> failedData) {
        this.impl$failedData.addAll(failedData);
    }

    @Override
    public List<DataView> bridge$getFailedData() {
        return this.impl$failedData;
    }

    @Override
    public DataTransactionResult bridge$offerCustom(Mutable manipulator, MergeFunction function) {
        throw new MissingImplementationException("CustomDataHolderMixin", "bridge$offerCustom");
    }

    @Override
    public <T extends Mutable> Optional<T> bridge$getCustom(Class<T> customClass) {
        throw new MissingImplementationException("CustomDataHolderMixin", "bridge$getCustom");
    }

    @Override
    public DataTransactionResult bridge$removeCustom(Class<? extends Mutable> customClass) {
        throw new MissingImplementationException("CustomDataHolderMixin", "bridge$removeCustom");
    }

    @Override
    public void bridge$removeCustomFromNBT(Mutable manipulator) {
        if (this instanceof DataCompoundHolder) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
            if (spongeData.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
                final ListNBT tagList = spongeData.getList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
                if (!tagList.isEmpty()) {
                    throw new MissingImplementationException("CustomDataHolderMixin", "bridge$removeCustomFromNBT");
// TODO
//                    final CatalogKey key = DataUtil.getRegistrationFor(manipulator).getKey();
//                    for (int i = 0; i < tagList.size(); i++) {
//                        final CompoundNBT tag = tagList.getCompound(i);
//                        if (key.toString().equals(tag.getString(Constants.Sponge.MANIPULATOR_ID))) {
//                            tagList.remove(i);
//                            break;
//                        }
//                        final String dataClass = tag.getString(Constants.Sponge.CUSTOM_DATA_CLASS);
//                        if (dataClass.equalsIgnoreCase(manipulator.getClass().getName())) {
//                            tagList.remove(i);
//                            break;
//                        }
//                    }

                }
            }
        }
    }
}
