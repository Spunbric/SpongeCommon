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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.MerchantOffer;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferListMutator;
import org.spongepowered.api.item.merchant.VillagerRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.merchant.villager.VillagerEntity;

/*
 * Basically, until Forge figures out their VillagerRegistry stuff, we can only hope to
 * make this useful by enforcing generic villager registrations ourselves.
 * The related Forge PR: https://github.com/MinecraftForge/MinecraftForge/pull/2337
 *
 * Note: This registry is being used by MixinVillager in common as Forge doesn't
 * currently change it.
 */
public final class SpongeVillagerRegistry implements VillagerRegistry {

    public static SpongeVillagerRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<Profession, Multimap<Integer, TradeOfferListMutator>> careerGeneratorMap = new HashMap<>();

    SpongeVillagerRegistry() {
    }

    @Override
    public Multimap<Integer, TradeOfferListMutator> getTradeOfferLevelMap(final Profession career) {
        final Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(checkNotNull(career, "Career cannot be null!"));
        if (multimap == null) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.copyOf(multimap);
    }

    @Override
    public VillagerRegistry addMutator(final Profession career, final int level, final TradeOfferListMutator generator) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generator, "Generator cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, 3);
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.put(level, generator);
        return this;
    }

    @Override
    public VillagerRegistry addMutators(final Profession career, final int level, final TradeOfferListMutator generator, final TradeOfferListMutator... generators) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generator, "Generator cannot be null!");
        checkNotNull(generators, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        final List<TradeOfferListMutator> list = new ArrayList<>();
        list.add(generator);
        for (final TradeOfferListMutator element : generators) {
            list.add(checkNotNull(element, "TradeOfferListMutator cannot be null!"));
        }
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, list.size());
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.putAll(level, list);
        return this;
    }

    @Override
    public VillagerRegistry setMutators(final Profession career, final int level, final List<TradeOfferListMutator> generators) {
        checkArgument(level > 0, "Career level must be at least greater than zero!");
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generators, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap == null) {
            multimap = ArrayListMultimap.create(3, generators.size());
            this.careerGeneratorMap.put(career, multimap);
        }
        multimap.replaceValues(level, generators);
        return this;
    }

    @Override
    public VillagerRegistry setMutators(final Profession career, final Multimap<Integer, TradeOfferListMutator> generatorMap) {
        checkNotNull(career, "Career cannot be null!");
        checkNotNull(generatorMap, "Generators cannot be null!");
        Multimap<Integer, TradeOfferListMutator> multimap = this.careerGeneratorMap.get(career);
        if (multimap != null) {
            multimap.clear();
        }
        multimap = ArrayListMultimap.create(generatorMap);
        this.careerGeneratorMap.put(career, multimap);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void populateOffers(final Merchant merchant, final List<MerchantOffer> currentOffers,
        final VillagerData data, final Random random) {
        populateOffers(merchant, (List<TradeOffer>) (List<?>) currentOffers, (Profession) data.getProfession(),
            data.getLevel(), random);
    }

    private static TradeOfferListMutator generatorFor(final VillagerTrades.ITrade iTradeList) {
        return (TradeOfferListMutator) iTradeList;
    }

    static final class Holder {
         static final SpongeVillagerRegistry INSTANCE = new SpongeVillagerRegistry();
    }
}
