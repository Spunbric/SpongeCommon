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
package org.spongepowered.common.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.entity.EntityTypes.UNKNOWN;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeEntityArchetypeBuilder extends AbstractDataBuilder<EntityArchetype> implements EntityArchetype.Builder {

    EntityType entityType = EntityTypes;
    DataContainer entityData;
    CompoundNBT compound;

    public SpongeEntityArchetypeBuilder() {
        super(EntityArchetype.class, Constants.Sponge.EntityArchetype.BASE_VERSION);
    }

    @Override
    public EntityArchetype.Builder reset() {
        this.entityType = UNKNOWN;
        this.entityData = null;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final EntityArchetype value) {
        this.entityType = value.getType();
        this.entityData = value.getEntityData();
        return this;
    }

    @Override
    protected Optional<EntityArchetype> buildContent(final DataView container) throws InvalidDataException {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_TYPE)) {
            builder.type(container.getCatalogType(Constants.Sponge.EntityArchetype.ENTITY_TYPE, EntityType.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a TileEntityType!"))
            );
        } else {
            throw new InvalidDataException("Missing the TileEntityType and BlockState! Cannot re-construct a TileEntityArchetype!");
        }

        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_DATA)) {
            builder.entityData(container.getView(Constants.Sponge.EntityArchetype.ENTITY_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the TileEntity data tag!"))
            );
        }
        return Optional.of(builder.build());
    }

    @Override
    public EntityArchetype.Builder type(final EntityType type) {
        Preconditions.checkNotNull(type, "EntityType cannot be null!");
        Preconditions.checkArgument(type != UNKNOWN, "EntityType cannot be set to UNKNOWN!");
        if (this.entityType != type) {
            this.entityData = null;
        }
        this.entityType = type;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final Entity entity) {
        Preconditions.checkNotNull(entity, "Cannot build an EntityArchetype for a null entity!");
        this.entityType = Preconditions.checkNotNull(entity.getType(), "Entity is returning a null EntityType!");
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final CompoundNBT compound = new CompoundNBT();
        minecraftEntity.writeWithoutTypeId(compound);
        compound.putString(Constants.Sponge.EntityArchetype.ENTITY_ID, entity.getType().getId());
        compound.remove(Constants.UUID);
        compound.remove(Constants.UUID_MOST);
        compound.remove(Constants.UUID_LEAST);
        compound.putBoolean(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN, true);
        this.compound = compound;
        return this;
    }

    @Override
    public EntityArchetype.Builder entityData(final DataView view) {
        Preconditions.checkNotNull(view, "Provided DataView cannot be null!");
        final DataContainer copy = view.copy();
        DataUtil.getValidators(Validations.ENTITY).validate(copy);
        this.entityData = copy;
        this.compound = null;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityArchetype.Builder setData(final Mutable<?, ?> manipulator) {
        if (this.entityData == null) {
            this.entityData = DataContainer.createNew();
            this.compound = null;
        }
        DataUtil.getRawNbtProcessor(NBTDataTypes.ENTITY, manipulator.getClass())
                .ifPresent(processor -> processor.storeToView(this.entityData, manipulator));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends Value<E>> EntityArchetype.Builder set(final V value) {
        if (this.entityData == null) {
            this.entityData = DataContainer.createNew();
            this.compound = null;
        }
        this.compound = null;
        DataUtil.getRawNbtProcessor(NBTDataTypes.ENTITY, value.getKey())
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends Value<E>> EntityArchetype.Builder set(final Key<V> key, final E value) {
        if (this.entityData == null) {
            this.entityData = DataContainer.createNew();
        }
        this.compound = null;
        DataUtil.getRawNbtProcessor(NBTDataTypes.ENTITY, key)
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @Override
    public EntityArchetype build() {
        Preconditions.checkNotNull(this.entityType);
        Preconditions.checkState(this.entityType != UNKNOWN);
        if (this.entityData != null) {
            this.entityData.remove(Constants.Entity.Player.UUID);
        }
        return new SpongeEntityArchetype(this);
    }
}
