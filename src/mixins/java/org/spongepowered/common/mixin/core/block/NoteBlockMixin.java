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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.sound.PlaySoundEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin extends BlockMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "eventReceived(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"),
            cancellable = true)
    private void impl$throwNoteBlockSoundEvent(BlockState state, World worldIn, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!ShouldFire.PLAY_SOUND_EVENT_NOTE_BLOCK) {
            return;
        }

        //No noteblock sounds if the block above it isn't air
        if (worldIn.getBlockState(pos.up()).getMaterial() != Material.AIR) {
            return;
        }

        //InstrumentProperty doesn't return what we wan't for the noteblock directly, so we have to check the block under it.
        InstrumentType instrumentType = ((ServerWorld) worldIn).getBlock(pos.getX(), pos.getY() - 1, pos.getZ()).get(Keys.REPRESENTED_INSTRUMENT).orElse(null);
        if (instrumentType == null) {
            return;
        }

        float pitch = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final PlaySoundEvent.NoteBlock event = SpongeCommonEventFactory.callPlaySoundNoteBlockEvent(
                    frame.getCurrentCause(), (ServerWorld) worldIn, pos,
                    NoteBlockInstrument.byState(state).getSound(), instrumentType,
                    SpongeCommon.getRegistry().getCatalogRegistry().requireRegistry(NotePitch.class).getByValue(param), pitch);
            if (event.isCancelled()) {
                callbackInfo.setReturnValue(true);
            }
        }
    }


}
