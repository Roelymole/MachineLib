package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record TankInteractionPacket(int id, int tank) implements CustomPacketPayload {
    public static final Type<TankInteractionPacket> TYPE = new Type<>(Constant.id("tank_click"));
    public static final StreamCodec<ByteBuf, TankInteractionPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TankInteractionPacket::id,
            ByteBufCodecs.INT, TankInteractionPacket::tank,
            TankInteractionPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ServerPlayNetworking.Context context) {
        if (context.player().containerMenu instanceof MachineMenu<?> menu && menu.containerId == this.id) {
            if (menu.tanks.size() > this.tank) {
                // todo: re-sync on failure
                menu.tanks.get(this.tank).acceptStack(ContainerItemContext.ofPlayerCursor(context.player(), menu));
            }
        }
    }
}
