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
