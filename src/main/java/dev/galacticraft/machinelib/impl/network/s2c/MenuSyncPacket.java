package dev.galacticraft.machinelib.impl.network.s2c;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MenuSyncPacket(RegistryFriendlyByteBuf buf) implements CustomPacketPayload {
    public static final Type<MenuSyncPacket> TYPE = new Type<>(Constant.id("menu_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MenuSyncPacket> CODEC = MachineMenu.BUF_IDENTITY_CODEC.map(MenuSyncPacket::new, MenuSyncPacket::buf);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ClientPlayNetworking.Context context) {
        LocalPlayer player = context.player();
        if (player != null && player.containerMenu instanceof MachineMenu<?> menu) {
            menu.receiveState(buf);
            menu.machine.setChanged();
        }
    }
}
