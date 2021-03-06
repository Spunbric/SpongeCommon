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
package org.spongepowered.common.mixin.inventory.api.equipable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.api.mcp.entity.LivingEntityMixin_API;

import java.util.Optional;

import javax.annotation.Nullable;

// All living implementors of Equipable
@Mixin({PlayerEntity.class,
        ArmorStandEntity.class,
        MobEntity.class})
public abstract class EquippableMixin_API implements Equipable {

    // TODO can we implement canEquip?
    // We might want to allow plugins to set any item
    // but we should least expose checks if an item can be equipped normally

    @Override
    public boolean canEquip(final EquipmentType type) {
        return true;
    }

    @Override
    public boolean canEquip(final EquipmentType type, @Nullable final ItemStack equipment) {
        return true;
    }

    @Override
    public Optional<ItemStack> getEquipped(final EquipmentType type) {
        if (type instanceof SpongeEquipmentType) {
            final EquipmentSlotType[] slots = ((SpongeEquipmentType) type).getSlots();
            if (slots.length != 1) {
                throw new IllegalArgumentException("Only EquipmentTypes for a single Slot are possible");
            }

            final net.minecraft.item.ItemStack nmsItem = ((LivingEntity) (Object) this).getItemStackFromSlot(slots[0]);
            return Optional.of(ItemStackUtil.fromNative(nmsItem));
        }
        return Optional.empty();
    }

    @Override
    public boolean equip(final EquipmentType type, @Nullable final ItemStack equipment) {
        if (type instanceof SpongeEquipmentType) {
            EquipmentSlotType[] slots = ((SpongeEquipmentType) type).getSlots();
            if (slots.length == 0) {
                slots = EquipmentSlotType.values();
            }
            for (final EquipmentSlotType slot : slots) {
                // TODO check if canEquip?
                ((LivingEntity) (Object) this).setItemStackToSlot(slot, ItemStackUtil.toNative(equipment));
                return true;
            }
        }
        return false;
    }

}
