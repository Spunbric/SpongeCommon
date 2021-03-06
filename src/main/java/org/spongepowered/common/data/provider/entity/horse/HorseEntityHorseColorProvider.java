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
package org.spongepowered.common.data.provider.entity.horse;

import net.minecraft.entity.passive.horse.HorseEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.data.type.SpongeHorseColor;
import org.spongepowered.common.registry.MappedRegistry;

import java.util.Optional;

public class HorseEntityHorseColorProvider extends GenericMutableDataProvider<HorseEntity, HorseColor> {

    public HorseEntityHorseColorProvider() {
        super(Keys.HORSE_COLOR);
    }

    @Override
    protected Optional<HorseColor> getFrom(HorseEntity dataHolder) {
        final MappedRegistry<HorseColor, Integer> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(HorseColor.class);
        return Optional.of(registry.getReverseMapping(getHorseColor(dataHolder)));
    }

    @Override
    protected boolean set(HorseEntity dataHolder, HorseColor value) {
        final int style = HorseEntityHorseStyleProvider.getHorseStyle(dataHolder);
        dataHolder.setHorseVariant(((SpongeHorseColor) value).getMetadata() | style);
        return true;
    }

    public static int getHorseColor(HorseEntity dataHolder) {
        return dataHolder.getHorseVariant() & 0xFF;
    }

}
