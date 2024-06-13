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

package dev.galacticraft.machinelib.api.network;

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamEncoder;

public class MachineUpdatePacket {
//    public static final StreamCodec<RegistryFriendlyByteBuf, MachineUpdatePacket> CODEC = StreamCodec.ofMember((buf, packet) -> {
//        buf.writeBlockPos(packet.pos);
//        packet.configuration.networkCodec().encode(buf, packet.configuration);
//        MachineState.CODEC.encode(buf, packet.state);
//        MachineEnergyStorage.CODEC.encode(buf, packet.energy);
//        MachineItemStorage.CODEC.encode(buf, packet.items);
//        MachineFluidStorage.CODEC.encode(buf, packet.fluids);
//    }, MachineUpdatePacket::new);
//
//    protected final BlockPos pos;
//    protected final MachineConfiguration configuration;
//    protected final MachineState state;
//    protected final MachineEnergyStorage energy;
//    protected final MachineItemStorage items;
//    protected final MachineFluidStorage fluids;
//
//    public MachineUpdatePacket(BlockPos pos, MachineConfiguration configuration, MachineState state, MachineEnergyStorage energy, MachineItemStorage items, MachineFluidStorage fluids) {
//        this.pos = pos;
//        this.configuration = configuration;
//        this.state = state;
//        this.energy = energy;
//        this.items = items;
//        this.fluids = fluids;
//    }
//
//    public MachineUpdatePacket(RegistryFriendlyByteBuf buf) {
//        this.pos = buf.readBlockPos();
//        this.configuration = MachineConfiguration.CODEC.decode(buf);
//        this.state = MachineState.CODEC.decode(buf);
//        this.energy = MachineEnergyStorage.CODEC.decode(buf);
//        this.items = MachineItemStorage.CODEC.decode(buf);
//        this.fluids = MachineFluidStorage.CODEC.decode(buf);
//    }
//
//    public BlockPos getPos() {
//        return pos;
//    }
//
//    public MachineConfiguration getConfiguration() {
//        return configuration;
//    }
//
//    public MachineState getState() {
//        return state;
//    }
//
//    public MachineEnergyStorage getEnergy() {
//        return energy;
//    }
//
//    public MachineItemStorage getItems() {
//        return items;
//    }
//
//    public MachineFluidStorage getFluids() {
//        return fluids;
//    }
}
