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

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.registry.CatalogRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.registry.builtin.sponge.EventContextKeyStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.ItemSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeCatalogRegistry implements CatalogRegistry {

    private final Map<Class<CatalogType>, Map<String, Supplier<CatalogType>>> suppliers;
    private final Map<ResourceKey, Registry<CatalogType>> registries;
    private final Map<Class<CatalogType>, Registry<CatalogType>> registriesByType;

    public SpongeCatalogRegistry() {
        this.suppliers = new IdentityHashMap<>();
        this.registries = new Object2ObjectOpenHashMap<>();
        this.registriesByType = new IdentityHashMap<>();
    }

    @Override
    public <T extends CatalogType, E extends T> Supplier<E> provideSupplier(final Class<T> catalogClass, final String suggestedId) {
        Preconditions.checkNotNull(suggestedId);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.get(catalogClass);
        if (catalogSuppliers == null) {
            throw new UnknownTypeException(String.format("Supplier for type '%s' has not been registered!", catalogClass));
        }

        final Supplier<CatalogType> catalogSupplier = catalogSuppliers.get(suggestedId.toLowerCase());
        if (catalogSupplier == null) {
            throw new UnknownTypeException(String.format("Supplier for type '%s' with id '%s' has not been registered!", catalogClass, suggestedId));
        }

        return (Supplier<E>) (Object) catalogSupplier;
    }

    @Override
    public <T extends CatalogType> Optional<T> get(final Class<T> typeClass, final ResourceKey key) {
        Preconditions.checkNotNull(key);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Optional.empty();
        }

        return (Optional<T>) registry.getValue((ResourceLocation) (Object) key);
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Collections.emptyList();
        }
        return (Collection<T>) (Object) Collections.unmodifiableList(Arrays.asList(((SimpleRegistryAccessor) registry).accessor$getValues()));
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) Arrays.stream(((SimpleRegistryAccessor) registry).accessor$getValues());
        }

        return stream;
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllFor(final Class<T> typeClass, final String namespace) {
        Preconditions.checkNotNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final List<T> types = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, Object> entry : ((SimpleRegistryAccessor) registry).accessor$getRegistryObjects().entrySet()) {
            if (entry.getKey().getNamespace().equals(namespace)) {
                types.add((T) entry.getValue());
            }
        }

        return types;
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllFor(final Class<T> typeClass, final String namespace) {
        Preconditions.checkNotNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) ((SimpleRegistryAccessor) registry).accessor$getRegistryObjects()
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().getNamespace().equals(namespace))
                .map(Map.Entry::getValue);
        }

        return stream;
    }

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerSupplier(final Class<E> catalogClass, final String suggestedId, Supplier<E> supplier) {
        Preconditions.checkNotNull(supplier);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.computeIfAbsent((Class<CatalogType>) (Object) catalogClass, k -> new Object2ObjectArrayMap<>());
        if (catalogSuppliers.containsKey(suggestedId)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' with id '%s' has a supplier already registered!", catalogClass,
                suggestedId));
        }

        catalogSuppliers.put(suggestedId, (Supplier<CatalogType>) (Object) supplier);
        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key) {
        Preconditions.checkNotNull(key);

        return this.registerRegistry(catalogClass, key, null, false);
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, @Nullable final Supplier<Set<T>> defaultsSupplier, final boolean generateSuppliers) {
        Preconditions.checkNotNull(key);

        if (this.registries.get(key) != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered!", catalogClass));
        }

        final SimpleRegistry<T> registry = new SimpleRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(catalogType -> {
                registry.register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(catalogType.getKey().getValue(), () -> catalogType);
                }
            });
        }
        return this;
    }

    public SpongeCatalogRegistry registerRegistry(final Class<CatalogType> catalogClass, final ResourceKey key, final Registry<CatalogType> registry) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(registry);

        this.registries.put(key, registry);
        this.registriesByType.put(catalogClass, registry);
        return this;
    }

    private <T extends CatalogType> SpongeCatalogRegistry generateCallbackRegistry(Class<T> catalogClass, ResourceKey key, BiConsumer<ResourceLocation, T> callback) {
        Preconditions.checkNotNull(key);

        final Registry<CatalogType> registry = this.registries.putIfAbsent(key, (Registry<CatalogType>) new CallbackRegistry<>(callback));
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered!", catalogClass));
        }
        this.registriesByType.put((Class<CatalogType>) catalogClass, registry);

        return this;
    }

    private <T extends CatalogType, U> SpongeCatalogRegistry registerMappedRegistry(Class<T> catalogClass, ResourceKey key, @Nullable Supplier<Set<Tuple<T, U>>> defaultsSupplier, boolean generateSuppliers) {
        Preconditions.checkNotNull(key);

        if (this.registries.containsKey(key)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered!", catalogClass));
        }

        final MappedRegistry<T, U> registry = new MappedRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(kv -> {
                registry.register((ResourceLocation) (Object) kv.getFirst().getKey(), kv.getFirst());
                registry.registerMapping(kv.getFirst(), kv.getSecond());

                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(key.getValue(), kv::getFirst);
                }
            });
        }
        return this;
    }

    public <T extends CatalogType, R extends Registry<T>> @Nullable R getRegistry(Class<T> catalogClass) {
        return (R) this.registriesByType.get(catalogClass);
    }

    public <T extends CatalogType, R extends Registry<T>> @NonNull R requireRegistry(Class<T> catalogClass) {
        final R registry = this.getRegistry(catalogClass);
        if (registry == null) {
            throw new IllegalArgumentException("No registry is registered for " + catalogClass);
        }
        return registry;
    }

    public <C extends CatalogType> C registerCatalog(C catalogType) {
        Preconditions.checkNotNull(catalogType);

        final Registry<C> registry = (Registry<C>) this.registriesByType.get(catalogType.getClass());
        if (registry == null) {
            throw new UnknownTypeException(String.format("Catalog '%s' with id '%s' has no registry registered!", catalogType.getClass(), catalogType.getKey()));
        }

        return ((SimpleRegistry<C>) registry).register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
    }

    /**
     * Only specify lines of registries that are not found in {@link Registry}.
     */
    public void registerDefaultRegistries() {

        // TODO 1.14 - We'll take on a case by case basis if any mods are extending/replacing Enum values and therefore breaks this. Otherwise it will
        // TODO 1.14 - get to the point of insanity if literally every enum in the game becomes hardcoded lines that we have to map out...

        // TODO 1.14 - Text stuff needs to be registered as soon as possible in the engine, needed by BossBarOverlay (as an example)
//        this
//            .generateMappedRegistry(TextColor.class, ResourceKey.minecraft("text_color"), TextColorStreamGenerator.stream(), true)
//            .generateMappedRegistry(TextStyle.Type.class, ResourceKey.minecraft("text_style"), TextStyleTypeStreamGenerator.stream(), true)
//            .generateRegistry(TextSerializer.class, ResourceKey.minecraft("text_serializer"), TextSerializerStreamGenerator.stream(), true)
//        ;

        this
//            .generateRegistry(AccountDeletionResultType.class, ResourceKey.sponge("account_deletion_result_type"), AccountDeletionResultTypeStreamGenerator.stream(), true)
//            .registerRegistry(Advancement.class, ResourceKey.minecraft("advancement"))
//            .registerRegistry(AdvancementTree.class, ResourceKey.minecraft("advancement_tree"))
//            .generateRegistry(AdvancementType.class, ResourceKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true)
//            .generateRegistry(ArmorMaterial.class, ResourceKey.minecraft("armor_material"), Arrays.stream(net.minecraft.item.ArmorMaterial.values()), true)
//            .generateRegistry(BanType.class, ResourceKey.minecraft("ban_type"), BanTypeStreamGenerator.stream(), true)
//            .generateRegistry(BannerPatternShape.class, ResourceKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true)
//            .generateRegistry(BossBarOverlay.class, ResourceKey.minecraft("boss_bar_overlay"), Arrays.stream(BossInfo.Overlay.values()), true)
//            .generateRegistry(BossBarColor.class, ResourceKey.minecraft("boss_bar_color"), Arrays.stream(BossInfo.Color.values()), true)
//            .generateRegistry(BodyPart.class, ResourceKey.minecraft("body_part"), BodyPartStreamGenerator.stream(), true)
//            .generateRegistry(ChatType.class, ResourceKey.minecraft("chat_type"), Arrays.stream(net.minecraft.util.text.ChatType.values()), true)
//            .generateRegistry(ChatVisibility.class, ResourceKey.minecraft("chat_visibility"), Arrays.stream(net.minecraft.entity.player.ChatVisibility.values()), true)
//            .generateRegistry(ChestAttachmentType.class, ResourceKey.minecraft("chest_attachment_type"), Arrays.stream(ChestType.values()), true)
//            .generateRegistry(CollisionRule.class, ResourceKey.minecraft("collision_rule"), Arrays.stream(Team.CollisionRule.values()), true)
//            .generateRegistry(ComparatorMode.class, ResourceKey.minecraft("comparator_mode"), Arrays.stream(net.minecraft.state.properties.ComparatorMode.values()), true)
//            .generateRegistry(DamageType.class, ResourceKey.sponge("damage_type"), DamageTypeStreamGenerator.stream(), true)
//            .generateRegistry(Difficulty.class, ResourceKey.minecraft("difficulty"), Arrays.stream(net.minecraft.world.Difficulty.values()), true)
//            .generateRegistry(DismountType.class, ResourceKey.minecraft("dismount_type"), DismountTypeStreamGenerator.stream(), true)
//            .generateRegistry(DragonPhaseType.class, ResourceKey.minecraft("dragon_phase_type"), DragonPhaseTypeStreamGenerator.stream(), true)
//            .generateRegistry(DyeColor.class, ResourceKey.minecraft("dye_color"), Arrays.stream(net.minecraft.item.DyeColor.values()), true)
            .generateRegistry(EventContextKey.class, ResourceKey.sponge("event_context_key"), EventContextKeyStreamGenerator.stream(), true)
//            .generateRegistry(FoxType.class, ResourceKey.minecraft("fox_type"), Arrays.stream(FoxEntity.Type.values()), true)
//            .generateRegistry(GameMode.class, ResourceKey.minecraft("game_mode"), Arrays.stream(GameType.values()), true)
//            .generateRegistry(GoalExecutorType.class, ResourceKey.minecraft("goal_executor_type"), GoalExecutorTypeStreamGenerator.stream(), true)
//            .generateRegistry(HandPreference.class, ResourceKey.minecraft("hand_preference"), Arrays.stream(HandSide.values()), true)
//            .generateRegistry(HandType.class, ResourceKey.minecraft("hand_type"), Arrays.stream(Hand.values()), true)
//            .generateRegistry(Hinge.class, ResourceKey.minecraft("hinge"), Arrays.stream(DoorHingeSide.values()), true)
//            .generateRegistry(InstrumentType.class, ResourceKey.minecraft("instrument_type"), Arrays.stream(NoteBlockInstrument.values()), true)
//            .generateRegistry(MooshroomType.class, ResourceKey.minecraft("mooshroom_type"), Arrays.stream(MooshroomEntity.Type.values()), true)
//            .generateRegistry(MusicDisc.class, ResourceKey.minecraft("music_disc"), MusicDiscStreamGenerator.stream(), true)
//            .generateRegistry(PandaGene.class, ResourceKey.minecraft("panda_gene"), Arrays.stream(PandaEntity.Type.values()), true)
//            .generateRegistry(PhantomPhase.class, ResourceKey.minecraft("phantom_phase"), Arrays.stream(PhantomEntity.AttackPhase.values()), true)
//            .generateRegistry(PickupRule.class, ResourceKey.minecraft("pickup_rule"), Arrays.stream(AbstractArrowEntity.PickupStatus.values()), true)
//            .generateRegistry(PistonType.class, ResourceKey.minecraft("piston_type"), Arrays.stream(net.minecraft.state.properties.PistonType.values()), true)
//            .generateRegistry(PortalAgentType.class, ResourceKey.minecraft("portal_agent_type"), PortalAgentTypeStreamGenerator.stream(), true)
//            .generateRegistry(PortionType.class, ResourceKey.minecraft("portion_type"), Arrays.stream(Half.values()), true)
//            .generateRegistry(RaidStatus.class, ResourceKey.minecraft("raid_status"), Arrays.stream(Raid.Status.values()), true)
//            .generateRegistry(RailDirection.class, ResourceKey.minecraft("rail_direction"), Arrays.stream(RailShape.values()), true)
//            .generateRegistry(SlabPortion.class, ResourceKey.minecraft("slab_portion"), Arrays.stream(SlabType.values()), true)
//            .generateRegistry(SpawnType.class, ResourceKey.sponge("spawn_type"), SpawnTypeStreamGenerator.stream(), true)
//            .generateRegistry(SpellType.class, ResourceKey.minecraft("spell_type"), Arrays.stream(SpellcastingIllagerEntity.SpellType.values()), true)
//            .generateRegistry(StairShape.class, ResourceKey.minecraft("stair_shape"), Arrays.stream(StairsShape.values()), true)
//            .generateRegistry(StructureMode.class, ResourceKey.minecraft("structure_mode"), Arrays.stream(net.minecraft.state.properties.StructureMode.values()), true)
//            .generateRegistry(ToolType.class, ResourceKey.minecraft("tool_type"), Arrays.stream(ItemTier.values()), true)
//            .generateRegistry(TropicalFishShape.class, ResourceKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true)
//            .generateRegistry(WireAttachmentType.class, ResourceKey.minecraft("wire_attachment_type"), Arrays.stream(RedstoneSide.values()), true)
//            .generateRegistry(WoodType.class, ResourceKey.minecraft("wood_type"), WoodTypeStreamGenerator.stream(), true)
//            .generateRegistry(Visibility.class, ResourceKey.minecraft("visibility"), Arrays.stream(Team.Visible.values()), true)
            .registerRegistry(Currency.class, ResourceKey.sponge("currency"))
        ;

//        this
//            .generateMappedRegistry(CatType.class, ResourceKey.minecraft("cat_type"), CatTypeStreamGenerator.stream(), true)
//            .generateMappedRegistry(FireworkShape.class, ResourceKey.minecraft("firework_shape"), FireworkShapeStreamGenerator.stream(), true)
//            .generateMappedRegistry(GoalType.class, ResourceKey.minecraft("goal_type"), GoalTypeStreamGenerator.stream(), true)
//            .generateMappedRegistry(HorseColor.class, ResourceKey.minecraft("horse_color"), HorseColorStreamGenerator.stream(), true)
//            .generateMappedRegistry(HorseStyle.class, ResourceKey.minecraft("horse_style"), HorseStyleStreamGenerator.stream(), true)
//            .generateMappedRegistry(LlamaType.class, ResourceKey.minecraft("llama_type"), LlamaTypeStreamGenerator.stream(), true)
//            .generateMappedRegistry(NotePitch.class, ResourceKey.minecraft("note_pitch"), NotePitchStreamGenerator.stream(), true)
//            .generateMappedRegistry(ParrotType.class, ResourceKey.minecraft("parrot_type"), ParrotTypeStreamGenerator.stream(), true)
//            .generateMappedRegistry(RabbitType.class, ResourceKey.minecraft("rabbit_type"), RabbitTypeStreamGenerator.stream(), true)
//            .generateMappedRegistry(DataTranslator.class, ResourceKey.sponge("data_translator"), DataSerializers.stream(), true)
//            .generateMappedRegistry(DisplaySlot.class, ResourceKey.minecraft("display_slot"), DisplaySlotStreamGenerator.stream(), true)
//        ;
//
//        this.generateCallbackRegistry(DataRegistration.class, ResourceKey.sponge("data_registration"), (key, value) -> SpongeDataManager.getInstance().registerDataRegistration((SpongeDataRegistration) value));

        this.registerDefaultSuppliers();
    }

    /**
     * Only specify lines of registries found in {@link Registry} as these are true Vanilla registries
     */
    private void registerDefaultSuppliers() {

        // TODO 1.14 - This is not right but I don't want this forgotten so here for now
        // TODO 1.14 - Stats are stupid, need to handle them manually

        // Class based/Likely for mods to override
//        BiomeSupplier.registerSuppliers(this);
//        BlockSupplier.registerSuppliers(this);
//        ContainerTypeSupplier.registerSuppliers(this);
//        CriteriaTriggersSupplier.registerSuppliers(this);
//        DimensionTypeSupplier.registerSuppliers(this);
//        EffectSupplier.registerSuppliers(this);
//        EnchantmentSupplier.registerSuppliers(this);
//        EntityTypeSupplier.registerSuppliers(this);
//        FluidSupplier.registerSuppliers(this);
        ItemSupplier.registerSuppliers(this);
//        PaintingTypeSupplier.registerSuppliers(this);
//        ParticleTypeSupplier.registerSuppliers(this);
//        SoundEventSupplier.registerSuppliers(this);
//        TileEntityTypeSupplier.registerSuppliers(this);
//        VillagerProfessionSupplier.registerSuppliers(this);

        // Inventory
//        SpongeQueryTypes.registerSuppliers(this);
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateRegistry(Class<T> catalogClass, ResourceKey key, Stream<E> valueStream, boolean generateSuppliers) {
        this.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), generateSuppliers);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateMappedRegistry(Class<T> catalogClass, ResourceKey key, Stream<Tuple<T, E>> valueStream, boolean generateSuppliers) {
        this.registerMappedRegistry(catalogClass, key, () -> valueStream.collect(Collectors.toSet()), generateSuppliers);
        return this;
    }

}
