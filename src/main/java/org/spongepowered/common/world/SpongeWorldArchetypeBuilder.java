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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.gen.GeneratorType;
import org.spongepowered.api.world.gen.GeneratorTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.api.world.teleport.PortalAgentTypes;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.util.Functional;

import javax.annotation.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public final class SpongeWorldArchetypeBuilder implements WorldArchetype.Builder {

    private static final Random RANDOM = new Random();

    private ResourceKey key;
    private Supplier<? extends DimensionType> dimensionType = Functional.throwNull();
    private Supplier<? extends GeneratorType> generatorType = Functional.throwNull();
    private Supplier<? extends Difficulty> difficulty = Functional.throwNull();
    private Supplier<? extends GameMode> gameMode = Functional.throwNull();
    private Supplier<? extends SerializationBehavior> serializationBehavior = Functional.throwNull();
    private Supplier<? extends PortalAgentType> portalAgentType = Functional.throwNull();
    private long seed;
    private boolean areStructuresEnabled;
    private boolean hardcore;
    private boolean enabled;
    private boolean loadOnStartup;
    @Nullable private Boolean keepSpawnLoaded;
    private boolean generateSpawnOnLoad;
    private boolean pvpEnabled;
    private boolean commandsEnabled;
    private boolean generateBonusChest;
    @Nullable private DataContainer generatorSettings;
    private boolean randomizedSeed;

    public SpongeWorldArchetypeBuilder() {
        this.reset();
    }

    @Override
    public WorldArchetype.Builder key(ResourceKey key) {
        this.key = checkNotNull(key);
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder seed(long seed) {
        this.seed = seed;
        this.randomizedSeed = false;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder randomSeed() {
        this.seed = SpongeWorldArchetypeBuilder.RANDOM.nextLong();
        this.randomizedSeed = true;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder gameMode(GameMode gameMode) {
        checkNotNull(gameMode);
        this.gameMode = () -> gameMode;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generatorType(GeneratorType type) {
        checkNotNull(type);
        this.generatorType = () -> type;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder dimensionType(DimensionType type) {
        checkNotNull(type);
        this.dimensionType = () -> type;
        return this;
    }

    @Override
    public WorldArchetype.Builder difficulty(Difficulty difficulty) {
        checkNotNull(difficulty);
        this.difficulty = () -> difficulty;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateStructures(boolean state) {
        this.areStructuresEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder hardcore(boolean state) {
        this.hardcore = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder enabled(boolean state) {
        this.enabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder loadOnStartup(boolean state) {
        this.loadOnStartup = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder keepSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateSpawnOnLoad(boolean state) {
        this.generateSpawnOnLoad = state;
        return this;
    }

    @Override
    public WorldArchetype.Builder portalAgent(PortalAgentType type) {
        checkNotNull(type);
        this.portalAgentType = () -> type;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder pvpEnabled(boolean state) {
        this.pvpEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder commandsEnabled(boolean state) {
        this.commandsEnabled = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder generateBonusChest(boolean state) {
        this.generateBonusChest = state;
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder serializationBehavior(SerializationBehavior behavior) {
        checkNotNull(behavior);
        this.serializationBehavior = () -> behavior;
        return this;
    }

    @Override
    public WorldArchetype.Builder generatorSettings(DataContainer generatorSettings) {
        this.generatorSettings = checkNotNull(generatorSettings);
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(WorldArchetype value) {
        checkNotNull(value);
        this.dimensionType(value.getDimensionType());
        this.generatorType(value.getGeneratorType());
        this.gameMode(value.getGameMode());
        this.difficulty(value.getDifficulty());
        this.serializationBehavior(value.getSerializationBehavior());
        this.seed = value.getSeed();
        this.randomizedSeed = value.isSeedRandomized();
        this.areStructuresEnabled = value.areStructuresEnabled();
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.portalAgent(value.getPortalAgentType());
        this.generatorSettings = value.getGeneratorSettings();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder from(WorldProperties value) {
        checkNotNull(value);
        this.dimensionType(value.getDimensionType());
        this.generatorType(value.getGeneratorType());
        this.gameMode(value.getGameMode());
        this.difficulty(value.getDifficulty());
        this.serializationBehavior(value.getSerializationBehavior());
        this.seed = value.getSeed();
        this.randomizedSeed = false;
        this.areStructuresEnabled = value.areStructuresEnabled();
        this.hardcore = value.isHardcore();
        this.enabled = value.isEnabled();
        this.loadOnStartup = value.doesLoadOnStartup();
        this.keepSpawnLoaded = value.doesKeepSpawnLoaded();
        this.pvpEnabled = value.isPVPEnabled();
        this.generateSpawnOnLoad = value.doesGenerateSpawnOnLoad();
        this.commandsEnabled = value.areCommandsEnabled();
        this.generateBonusChest = value.doesGenerateBonusChest();
        this.portalAgent(value.getPortalAgentType());
        this.generatorSettings = value.getGeneratorSettings();
        return this;
    }

    @Override
    public SpongeWorldArchetypeBuilder reset() {
        this.dimensionType = DimensionTypes.OVERWORLD;
        this.generatorType = GeneratorTypes.DEFAULT;
        this.gameMode = GameModes.SURVIVAL;
        this.difficulty = Difficulties.NORMAL;
        this.serializationBehavior = SerializationBehaviors.AUTOMATIC;
        this.seed = SpongeWorldArchetypeBuilder.RANDOM.nextLong();
        this.randomizedSeed = true;
        this.areStructuresEnabled = true;
        this.hardcore = false;
        this.enabled = true;
        this.loadOnStartup = true;
        this.keepSpawnLoaded = null;
//        this.generateSpawnOnLoad = ((DimensionTypeBridge) this.dimensionType.get()).bridge$getDimensionConfig().getConfig().getWorld().getGenerateSpawnOnLoad();
        this.generatorSettings = DataContainer.createNew();
        this.pvpEnabled = true;
        this.commandsEnabled = true;
        this.generateBonusChest = false;
        this.portalAgentType = PortalAgentTypes.DEFAULT;
        this.generatorSettings = this.generatorType.get().getDefaultGeneratorSettings();
        return this;
    }

    @Override
    public WorldArchetype build() throws IllegalArgumentException {
        final WorldSettings settings = new WorldSettings(this.seed, (GameType) (Object) this.gameMode, this.areStructuresEnabled, this.hardcore,
            (WorldType) this.generatorType);
        final WorldSettingsBridge settingsBridge = (WorldSettingsBridge) (Object) settings;
        settingsBridge.bridge$setKey(this.key);
        settingsBridge.bridge$setDimensionType(this.dimensionType.get());
        settingsBridge.bridge$setDifficulty(this.difficulty.get());
        settingsBridge.bridge$setSerializationBehavior(this.serializationBehavior.get());
        settingsBridge.bridge$setGeneratorSettings(this.generatorSettings);
        settingsBridge.bridge$setEnabled(this.enabled);
        settingsBridge.bridge$setLoadOnStartup(this.loadOnStartup);
        settingsBridge.bridge$setKeepSpawnLoaded(this.keepSpawnLoaded);
        settingsBridge.bridge$setGenerateSpawnOnLoad(this.generateSpawnOnLoad);
        settingsBridge.bridge$setPVPEnabled(this.pvpEnabled);
        settingsBridge.bridge$setCommandsEnabled(this.commandsEnabled);
        settingsBridge.bridge$setGenerateBonusChest(this.generateBonusChest);
        settingsBridge.bridge$setPortalAgentType(this.portalAgentType.get());
        settingsBridge.bridge$setRandomSeed(this.randomizedSeed);
        settingsBridge.bridge$setGeneratorSettings(this.generatorSettings);

        return (WorldArchetype) (Object) settings;
    }
}
