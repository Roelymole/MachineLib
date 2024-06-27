/*
 * Copyright (c) 2021-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.api.machine.configuration;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.misc.Equivalent;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import team.reborn.energy.api.EnergyStorage;

/**
 * Represents a face of a {@link MachineBlockEntity} that has been configured to
 * accept certain types of resources.
 */
public class IoFace implements Serializable<ByteTag>, PacketSerializable<ByteBuf>, Equivalent<IoFace> {
    public static final StreamCodec<ByteBuf, IoFace> CODEC = PacketSerializable.createCodec(IoFace::new);

    /**
     * The type of resource that this face is configured to accept.
     */
    protected @NotNull ResourceType type;
    /**
     * The flow direction of this face.
     */
    protected @NotNull ResourceFlow flow;

    public IoFace() {
        this(ResourceType.NONE, ResourceFlow.BOTH);
    }

    public IoFace(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    public @NotNull ResourceType getType() {
        return this.type;
    }

    public @NotNull ResourceFlow getFlow() {
        return this.flow;
    }

    public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    public @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull StorageProvider<Item, ItemVariant> provider) {
        return null;
    }

    public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull StorageProvider<Fluid, FluidVariant> provider) {
        return null;
    }

    public @Nullable EnergyStorage getExposedEnergyStorage(@NotNull MachineEnergyStorage storage) {
        return null;
    }

    @Override
    public @NotNull ByteTag createTag() {
        return ByteTag.valueOf(pack(this.type, this.flow));
    }

    @Override
    public void readTag(@NotNull ByteTag tag) {
        byte packed = tag.getAsByte();
        this.type = unpackType(packed);
        this.flow = unpackFlow(packed);
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        buf.writeByte(pack(this.type, this.flow));
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        byte packed = buf.readByte();
        this.type = unpackType(packed);
        this.flow = unpackFlow(packed);
    }

    @Override
    public boolean hasChanged(@NotNull IoFace previous) {
        return previous.type != this.type
                || previous.flow != this.flow;
    }

    @Override
    public void copyInto(@NotNull IoFace other) {
        other.type = this.type;
        other.flow = this.flow;
    }

    @VisibleForTesting
    public static byte pack(ResourceType type, ResourceFlow flow) {
        return (byte) (flow.getId() << 3 | type.getId());
    }

    @VisibleForTesting
    public static ResourceFlow unpackFlow(byte packed) {
        return ResourceFlow.getFromId((byte) (packed >> 3));
    }

    @VisibleForTesting
    public static ResourceType unpackType(byte packed) {
        return ResourceType.getFromId((byte) (packed & 0b111));
    }

    @FunctionalInterface
    public interface StorageProvider<Resource, Variant extends TransferVariant<Resource>> {
        ExposedStorage<Resource, Variant> createExposedStorage(@NotNull ResourceFlow flow);
    }
}
