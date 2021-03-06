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
package org.spongepowered.common.mixin.core.server;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;

import java.net.URISyntaxException;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends RecursiveEventLoop<TickDelayedTask> implements MinecraftServerBridge, SubjectBridge,
        CommandSourceProviderBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final protected IChunkStatusListenerFactory chunkStatusListenerFactory;
    @Shadow private int tickCounter;
    @Shadow public abstract boolean shadow$isDedicatedServer();
    @Shadow public abstract boolean shadow$isServerRunning();
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract Iterable<ServerWorld> shadow$getWorlds();
    @Shadow @Final protected Thread serverThread;

    @Shadow public abstract CommandSource getCommandSource();

    @Nullable private ResourcePack impl$resourcePack;
    private boolean impl$enableSaving = true;

    public MinecraftServerMixin(String name) {
        super(name);
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate bridge$permDefault(String permission) {
        return Tristate.TRUE;
    }

    @Inject(method = "startServerThread", at = @At("HEAD"))
    private void impl$prepareServerPhaseTracker(CallbackInfo ci) {
        try {
            PhaseTracker.SERVER.setThread(this.serverThread);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the server PhaseTracker!");
        }
    }

//    /**
//     * @author Zidane - Minecraft 1.14.4
//     * @reason Sponge rewrites the method to use the Sponge {@link WorldManager} to load worlds,
//     * migrating old worlds, upgrading worlds to our standard, and configuration loading.
//     */
//    @Overwrite
//    public void loadAllWorlds(String directoryName, String levelName, long seed, WorldType type, JsonElement generatorOptions) {
//        SpongeCommon.getWorldManager().loadAllWorlds((MinecraftServer) (Object) this, directoryName, levelName, seed, type, generatorOptions);
//    }

//    @Inject(method = "loadInitialChunks", at = @At("HEAD"), cancellable = true)
//    private void impl$cancelLoadInitialChunks(IChunkStatusListener p_213186_1_, CallbackInfo ci) {
//        ci.cancel();
//    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    private void impl$createSpongeResourcePackWrapper(String url, String hash, CallbackInfo ci) {
        if (url.length() == 0) {
            this.impl$resourcePack = null;
        } else {
            try {
                this.impl$resourcePack = SpongeResourcePack.create(url, hash);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ResourcePack bridge$getResourcePack() {
        return this.impl$resourcePack;
    }

    @Override
    public void bridge$setSaveEnabled(boolean enabled) {
        this.impl$enableSaving = enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void impl$onServerTickStart(CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Override
    public CommandSource bridge$getCommandSource(final Cause cause) {
        return this.getCommandSource();
    }

//    @Inject(method = "tick", at = @At(value = "RETURN"))
//    private void impl$completeTickCheckAnimation(CallbackInfo ci) {
//        final int lastAnimTick = SpongeCommonEventFactory.lastAnimationPacketTick;
//        final int lastPrimaryTick = SpongeCommonEventFactory.lastPrimaryPacketTick;
//        final int lastSecondaryTick = SpongeCommonEventFactory.lastSecondaryPacketTick;
//        if (SpongeCommonEventFactory.lastAnimationPlayer != null) {
//            final ServerPlayerEntity player = SpongeCommonEventFactory.lastAnimationPlayer.get();
//            if (player != null && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
//                final BlockSnapshot blockSnapshot = BlockSnapshot.empty();
//
//                final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player) + 1);
//
//                // Hit non-air block
//                if (result instanceof BlockRayTraceResult) {
//                    return;
//                }
//
//                if (!player.getHeldItemMainhand().isEmpty() && SpongeCommonEventFactory.callInteractItemEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null, blockSnapshot).isCancelled()) {
//                    SpongeCommonEventFactory.lastAnimationPacketTick = 0;
//                    SpongeCommonEventFactory.lastAnimationPlayer = null;
//                    return;
//                }
//
//                SpongeCommonEventFactory.callInteractBlockEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null);
//            }
//            SpongeCommonEventFactory.lastAnimationPlayer = null;
//        }
//        SpongeCommonEventFactory.lastAnimationPacketTick = 0;
//
//        TimingsManager.FULL_SERVER_TICK.stopTiming();
//    }

//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 900))
//    private int getSaveTickInterval(int tickInterval) {
//        if (!this.shadow$isDedicatedServer()) {
//            return tickInterval;
//        } else if (!this.shadow$isServerRunning()) {
//            // Don't autosave while server is stopping
//            return this.tickCounter + 1;
//        }
//
//        final int autoPlayerSaveInterval = SpongeCommon.getGlobalConfigAdapter().getConfig().getWorld().getAutoPlayerSaveInterval();
//        if (autoPlayerSaveInterval > 0 && (this.tickCounter % autoPlayerSaveInterval == 0)) {
//            this.shadow$getPlayerList().saveAllPlayerData();
//        }
//
//        this.save(true, true, false);
//        // force check to fail as we handle everything above
//        return this.tickCounter + 1;
//    }

//    /**
//     * @author Zidane - Minecraft 1.14.4
//     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
//     */
//    @Overwrite
//    public boolean save(boolean suppressLog, boolean flush, boolean forced) {
//        if (!this.impl$enableSaving) {
//            return false;
//        }
//
//        for (final ServerWorld world : this.shadow$getWorlds()) {
//            final SerializationBehavior serializationBehavior = ((WorldInfoBridge) world.getWorldInfo()).bridge$getSerializationBehavior();
//            final boolean save = serializationBehavior != SerializationBehaviors.NONE;
//            boolean log = !suppressLog;
//
//            if (save) {
//                // Sponge start - check auto save interval in world config
//                if (this.shadow$isDedicatedServer() && this.shadow$isServerRunning()) {
//                    final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
//                    final int autoSaveInterval = configAdapter.getConfig().getWorld().getAutoSaveInterval();
//                    if (log) {
//                        log = configAdapter.getConfig().getLogging().logWorldAutomaticSaving();
//                    }
//                    if (autoSaveInterval <= 0 || serializationBehavior != SerializationBehaviors.AUTOMATIC) {
//                        if (log) {
//                            LOGGER.warn("Auto-saving has been disabled for world \'" + world.getWorldInfo().getWorldName() + "\'/"
//                                    + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey() + ". "
//                                    + "No chunk data will be auto-saved - to re-enable auto-saving set 'auto-save-interval' to a value greater than"
//                                    + " zero in the corresponding serverWorld config.");
//                        }
//                        continue;
//                    }
//                    if (this.tickCounter % autoSaveInterval != 0) {
//                        continue;
//                    }
//                    if (log) {
//                        LOGGER.info("Auto-saving chunks for world \'" + world.getWorldInfo().getWorldName() + "\'/"
//                                + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey());
//                    }
//                } else if (log) {
//                    LOGGER.info("Saving chunks for world \'" + world.getWorldInfo().getWorldName() + "\'/"
//                        + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey());
//                }
//
//                try {
//                    ((ServerWorldBridge) world).bridge$saveChunksAndProperties(null, flush, world.disableLevelSaving && !forced);
//                } catch (SessionLockException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return true;
//    }

//    /**
//     * @author Zidane - Minecraft 1.14.4
//     * @reason Set the difficulty without marking as custom
//     */
//    @Overwrite
//    public void setDifficultyForAllWorlds(Difficulty difficulty, boolean forceDifficulty) {
//        for (ServerWorld world : this.shadow$getWorlds()) {
//            this.bridge$updateWorldForDifficulty(world, difficulty, false);
//        }
//    }

}
