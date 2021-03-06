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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.tick.BlockEventTickContext;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;
import org.spongepowered.common.event.tracking.phase.tick.DimensionContext;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.event.tracking.phase.tick.TileEntityTickContext;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TrackingUtil {

    public static final int BREAK_BLOCK_INDEX = BlockChange.BREAK.ordinal();
    public static final int PLACE_BLOCK_INDEX = BlockChange.PLACE.ordinal();
    public static final int DECAY_BLOCK_INDEX = BlockChange.DECAY.ordinal();
    public static final int CHANGE_BLOCK_INDEX = BlockChange.MODIFY.ordinal();
    private static final int MULTI_CHANGE_INDEX = BlockChange.values().length;
    private static final int EVENT_COUNT = BlockChange.values().length + 1;
    private static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
            builders ->
                    transaction -> {
                        final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                        builders[blockChange.ordinal()].add(transaction);
                        builders[TrackingUtil.MULTI_CHANGE_INDEX].add(transaction);
                    }
            ;
    static final Function<SpongeBlockSnapshot, Optional<Transaction<BlockSnapshot>>> TRANSACTION_CREATION =
        (blockSnapshot) -> blockSnapshot.getServerWorld().map(worldServer -> {
            final BlockPos targetPos = blockSnapshot.getBlockPos();
            final SpongeBlockSnapshot replacement = ((TrackedWorldBridge) worldServer).bridge$createSnapshot(targetPos, BlockChangeFlags.NONE);
            return new Transaction<>(blockSnapshot, replacement);
        });
    public static final int WIDTH = 40;

    public static void tickEntity(final Consumer<net.minecraft.entity.Entity> consumer, final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final EntityBridge mixinEntity = (EntityBridge) entity;
        if (!mixinEntity.bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
        ) {
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entityTiming.startTiming();
            consumer.accept(entity);
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    public static void tickkGlobalEntity(final Consumer<net.minecraft.entity.Entity> consumer, final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final EntityBridge mixinEntity = (EntityBridge) entity;
        if (!mixinEntity.bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
        ) {
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entityTiming.startTiming();
            consumer.accept(entity);
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    public static void tickRidingEntity(final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final EntityBridge mixinEntity = (EntityBridge) entity;
        if (!mixinEntity.bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (
             final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
             ) {
            entityTiming.startTiming();
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entity.updateRidden();
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    @SuppressWarnings({"unused", "try"})
    public static void tickTileEntity(final TrackedWorldBridge mixinWorldServer, final ITickableTileEntity tile) {
        checkArgument(tile instanceof BlockEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final TileEntity tileEntity = (TileEntity) tile;
        final TileEntityBridge mixinTileEntity = (TileEntityBridge) tile;
        final BlockPos pos = tileEntity.getPos();
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) tile).bridge$getActiveChunk();
        if (!mixinTileEntity.bridge$shouldTick()) {
            return;
        }
        if (chunk == null) {
            ((ActiveChunkReferantBridge) tile).bridge$setActiveChunk((ChunkBridge) tileEntity.getWorld().getChunkAt(tileEntity.getPos()));
        }

        final TileEntityTickContext context = TickPhase.Tick.TILE_ENTITY.createPhaseContext(PhaseTracker.SERVER).source(mixinTileEntity);
        try (final PhaseContext<?> phaseContext = context) {

            if (tile instanceof CreatorTrackedBridge) {
                // Add notifier and owner so we don't have to perform lookups during the phases and other processing
                ((CreatorTrackedBridge) tile).tracked$getNotifierReference().ifPresent(phaseContext::notifier);
                // Allow the tile entity to validate the owner of itself. As long as the tile entity
                // chunk is already loaded and activated, and the tile entity has already loaded
                // the owner of itself.
                ((CreatorTrackedBridge) tile).tracked$getCreatorReference().ifPresent(phaseContext::creator);
            }

            // Finally, switch the context now that we have the owner and notifier
            phaseContext.buildAndSwitch();

            try (final Timing timing = ((TimingBridge) tileEntity).bridge$getTimingsHandler().startTiming()) {
                tile.tick();
            }
        } catch (Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, context);
        }
        // We delay clearing active chunk if TE is invalidated during tick so we must remove it after
        if (tileEntity.isRemoved()) {
            ((ActiveChunkReferantBridge) tileEntity).bridge$setActiveChunk(null);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void updateTickBlock(
            final TrackedWorldBridge mixinWorld, final net.minecraft.block.BlockState block, final BlockPos pos, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot snapshot = mixinWorld.bridge$createSnapshot(block, pos, BlockChangeFlags.NONE);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(PhaseTracker.getCauseStackManager().getCurrentCause(), snapshot);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState)block).build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext(PhaseTracker.SERVER).source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(world, pos, currentContext, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
             final Timing timing = ((TimingBridge) block.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            block.tick(world, pos, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, phaseContext);

        }
    }

    @SuppressWarnings("rawtypes")
    public static void randomTickBlock(final TrackedWorldBridge mixinWorld,
                                       final net.minecraft.block.BlockState state, final BlockPos pos, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot currentTickBlock = mixinWorld.bridge$createSnapshot(state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent
                event =
                SpongeEventFactory.createTickBlockEventRandom(PhaseTracker.getCauseStackManager().getCurrentCause(), currentTickBlock);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState) state).build();
        final BlockTickContext phaseContext = TickPhase.Tick.RANDOM_BLOCK.createPhaseContext(PhaseTracker.SERVER).source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(world, pos, currentContext, phaseContext);
        // Now actually switch to the new phase
        try (final PhaseContext<?> context = phaseContext) {
            context.buildAndSwitch();
            state.randomTick(world, pos, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, phaseContext);
        }
    }


    public static void tickWorldProvider(final ServerWorldBridge worldServer) {
        final Dimension worldProvider = ((ServerWorld) worldServer).dimension;
        try (final DimensionContext context = TickPhase.Tick.DIMENSION.createPhaseContext(PhaseTracker.SERVER).source(worldProvider)) {
            context.buildAndSwitch();
            worldProvider.tick();
        }
    }

    public static boolean fireMinecraftBlockEvent(final ServerWorld worldIn, final BlockEventData event) {
        final net.minecraft.block.BlockState currentState = worldIn.getBlockState(event.getPosition());
        final BlockEventDataBridge blockEvent = (BlockEventDataBridge) event;
        final Object source = blockEvent.bridge$getTileEntity() != null ? blockEvent.bridge$getTileEntity() : blockEvent.bridge$getTickingLocatable();
        if (source == null) {
            // No source present which means we are ignoring the phase state
            return currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        }
        final BlockEventTickContext phaseContext = TickPhase.Tick.BLOCK_EVENT.createPhaseContext(PhaseTracker.SERVER);
        phaseContext.source(source);

        final User user = ((BlockEventDataBridge) event).bridge$getSourceUser();
        if (user != null) {
            phaseContext.creator = user;
            phaseContext.notifier = user;
        }

        boolean result = true;
        try (final BlockEventTickContext o = phaseContext) {
            o.buildAndSwitch();
            phaseContext.setEventSucceeded(currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter()));
            // We need to grab the result here as the phase context close will trigger a reset
            result = phaseContext.wasNotCancelled();
        } // We can't return onBlockEventReceived because the phase state may have cancelled all transactions
        // at which point we want to keep track of the return value from the target, and from the block events.
        return result;
    }

    static boolean forceModify(final Block originalBlock, final Block newBlock) {
        if (originalBlock instanceof RepeaterBlock && newBlock instanceof RepeaterBlock) {
            return true;
        } else if (originalBlock instanceof RedstoneTorchBlock && newBlock instanceof RedstoneTorchBlock) {
            return true;
        } else
            return originalBlock instanceof RedstoneLampBlock && newBlock instanceof RedstoneLampBlock;
    }

    private TrackingUtil() {
    }

    @Nullable
    public static User getNotifierOrOwnerFromBlock(final ServerWorld world, final BlockPos blockPos) {
        final ChunkBridge mixinChunk = (ChunkBridge) world.getChunkAt(blockPos);
        final User notifier = mixinChunk.bridge$getBlockNotifier(blockPos).orElse(null);
        if (notifier != null) {
            return notifier;
        }

        return mixinChunk.bridge$getBlockCreator(blockPos).orElse(null);
    }

    public static Supplier<IllegalStateException> throwWithContext(final String s, final PhaseContext<?> phaseContext) {
        return () -> {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Exception trying to process over a phase!").centre().hr();
            printer.addWrapped(TrackingUtil.WIDTH, "%s : %s", "State", phaseContext.state);
            printer.addWrapped(TrackingUtil.WIDTH, "%s :", "PhaseContext");
            PhasePrinter.CONTEXT_PRINTER.accept(printer, phaseContext);
            printer.add("Stacktrace:");
            final IllegalStateException exception = new IllegalStateException(s + " Please analyze the current phase context. ");
            printer.add(exception);
            printer.trace(System.err, SpongeCommon.getLogger(), Level.ERROR);
            return exception;
        };
    }

    public static boolean processBlockCaptures(final PhaseContext<?> context) {
        return TrackingUtil.processBlockCaptures(context, 0, context.getCapturedBlockSupplier());
    }

    /**
     * Processes the given list of {@link BlockSnapshot}s and creates and throws and processes
     * the {@link ChangeBlockEvent}s as appropriately determined based on the {@link BlockChange}
     * for each snapshot. If any transactions are invalid or events cancelled, this event
     * returns {@code false} to signify a transaction was cancelled. This return value
     * is used for portal creation.
     *
     * @param context The phase context, only used by the phase for handling processes.
     * @param supplier
     * @return True if no events or transactions were cancelled
     */
    @SuppressWarnings({"unchecked"})
    static boolean processBlockCaptures(final PhaseContext<?> context, final int currentDepth, final MultiBlockCaptureSupplier supplier) {
        // Fail fast and check if it's empty.
        if (!supplier.hasBlocksCaptured()) {
            if (((IPhaseState) context.state).hasSpecificBlockProcess(context) && supplier.hasTransactions()) {
                // Then we just need to process the transactions, there may be things that are not
                // specifically block captured
                final ListMultimap<BlockPos, BlockEventData> scheduledEvents = supplier.getScheduledEvents();
                // Clear captured snapshots after processing them
                supplier.clear();
                return supplier.processTransactions(ImmutableList.of(), context, true, scheduledEvents, currentDepth);

            }
            return false;
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[TrackingUtil.EVENT_COUNT];
        final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[TrackingUtil.EVENT_COUNT];
        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }

        TrackingUtil.createTransactionLists(context, supplier, transactionArrays, transactionBuilders);
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents = supplier.getScheduledEvents();

        // Clear captured snapshots after processing them
        supplier.clear();

        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];

        // Creates the block events accordingly to the transaction arrays
        TrackingUtil.iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents); // Needs to throw events
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post postEvent = TrackingUtil.throwMultiEventsAndCreatePost(context, transactionArrays, blockEvents, mainEvents);

        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return false;
        }

        final List<Transaction<BlockSnapshot>> invalid = new ArrayList<>();

        // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
        // transactions of the preceding block events)
        boolean noCancelledTransactions = TrackingUtil.checkCancelledEvents(blockEvents, postEvent, scheduledEvents, context, invalid);

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        TrackingUtil.clearInvalidTransactionDrops(context, postEvent);

        if (!invalid.isEmpty()) {
            // We need to set this value and return it to signify that some transactions were cancelled
            noCancelledTransactions = false;
            TrackingUtil.rollBackTransactions(context, invalid);
            invalid.clear(); // Clear because we might re-enter for some reasons yet to be determined.

        }
        return TrackingUtil.performBlockAdditions(postEvent.getTransactions(), context, noCancelledTransactions, scheduledEvents, currentDepth);

    }

    private static void createTransactionLists(final PhaseContext<?> context, final MultiBlockCaptureSupplier supplier,
        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders) {
        final List<SpongeBlockSnapshot> snapshots = supplier.get();
        for (final SpongeBlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            context.getCapturedBlockSupplier().createTransaction(snapshot)
                .ifPresent(transaction -> TrackingUtil.TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(transaction));
        }
        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
    }

    private static boolean checkCancelledEvents(final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final PhaseContext<?> context, final List<Transaction<BlockSnapshot>> invalid) {
        boolean noCancelledTransactions = true;
        for (final ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (final Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    if (!scheduledEvents.isEmpty()) {
                        scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                    }
                    transaction.setValid(false);
                }
            }
        }
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            noCancelledTransactions = false;
            for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                if (!scheduledEvents.isEmpty()) {
                    scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                }
                transaction.setValid(false);
            }
        }
        for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                noCancelledTransactions = false;
            }
        }
        // Now to check, if any of the transactions being cancelled means cancelling the entire event.
        if (!noCancelledTransactions) {
            // This is available to verify only when necessary that a state
            // absolutely needs to cancel the entire transaction chain, this is mostly for more fasts
            // since we don't want to iterate over the transaction list multiple times.
            final boolean cancelAll = ((IPhaseState) context.state).getShouldCancelAllTransactions(context, blockEvents, postEvent, scheduledEvents, false);

            for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                if (cancelAll) {
                    if (!scheduledEvents.isEmpty()) {
                        scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                    }
                    transaction.setValid(false);
                    noCancelledTransactions = false;
                }
                if (!transaction.isValid()) {
                    invalid.add(transaction);
                }
            }
        }

        return noCancelledTransactions;
    }

    private static void clearInvalidTransactionDrops(final PhaseContext<?> context, final ChangeBlockEvent.Post postEvent) {
        for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                // Cancel any block drops performed, avoids any item drops, regardless
                if (transaction.getOriginal() instanceof SpongeBlockSnapshot) {
                    final BlockPos pos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                    context.getBlockItemDropSupplier().removeAllIfNotEmpty(pos);
                    context.getPerBlockEntitySpawnSuppplier().removeAllIfNotEmpty(pos);
                }
            }
        }
    }

    private static void rollBackTransactions(final PhaseContext<?> context, final List<Transaction<BlockSnapshot>> invalid) {
        // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
        // or the events were cancelled), again in reverse order of which they were received.
        for (final Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
            transaction.getOriginal().restore(true, BlockChangeFlags.NONE);
            ((IPhaseState) context.state).processCancelledTransaction(context, transaction, transaction.getOriginal());
        }
    }

    private static void iterateChangeBlockEvents(final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, final List<ChangeBlockEvent> blockEvents,
        final ChangeBlockEvent[] mainEvents) {
        for (final BlockChange blockChange : BlockChange.values()) {
            if (blockChange == BlockChange.DECAY) { // Decay takes place after.
                continue;
            }
            if (!transactionArrays[blockChange.ordinal()].isEmpty()) {
                final ChangeBlockEvent event = blockChange.createEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), transactionArrays[blockChange.ordinal()]);
                mainEvents[blockChange.ordinal()] = event;
                SpongeCommon.postEvent(event);
                blockEvents.add(event);
            }
        }
        if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) { // Needs to be placed into iterateChangeBlockEvents
            final ChangeBlockEvent event = BlockChange.DECAY.createEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), transactionArrays[BlockChange.DECAY.ordinal()]);
            mainEvents[BlockChange.DECAY.ordinal()] = event;
            SpongeCommon.postEvent(event);
            blockEvents.add(event);
        }
    }

    static boolean performBlockAdditions(final List<Transaction<BlockSnapshot>> transactions,
        final PhaseContext<?> phaseContext, final boolean noCancelledTransactions,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents,
        final int currentDepth) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        boolean hasEvents = false;
        if (!scheduledEvents.isEmpty()) {
            hasEvents = true;
        }
        if (((IPhaseState) phaseContext.state).hasSpecificBlockProcess(phaseContext)) {
            // In some states, we need to be taking advantage of processing the transactions in the order in which they
            // were processed. This means recycling some usage of how transactions are processed, but at the same time
            // processing the ORDER of them differently (since some notifications or block events can be thrown around
            // from time to time
            return phaseContext.getCapturedBlockSupplier().processTransactions(transactions, phaseContext, noCancelledTransactions, scheduledEvents, currentDepth);
        }
        for (final Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue;
            }
            TrackingUtil.performTransactionProcess(transaction, phaseContext, currentDepth);
        }
        phaseContext.getCapturedBlockSupplier().clearProxies();
        return noCancelledTransactions;
    }

    /**
     * The heart of all that is chaos. If you're reading this... well.. Let me explain it to you..
     * Based on the provided transaction, pulling from the original block and new {@link IBlockState},
     * we can perform physics such as {@link Block#onBlockAdded(net.minecraft.world.World, BlockPos, IBlockState)}
     * and notify neighbors. It is important that this method is replicated based on a combination of
     * {@link net.minecraft.world.World#setBlockState(BlockPos, IBlockState, int)} and
     * {@link Chunk#setBlockState(BlockPos, IBlockState)} as various "physics" and "notification" operations
     * are performed in precise order. This method is utilized in both bulk and non-bulk captures when
     * an event is required to be thrown. The deterministic requirement to know whether a bulk capture
     * is being performed or not is with the provided {@link IPhaseState} itself.
     * @param transaction The transaction to perform
     * @param phaseContext The currently working phase context
     * @param currentDepth The current processing depth, to avoid stack overflows
     */
    public static void performTransactionProcess(final Transaction<BlockSnapshot> transaction, final PhaseContext<?> phaseContext, final int currentDepth) {
        // Handle custom replacements - these need to get actually set onto the chunk, but ignored as far as tracking
        // goes.
        if (transaction.getCustom().isPresent()) {
            // Custom replacements should not trigger any physics or notifications, except for sending the notification to
            // a client. Meaning the intermediary block changes will also be ignored.
            transaction.getFinal().restore(true, BlockChangeFlags.NONE);
        }

        final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
        final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

        // Handle item drops captured
        final Optional<ServerWorld> worldServer = oldBlockSnapshot.getServerWorld();
        if (!worldServer.isPresent()) {
            // Emit a log warning about a missing world
            final String transactionForLogging = MoreObjects.toStringHelper("Transaction")
                .add("World", oldBlockSnapshot.getWorldUniqueId())
                .add("Position", oldBlockSnapshot.getBlockPos())
                .add("Original State", oldBlockSnapshot.getState())
                .add("Changed State", newBlockSnapshot.getState())
                .toString();
            SpongeCommon.getLogger().warn("Unloaded/Missing World for a captured block change! Skipping change: " + transactionForLogging);
            return;
        }
        final ServerWorldBridge mixinWorld = (ServerWorldBridge) worldServer.get();
        // Reset any previously set transactions
        final BlockPos pos = oldBlockSnapshot.getBlockPos();
        TrackingUtil.performBlockEntitySpawns( phaseContext.state, phaseContext, oldBlockSnapshot, pos);

        final ServerWorld world = (ServerWorld) mixinWorld;
        SpongeHooks.logBlockAction(world, oldBlockSnapshot.blockChange, transaction);
        final SpongeBlockChangeFlag originalChangeFlag = oldBlockSnapshot.getChangeFlag();
        final net.minecraft.block.BlockState originalState = (net.minecraft.block.BlockState) oldBlockSnapshot.getState();
        final net.minecraft.block.BlockState newState = (net.minecraft.block.BlockState) newBlockSnapshot.getState();
        // So basically, the gist is this: If we have intermediary states during captures, we want to process the states
        // in the order in which they were applied. The issue is that since some changes end up having "don't tell clients about this, but tell clients about that"
        // flags, we have to abide by the changes accordingly. Likewise, this interacts with neighbor notifications being performed.
        if (transaction.getIntermediary().isEmpty()) {
            // We call onBlockAdded here for blocks without a TileEntity.
            // ChunkMixin#bridge$setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            TrackingUtil.performOnBlockAdded(phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, newState);

            ((IPhaseState)  phaseContext.state).postBlockTransactionApplication(oldBlockSnapshot.blockChange, transaction, phaseContext);

            if (originalChangeFlag.notifyClients()) { // Always try to notify clients of the change.
                world.notifyBlockUpdate(pos, originalState, newState, originalChangeFlag.getRawFlag());
            }

            TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, newBlockSnapshot, mixinWorld, pos, originalState, newState, originalChangeFlag);
        }
        net.minecraft.block.BlockState previousIntermediary = originalState;
        boolean processedOriginal = false;
        for (final Iterator<? extends BlockSnapshot> iterator = transaction.getIntermediary().iterator(); iterator.hasNext();) {
            final SpongeBlockSnapshot intermediary = (SpongeBlockSnapshot) iterator.next();
            final SpongeBlockChangeFlag intermediaryChangeFlag = intermediary.getChangeFlag();
            final net.minecraft.block.BlockState intermediaryState = (net.minecraft.block.BlockState) intermediary.getState();
            // We have to process the original block change (since it's not part of the intermediary changes)
            // as a original -> intermediary
            if (!processedOriginal) {
                TrackingUtil.performOnBlockAdded(phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, intermediaryState);
                if (originalChangeFlag.notifyClients()) {
                    world.notifyBlockUpdate(pos, originalState, intermediaryState, originalChangeFlag.getRawFlag());
                }
                TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, intermediary, mixinWorld, pos, originalState, intermediaryState, originalChangeFlag);
                processedOriginal = true;
            }
            // Then, we can process the intermediary to final potentially if there is only the original -> intermediary -> final,
            // whereas if there's more than one intermediary, the intermediary will refer to the previous intermediary
            // block state for appropriate physics.
            final boolean isFinal = !iterator.hasNext();
            TrackingUtil.performOnBlockAdded(phaseContext, currentDepth, pos, world, intermediaryChangeFlag, isFinal ? intermediaryState : previousIntermediary, isFinal ? newState : intermediaryState);
            if (intermediaryChangeFlag.notifyClients()) {
                world.notifyBlockUpdate(pos, isFinal ? intermediaryState :  previousIntermediary, isFinal ? newState : intermediaryState, intermediaryChangeFlag.getRawFlag());
            }
            TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, isFinal ? newBlockSnapshot : intermediary, mixinWorld, pos, originalState, isFinal ? newState : intermediaryState, intermediaryChangeFlag);
            if (isFinal) {
                return;
            }
            previousIntermediary = intermediaryState;
        }
    }

    private static void performOnBlockAdded(final PhaseContext<?> phaseContext, final int currentDepth, final BlockPos pos, final ServerWorld world,
        final SpongeBlockChangeFlag changeFlag, final net.minecraft.block.BlockState originalState, final net.minecraft.block.BlockState newState) {
        final Block newBlock = newState.getBlock();
        if (originalState.getBlock() != newBlock && changeFlag.performBlockPhysics()
            && (!SpongeImplHooks.hasBlockTileEntity(newState))) {
            newBlock.onBlockAdded(newState, world, pos, originalState, changeFlag.isBlockMoving());
            ((IPhaseState) phaseContext.state).performOnBlockAddedSpawns(phaseContext, currentDepth + 1);
        }
    }

    public static void performNeighborAndClientNotifications(final PhaseContext<?> phaseContext, final int currentDepth,
                                                             final SpongeBlockSnapshot newBlockSnapshot, final ServerWorldBridge mixinWorld, final BlockPos pos,
                                                             final net.minecraft.block.BlockState originalState, final net.minecraft.block.BlockState newState,
                                                             final SpongeBlockChangeFlag changeFlag) {
        final Block newBlock = newState.getBlock();
        final IPhaseState phaseState = phaseContext.state;
        if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
            // Append the snapshot being applied that is allowing us to keep track of which source is
            // performing the notification, it's quick and dirty.
            // TODO - somehow make this more functional so we're not relying on fields.
            final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
            final BlockSnapshot previousNeighbor = context.neighborNotificationSource;
            context.neighborNotificationSource = newBlockSnapshot;
            if (changeFlag.updateNeighbors()) {
                ((ServerWorld) mixinWorld).notifyNeighbors(pos, originalState.getBlock());
                if (newState.hasComparatorInputOverride()) {
                    ((ServerWorld) mixinWorld).updateComparatorOutputLevel(pos, newBlock);
                }
            }
            context.neighborNotificationSource = previousNeighbor;
        } else if (changeFlag.notifyObservers()) {
            final int i = changeFlag.getRawFlag() & -2;
            originalState.updateDiagonalNeighbors((IWorld) mixinWorld, pos, i);
            newState.updateNeighbors((IWorld) mixinWorld, pos, i);
            newState.updateDiagonalNeighbors((IWorld) mixinWorld, pos, i);
        }

        phaseState.performPostBlockNotificationsAndNeighborUpdates(phaseContext, newState, changeFlag, currentDepth + 1);
    }

    public static void performBlockEntitySpawns(final IPhaseState<?> state, final PhaseContext<?> phaseContext, final SpongeBlockSnapshot oldBlockSnapshot,
        final BlockPos pos) {
        // This is for pre-merged items
        if (state.doesCaptureEntitySpawns() || ((IPhaseState) state).doesCaptureEntityDrops(phaseContext)) {
            phaseContext.getBlockDropSupplier().acceptAndRemoveIfPresent(pos, items ->
                    TrackingUtil.spawnItemDataForBlockDrops(items, oldBlockSnapshot, phaseContext));
            // And this is for un-pre-merged items, these will be EntityItems, not ItemDropDatas.
            phaseContext.getBlockItemDropSupplier().acceptAndRemoveIfPresent(pos, items ->
                    TrackingUtil.spawnItemEntitiesForBlockDrops(items, oldBlockSnapshot, phaseContext));
            // This is for entities actually spawned
            phaseContext.getPerBlockEntitySpawnSuppplier().acceptAndRemoveIfPresent(pos, items ->
                    TrackingUtil.spawnEntitiesForBlock(items, phaseContext));
        }
    }

    private static void spawnItemEntitiesForBlockDrops(final Collection<ItemEntity> entityItems, final BlockSnapshot newBlockSnapshot,
        final PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = entityItems.stream()
                .map(entity -> (Entity) entity)
                .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(newBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    public static void spawnItemDataForBlockDrops(final Collection<ItemDropData> itemStacks, final BlockSnapshot oldBlockSnapshot,
        final PhaseContext<?> phaseContext) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream()
                .map(ItemDropData::getStack)
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            final DropItemEvent.Pre
                dropItemEventPre =
                SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(), originalSnapshots, itemSnapshots);
            SpongeCommon.postEvent(dropItemEventPre);
            if (dropItemEventPre.isCancelled()) {
                return;
            }
        }
        final ServerLocation worldLocation = oldBlockSnapshot.getLocation().get();
        final World world = worldLocation.getWorld();
        final ServerWorld worldServer = (ServerWorld) world;
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = itemStack.getStack();
                    float f = 0.5F;
                    double offsetX = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetY = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetZ = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    final double x = position.getX() + offsetX;
                    final double y = position.getY() + offsetY;
                    final double z = position.getZ() + offsetZ;
                    ItemEntity entityitem = new ItemEntity(worldServer, x, y, z, minecraftStack);
                    entityitem.setDefaultPickupDelay();
                    return entityitem;
                })
                .map(entity -> (Entity) entity)
                .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier ->  frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    private static void spawnEntitiesForBlock(final Collection<net.minecraft.entity.Entity> entities, final PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> entitiesSpawned = entities.stream()
            .map(entity -> (Entity) entity)
            .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            SpongeCommonEventFactory.callSpawnEntity(entitiesSpawned, phaseContext);
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private static ChangeBlockEvent.Post throwMultiEventsAndCreatePost(final PhaseContext<?> context,
        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays,
        final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent[] mainEvents) {
        if (!blockEvents.isEmpty()) {
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[TrackingUtil.MULTI_CHANGE_INDEX];
            // We suffix the cause with the extra events, without modifying the cause stack manager to avoid adding extra
            // contexts or resetting the caches, this allows us to avoid adding extra frames when unnecessary.
            final Cause currentCause = PhaseTracker.getCauseStackManager().getCurrentCause();
            final Cause causeToUse;
            if (((IPhaseState) context.state).shouldProvideModifiers(context)) {
                final Cause.Builder builder = Cause.builder().from(currentCause);
                final EventContext.Builder modified = EventContext.builder();
                modified.from(currentCause.getContext());
                for (final BlockChange blockChange : BlockChange.values()) {
                    final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                    if (mainEvent != null) {
                        builder.append(mainEvent);
                        modified.add((EventContextKey<? super ChangeBlockEvent>) blockChange.getKey(), mainEvent);
                    }
                }
                causeToUse = builder.build(modified.build());
            } else {
                causeToUse = currentCause;
            }
            final ChangeBlockEvent.Post post = ((IPhaseState) context.state).createChangeBlockPostEvent(context, transactions, causeToUse);
            SpongeCommon.postEvent(post);
            return post;
        }
        return null;
    }

    public static void associateTrackerToTarget(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction, final User user) {
        final BlockSnapshot finalSnapshot = transaction.getFinal();
        final SpongeBlockSnapshot spongeSnapshot = (SpongeBlockSnapshot) finalSnapshot;
        final BlockPos pos = spongeSnapshot.getBlockPos();
        final Block block = ((net.minecraft.block.BlockState) spongeSnapshot.getState()).getBlock();
        spongeSnapshot.getServerWorld()
            .map(world -> world.getChunkAt(pos))
            .map(chunk -> (ChunkBridge) chunk)
            .ifPresent(spongeChunk -> {
            final PlayerTracker.Type trackerType = blockChange == BlockChange.PLACE ? PlayerTracker.Type.CREATOR : PlayerTracker.Type.NOTIFIER;
            spongeChunk.bridge$addTrackedBlockPosition(block, pos, user, trackerType);
        });
    }

    public static void addTileEntityToBuilder(@Nullable final TileEntity existing, final SpongeBlockSnapshotBuilder builder) {
        // We MUST only check to see if a TE exists to avoid creating a new one.
        final BlockEntity tile = (BlockEntity) existing;
        if (existing == null) {
            return;
        }
        // TODO - gather custom data.
        final CompoundNBT nbt = new CompoundNBT();
        // Some mods like OpenComputers assert if attempting to save robot while moving
        try {
            existing.write(nbt);
            builder.unsafeNbt(nbt);
        }
        catch(Throwable t) {
            // ignore
        }
    }

    public static String phaseStateToString(final String type, final IPhaseState<?> state) {
        return TrackingUtil.phaseStateToString(type, null, state);
    }

    public static String phaseStateToString(final String type, @Nullable final String extra, final IPhaseState<?> state) {
        String name = state.getClass().getSimpleName();
        name = name.replace("Phase", "");
        name = name.replace("State", "");
        name = name.replace(type, "");

        if (extra == null) {
            return type + "{" + name + "}";
        } else if (name.isEmpty()) {
            return type + "{" + extra + "}";
        } else {
            return type + "{" + name + ":" + extra + "}";
        }
    }
}
