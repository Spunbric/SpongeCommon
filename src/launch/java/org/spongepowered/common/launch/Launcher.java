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
package org.spongepowered.common.launch;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.Blackboard;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.util.PluginMetadataHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Launcher {

    private static Launcher INSTANCE;

    private final Logger logger;
    private final PluginEnvironment pluginEnvironment;
    private final SpongePluginManager pluginManager;
    private final List<PluginContainer> launcherPlugins;
    private PluginContainer minecraftPlugin, apiPlugin, commonPlugin;

    protected Launcher(SpongePluginManager pluginManager) {
        this.logger = LogManager.getLogger("Sponge");
        this.pluginEnvironment = new PluginEnvironment();
        this.pluginManager = pluginManager;
        this.launcherPlugins = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <L extends Launcher> L getInstance() {
        return (L) Launcher.INSTANCE;
    }

    public static void setInstance(Launcher instance) {
        if (Launcher.INSTANCE != null) {
            throw new RuntimeException("Attempt made to re-set launcher instance!");
        }

        Launcher.INSTANCE = Preconditions.checkNotNull(instance);
    }

    public abstract boolean isVanilla();

    public final Logger getLogger() {
        return this.logger;
    }

    public final PluginEnvironment getPluginEnvironment() {
        return this.pluginEnvironment;
    }

    public final SpongePluginManager getPluginManager() {
        return this.pluginManager;
    }

    public final Injector getPlatformInjector() {
        return this.pluginEnvironment.getBlackboard().get(PluginKeys.PARENT_INJECTOR).get();
    }

    public abstract Stage getInjectionStage();

    public final boolean isDeveloperEnvironment() {
        return this.getInjectionStage() == Stage.DEVELOPMENT;
    }

    public final PluginContainer getMinecraftPlugin() {
        if (this.minecraftPlugin == null) {
            this.minecraftPlugin = this.pluginManager.getPlugin("minecraft").orElse(null);

            if (this.minecraftPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Minecraft, this is a serious issue!");
            }
        }

        return this.minecraftPlugin;
    }

    public final PluginContainer getApiPlugin() {
        if (this.apiPlugin == null) {
            this.apiPlugin = this.pluginManager.getPlugin("spongeapi").orElse(null);

            if (this.apiPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeAPI, this is a serious issue!");
            }
        }

        return this.apiPlugin;
    }

    public final PluginContainer getCommonPlugin() {
        if (this.commonPlugin == null) {
            this.commonPlugin = this.pluginManager.getPlugin("sponge").orElse(null);

            if (this.commonPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Sponge, this is a serious issue!");
            }
        }

        return this.commonPlugin;
    }

    public abstract PluginContainer getPlatformPlugin();

    public final List<PluginContainer> getLauncherPlugins() {
        if (this.launcherPlugins.isEmpty()) {
            this.launcherPlugins.add(this.getMinecraftPlugin());
            this.launcherPlugins.add(this.getApiPlugin());
            this.launcherPlugins.add(this.getCommonPlugin());
            this.launcherPlugins.add(this.getPlatformPlugin());
        }

        return this.launcherPlugins;
    }

    protected void onLaunch(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories, final String[] args) {
        this.populateBlackboard(pluginSpiVersion, baseDirectory, pluginDirectories);
        this.createInternalPlugins();
    }

    protected void populateBlackboard(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories) {
        final Blackboard blackboard = this.getPluginEnvironment().getBlackboard();
        blackboard.getOrCreate(PluginKeys.VERSION, () -> pluginSpiVersion);
        blackboard.getOrCreate(PluginKeys.BASE_DIRECTORY, () -> baseDirectory);
        blackboard.getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> pluginDirectories);
    }

    private void createInternalPlugins() {
        final Path gameDirectory = this.pluginEnvironment.getBlackboard().get(PluginKeys.BASE_DIRECTORY).get();
        try {
            final Collection<PluginMetadata> read = PluginMetadataHelper.builder().build().read(Launcher.class.getResourceAsStream("/META-INF/plugins.json"));
            for (final PluginMetadata metadata : read) {
                this.pluginManager.addPlugin(new DummyPluginContainer(metadata, gameDirectory, this.logger, this));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load metadata information for the common implementation! This should be impossible!");
        }

        this.createPlatformPlugins(gameDirectory);
    }

    protected abstract void createPlatformPlugins(final Path gameDirectory);

    public final void auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}
