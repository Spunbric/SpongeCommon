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
package org.spongepowered.common.registry.builtin.sponge;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.SpongeEventContextKey;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public final class EventContextKeyStreamGenerator {

    public static Stream<EventContextKey<?>> stream() {
        // @formatter:off
        return Stream.of(
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_process"), new TypeToken<LocatableBlock>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_queue"), new TypeToken<LocatableBlock>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_hit"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_target"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("break_event"), new TypeToken<ChangeBlockEvent.Break>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("command"), new TypeToken<String>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("creator"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("damage_type"), new TypeToken<DamageType>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("decay_event"), new TypeToken<ChangeBlockEvent.Decay>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("dismount_type"), new TypeToken<DismountType>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("entity_hit"), new TypeToken<Entity>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("fake_player"), new TypeToken<Player>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("fire_spread"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("growth_origin"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("grow_event"), new TypeToken<ChangeBlockEvent.Grow>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("igniter"), new TypeToken<Living>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("last_damage_source"), new TypeToken<DamageSource>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("leaves_decay"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_break"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_flow"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_mix"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("location"), new TypeToken<ServerLocation>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("message_channel"), new TypeToken<MessageChannel>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("modify_event"), new TypeToken<ChangeBlockEvent.Modify>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("neighbor_notify_source"), new TypeToken<BlockSnapshot>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("notifier"), new TypeToken<User>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_extend"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_retract"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("place_event"), new TypeToken<ChangeBlockEvent.Place>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("player"), new TypeToken<Player>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_break"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_place"), new TypeToken<ServerWorld>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("plugin"), new TypeToken<PluginContainer>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("projectile_source"), new TypeToken<ProjectileSource>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("rotation"), new TypeToken<Vector3d>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("simulated_player"), new TypeToken<GameProfile>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("spawn_type"), new TypeToken<SpawnType>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("subject"), new TypeToken<Subject>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("teleport_type"), new TypeToken<TeleportType>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_hand"), new TypeToken<HandType>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_item"), new TypeToken<ItemStackSnapshot>() { private static final long serialVersionUID = 1L; }),
            new SpongeEventContextKey<>(ResourceKey.sponge("weapon"), new TypeToken<ItemStackSnapshot>() { private static final long serialVersionUID = 1L; })
        );
        // @formatter:on
    }
}
