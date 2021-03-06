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
package org.spongepowered.common.mixin.core.network.rcon;

import net.minecraft.network.rcon.ClientThread;
import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.network.rcon.RConThread;
import net.minecraft.network.rcon.RConUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.rcon.RconConnectionEvent;
import org.spongepowered.api.network.RconConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.network.rcon.ClientThreadBridge;
import org.spongepowered.common.bridge.network.rcon.RConConsoleSourceBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

@Mixin(ClientThread.class)
public abstract class ClientThreadMixin extends RConThread implements ClientThreadBridge {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow private void sendMultipacketResponse(final int p_72655_1_, final String p_72655_2_) throws IOException {}
    @Shadow private void sendResponse(final int p_72654_1_, final int p_72654_2_, final String message) throws IOException {}
    @Shadow private void closeSocket() {}
    @Shadow private void sendLoginFailedResponse() throws IOException {}

    @Shadow private boolean loggedIn;
    @Shadow private Socket clientSocket;
    @Shadow @Final @Mutable private String rconPassword;
    @Shadow @Final @Mutable private byte[] buffer;

    private RConConsoleSource impl$source;

    protected ClientThreadMixin(final IServer p_i45300_1_, final String p_i45300_2_) {
        super(p_i45300_1_, p_i45300_2_);
    }

    @Inject(method = "closeSocket", at = @At("HEAD"))
    private void impl$rconLogoutCallback(final CallbackInfo ci) {
        if (this.loggedIn) {
            SpongeCommon.getServerScheduler().execute(() -> {
                final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
                causeStackManager.pushCause(this);
                causeStackManager.pushCause(this.impl$source);
                final RconConnectionEvent.Disconnect event = SpongeEventFactory.createRconConnectionEventDisconnect(
                        causeStackManager.getCurrentCause(), (RconConnection) this.impl$source);
                SpongeCommon.postEvent(event);
                causeStackManager.popCauses(2);
                return event;
            });
        }
    }

    /**
     * @author Cybermaxke
     * @reason Fix RCON, is completely broken
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public void run() {
        /// Sponge: START
        // Initialize the source
        this.impl$source = new RConConsoleSource(SpongeCommon.getServer());
        ((RConConsoleSourceBridge) this.impl$source).bridge$setConnection((ClientThread) (Object) this);

        // Call the connection event
        final RconConnectionEvent.Connect connectEvent;
        try {
            connectEvent = SpongeCommon.getServerScheduler().execute(() -> {
                final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
                causeStackManager.pushCause(this);
                causeStackManager.pushCause(this.impl$source);
                final RconConnectionEvent.Connect event = SpongeEventFactory.createRconConnectionEventConnect(
                        causeStackManager.getCurrentCause(), (RconConnection) this.impl$source);
                SpongeCommon.postEvent(event);
                causeStackManager.popCauses(2);
                return event;
            }).get();
        } catch (InterruptedException | ExecutionException ignored) {
            this.closeSocket();
            return;
        }
        if (connectEvent.isCancelled()) {
            this.closeSocket();
            return;
        }
        /// Sponge: END
        /// Sponge: All 'return' operations are replaced by 'break' and
        ///         'closeSocket' is moved out of the loop
        while (true) {
            try {
                if (!this.running) {
                    break;
                }

                final BufferedInputStream bufferedinputstream = new BufferedInputStream(this.clientSocket.getInputStream());
                final int i = bufferedinputstream.read(this.buffer, 0, this.buffer.length);
                /// Sponge: START
                if (i == -1) {
                    // We read end of file, which means that this socket should be closed.
                    // Break to exit the loop. As the read didn't advance anything, it will
                    // continue to return EOF until we manually exit the loop.
                    break;
                }
                /// Sponge: END

                if (10 <= i) {
                    int j = 0;
                    final int k = RConUtils.getBytesAsLEInt(this.buffer, 0, i);

                    if (k != i - 4) {
                        break;
                    }

                    j += 4;
                    final int l = RConUtils.getBytesAsLEInt(this.buffer, j, i);
                    j += 4;
                    final int i1 = RConUtils.getRemainingBytesAsLEInt(this.buffer, j);
                    j += 4;

                    switch (i1) {
                        case 2:
                            if (this.loggedIn) {
                                final String command = RConUtils.getBytesAsString(this.buffer, j, i);
                                try {
                                    /// Sponge: START
                                    // Execute the command on the main thread and wait for it
                                    SpongeCommon.getServerScheduler().execute(() -> {
                                        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
                                        // Only add the RemoteConnection here, the RconSource
                                        // will be added by the command manager
                                        causeStackManager.pushCause(this);
                                        SpongeCommon.getServer().getCommandManager().handleCommand(this.impl$source.getCommandSource(), command);
                                        causeStackManager.popCause();
                                    }).get();
                                    final String logContents = this.impl$source.getLogContents();
                                    this.impl$source.resetLog();
                                    this.sendMultipacketResponse(l, logContents);
                                    /// Sponge: END
                                } catch (Exception exception) {
                                    this.sendMultipacketResponse(l, "Error executing: " + command + " (" + exception.getMessage() + ")");
                                }
                                continue;
                            }
                            this.sendLoginFailedResponse();
                            break; // Sponge: 'continue' -> 'break', disconnect when a invalid operation is requested
                        case 3:
                            final String password = RConUtils.getBytesAsString(this.buffer, j, i);
                            if (!password.isEmpty() && password.equals(this.rconPassword)) {
                                /// Sponge: START
                                final RconConnectionEvent.Auth event = SpongeCommon.getServerScheduler().execute(() -> {
                                    final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
                                    causeStackManager.pushCause(this);
                                    causeStackManager.pushCause(this.impl$source);
                                    final RconConnectionEvent.Auth event1 = SpongeEventFactory.createRconConnectionEventAuth(
                                            causeStackManager.getCurrentCause(), (RconConnection) this.impl$source);
                                    SpongeCommon.postEvent(event1);
                                    causeStackManager.popCauses(2);
                                    return event1;
                                }).get();
                                if (!event.isCancelled()) {
                                    this.loggedIn = true;
                                    this.sendResponse(l, 2, "");
                                    continue;
                                }
                                /// Sponge: END
                            }

                            this.loggedIn = false;
                            this.sendLoginFailedResponse();
                            break; // Sponge: 'continue' -> 'break', disconnect if login failed
                        default:
                            this.sendMultipacketResponse(l, String.format("Unknown request %s", Integer.toHexString(i1)));
                            break; // Sponge: 'continue' -> 'break', disconnect when a invalid operation is requested
                    }
                }
            } catch (IOException e) {
                break;
            } catch (Exception e) {
                LOGGER.error("Exception whilst parsing RCON input", e);
                break;
            }
        }
        this.closeSocket();
    }

    @Override
    public boolean bridge$getLoggedIn() {
        return this.loggedIn;
    }

    @Override
    public void bridge$setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
