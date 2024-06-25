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

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.render.MachineRenderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Stores the configuration of a machine's I/O for all six faces.
 */
public class MachineIOConfig implements Serializable<ListTag>, PacketSerializable<ByteBuf>, MachineRenderData, DeltaPacketSerializable<ByteBuf, MachineIOConfig> {
    public static final StreamCodec<ByteBuf, MachineIOConfig> CODEC = PacketSerializable.createCodec(MachineIOConfig::new);

    private final MachineIOFace[] faces;

    public MachineIOConfig() {
        this.faces = new MachineIOFace[6];

        for (int i = 0; i < this.faces.length; i++) {
            this.faces[i] = new MachineIOFace();
        }
    }

    public MachineIOConfig(MachineIOFace[] faces) {
        this.faces = faces;
    }

    /**
     * Returns the I/O configuration for the given face.
     *
     * @param face the block face to pull the option from
     * @return the I/O configuration for the given face.
     */
    @NotNull
    public MachineIOFace get(@NotNull BlockFace face) {
        return this.faces[face.ordinal()];
    }

    @Override
    public @NotNull ListTag createTag() {
        ListTag nbt = new ListTag();
        for (MachineIOFace face : this.faces) {
            nbt.add(face.createTag());
        }
        return nbt;
    }

    @Override
    public void readTag(@NotNull ListTag tag) {
        for (int i = 0; i < tag.size(); i++) {
            this.faces[i].readTag(tag.getCompound(i));
        }
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        for (MachineIOFace face : this.faces) {
            face.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        for (MachineIOFace face : this.faces) {
            face.readPacket(buf);
        }
    }

    @Override
    public MachineIOConfig getIOConfig() {
        return this;
    }

    @Override
    public boolean hasChanged(@NotNull MachineIOConfig previous) {
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readDeltaPacket(@NotNull ByteBuf buf) {
        byte n = buf.readByte();
        for (int i = 0; i < n; i++) {
            this.faces[buf.readByte()].readPacket(buf);
        }
    }

    @Override
    public void writeDeltaPacket(@NotNull ByteBuf buf, @NotNull MachineIOConfig previous) {
        byte n = 0;
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                n++;
            }
        }
        buf.writeByte(n);
        for (int i = 0; i < this.faces.length; i++) {
            if (this.faces[i].hasChanged(previous.faces[i])) {
                buf.writeByte(i);
                this.faces[i].writePacket(buf);
            }
        }
    }

    @Override
    public void copyInto(@NotNull MachineIOConfig other) {
        for (int i = 0; i < this.faces.length; i++) {
            this.faces[i].copyInto(other.faces[i]);
        }
    }
}
