package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.network.s2c.SideConfigurationUpdatePacket;
import io.netty.buffer.ByteBuf;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SideConfigurationClickPacket(BlockFace face, Action action) implements CustomPacketPayload {
    public SideConfigurationClickPacket(BlockFace face, boolean reverse, boolean reset) {
        this(face, reset ? Action.RESET : reverse ? Action.PREVIOUS : Action.NEXT);
    }

    public static final CustomPacketPayload.Type<SideConfigurationClickPacket> TYPE = new CustomPacketPayload.Type<>(Constant.id("io_click"));
    public static final StreamCodec<ByteBuf, SideConfigurationClickPacket> CODEC = StreamCodec.composite(
            BlockFace.CODEC, p -> p.face,
            Action.CODEC, p -> p.action,
            SideConfigurationClickPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ServerPlayNetworking.Context context) {
        if (context.player().containerMenu instanceof MachineMenu<?> menu) {
            MachineBlockEntity machine = menu.machine;
            menu.cycleFaceConfig(this.face, this.action == Action.PREVIOUS, this.action == Action.RESET);
            MachineIOFace ioFace = machine.getConfiguration().getIOConfiguration().get(this.face);
            PacketSender.s2c(context.player()).send(new SideConfigurationUpdatePacket(this.face, ioFace.getType(), ioFace.getFlow()));
        }
    }

    public enum Action {
        NEXT,
        PREVIOUS,
        RESET;

        public static final StreamCodec<ByteBuf, Action> CODEC = ByteBufCodecs.BYTE.map(i -> i == -1 ? null : values()[i], face -> face == null ? -1 : (byte) face.ordinal());
    }
}
