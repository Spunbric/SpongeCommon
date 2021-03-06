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
package org.spongepowered.common.inventory.util;

import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.CraftingGridInventoryAdapter;
import org.spongepowered.common.inventory.custom.CarriedWrapperInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingGridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.plugin.PluginContainer;

import javax.annotation.Nullable;
import java.util.Optional;

public final class InventoryUtil {

    private InventoryUtil() {}

    @SuppressWarnings("rawtypes")
    public static CraftingGridInventory toSpongeInventory(CraftingInventory inv) {
        CraftingGridInventoryLens lens = new CraftingGridInventoryLens(0, inv.getWidth(), inv.getHeight(), BasicSlotLens::new);

        return new CraftingGridInventoryAdapter((Fabric) inv, lens);
    }


    @SuppressWarnings("unchecked")
    public static <C extends IInventory> C toNativeInventory(Inventory inv) {
        Fabric fabric = ((CraftingGridInventoryAdapter) inv).inventoryAdapter$getFabric();
        for (Object inventory : fabric.fabric$allInventories()) {
            if (inventory instanceof CraftingInventory) {
                return (C) inventory;
            }
        }

        // Gather Debug Info...
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid CraftingGridInventory. Could not find InventoryCrafting.\n")
          .append("Fabric was: ")
          .append(fabric.getClass().getSimpleName()).append(" Name: ")
          .append("Viewed:");
        for (Object iInventory : fabric.fabric$allInventories()) {
            sb.append("\n").append(iInventory.getClass().getName());
        }

        throw new IllegalStateException(sb.toString());
    }

    public static Optional<Inventory> getDoubleChestInventory(ChestTileEntity chest) {
        Optional<Chest> connectedChestOptional = ((Chest) chest).getConnectedChest();
        if (!connectedChestOptional.isPresent()) {
            return Optional.empty();
        }

        ChestType chestType = chest.getBlockState().get(ChestBlock.TYPE);
        ChestTileEntity connectedChest = (ChestTileEntity) connectedChestOptional.get();
        // Logic in the instanceof check of ChestBlock.getChestInventory but with exploded ternary operators.
        if (chestType == ChestType.RIGHT) {
            return Optional.of((Inventory) new DoubleSidedInventory(chest, connectedChest));
        } else {
            return Optional.of((Inventory) new DoubleSidedInventory(connectedChest, chest));
        }
    }

    // Utility
    public static Inventory toInventory(IInventory inventory) {
        return toInventory(inventory, null);
    }

    public static Inventory toInventory(Object inventory, @Nullable Object forgeItemHandler) {
        if (forgeItemHandler == null) {
            if (inventory instanceof ChestTileEntity) {
                inventory = getDoubleChestInventory(((ChestTileEntity) inventory)).orElse(((Inventory) inventory));
            }
            if (inventory instanceof Inventory) {
                return ((Inventory) inventory);
            }
        }
        if (forgeItemHandler instanceof Inventory) {
            return ((Inventory) forgeItemHandler);
        }
        return SpongeImplHooks.toInventory(inventory, forgeItemHandler);
    }

    public static InventoryAdapter findAdapter(Object inventory) {
        if (inventory instanceof InventoryAdapter) {
            return ((InventoryAdapter) inventory);
        }
        return SpongeImplHooks.findInventoryAdapter(inventory);
    }


    public static TrackedInventoryBridge forCapture(Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }

    public static PluginContainer getPluginContainer(Object inventory) {
        // TODO maybe caching?
        PluginContainer container;

        if (inventory instanceof CustomInventory) {
            return ((CustomInventory)inventory).getPlugin();
        }

        if (inventory instanceof CarriedInventory) {
            final Optional<?> carrier = ((CarriedInventory<?>) inventory).getCarrier();
            if (carrier.isPresent()) {
                inventory = carrier.get();
            }
        }

        final Object base = inventory;

        if (base instanceof BlockEntity) {
            ResourceKey key = ((BlockEntity) base).getBlock().getType().getKey();
            final String pluginId = key.getNamespace();
            container = Sponge.getPluginManager().getPlugin(pluginId)
                    .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + key.getNamespace() + ":" + key.getValue()));
        } else if (base instanceof Entity) {
            ResourceKey key = ((Entity) base).getType().getKey();
            final String pluginId = key.getNamespace();
            container = Sponge.getPluginManager().getPlugin(pluginId).orElseGet(() -> {
                SpongeCommon.getLogger().debug("Unknown plugin for [{}]", base);
                return SpongeCommon.getMinecraftPlugin();
            });
        } else if (base instanceof SpongeUser) {
            container = SpongeCommon.getMinecraftPlugin();
        } else {
            container = Sponge.getPluginManager().getPlugin(SpongeImplHooks.getModIdFromClass(base.getClass())).orElseGet(() -> {
                SpongeCommon.getLogger().debug("Unknown plugin for [{}]", base);
                return SpongeCommon.getMinecraftPlugin();
            });
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Carrier> CarriedInventory<T> carriedWrapperInventory(net.minecraft.inventory.IInventory inventory, T carrier) {
        return (CarriedInventory<T>) new CarriedWrapperInventory(inventory, carrier);
    }
}
