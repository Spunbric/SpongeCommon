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
package org.spongepowered.test;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Client;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("test")
public final class TestPlugin {

    private final Logger logger;

    @Inject
    public TestPlugin(final Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onConstruct(final ConstructPluginEvent event) {
        this.logger.info("Constructed: '{}'", event.getPlugin().toString());
        final ResourceKey tester = ResourceKey.of(event.getPlugin(), "tester");
        this.logger.info(tester);
    }

    @Listener
    public void onProvideService(final ProvideServiceEvent<WhitelistService> event) {
        this.logger.info(event);
        event.suggest(TestWhitelistService::new);
    }

    @Listener
    public void onStartingServer(final StartingEngineEvent<Server> event) {
        this.logger.info("Starting engine '{}'", event.getEngine());
    }

    @Listener
    public void onStartingClient(final StartingEngineEvent<Client> event) {
        this.logger.info("Starting engine '{}'", event.getEngine());
    }

    @Listener
    public void onStoppingServer(final StoppingEngineEvent<Server> event) {
        this.logger.info("Stopping engine '{}'", event.getEngine());
    }
}
