package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.menu.MachineData;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.impl.network.s2c.MenuSyncPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class MachineDataImpl extends MachineData {
    private final ServerPlayer player;

    public MachineDataImpl(ServerPlayer player) {
        this.player = player;
    }

    public void synchronize() {
        int size = this.data.size();

        int n = 0;
        for (int i = 0; i < size; i++) {
            var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
            if (key.hasChanged(this.delta.get(i))) n++;
        }

        if (n == size) {
            synchronizeFull();
            return;
        }

        if (n > 0) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.player.registryAccess());
            buf.writeByte(n);

            for (int i = 0; i < size; i++) {
                var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
                Object data = this.delta.get(i);

                if (key.hasChanged(data)) {
                    buf.writeByte(i);
                    key.writeDeltaPacket(buf, data);
                    key.copyInto(data);
                }
            }

            ServerPlayNetworking.getSender(this.player).sendPacket(new MenuSyncPacket(buf));
        }
    }

    @Override
    public void synchronizeFull() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.player.registryAccess());
        buf.writeByte(this.data.size());
        for (int i = 0; i < this.data.size(); i++) {
            var datum = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
            datum.writePacket(buf);
            datum.copyInto(this.delta.get(i));
        }

        ServerPlayNetworking.getSender(this.player).sendPacket(new MenuSyncPacket(buf));
    }

    @Override
    public void handle(RegistryFriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }

}
