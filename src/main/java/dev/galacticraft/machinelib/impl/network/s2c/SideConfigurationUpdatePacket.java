package dev.galacticraft.machinelib.impl.network.s2c;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SideConfigurationUpdatePacket(BlockFace face, ResourceType resource, ResourceFlow flow) implements CustomPacketPayload {
    public static final Type<SideConfigurationUpdatePacket> TYPE = new Type<>(Constant.id("io_update"));
    public static final StreamCodec<ByteBuf, SideConfigurationUpdatePacket> CODEC = StreamCodec.composite(
            BlockFace.CODEC, p -> p.face,
            ResourceType.STREAM_CODEC, p -> p.resource,
            ResourceFlow.STREAM_CODEC, p -> p.flow,
            SideConfigurationUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ClientPlayNetworking.Context context) {
        LocalPlayer player = context.client().player;
        if (player != null && player.containerMenu instanceof MachineMenu<?> menu) {
            menu.configuration.getIOConfiguration().get(face).setOption(resource, flow);
            menu.machine.setChanged();
        }
    }
}
