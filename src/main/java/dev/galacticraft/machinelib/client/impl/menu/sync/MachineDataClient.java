package dev.galacticraft.machinelib.client.impl.menu.sync;

import dev.galacticraft.machinelib.api.menu.MachineData;
import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class MachineDataClient extends MachineData {
    @Override
    public void synchronize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void synchronizeFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(RegistryFriendlyByteBuf buf) {
        int n = buf.readByte();

        if (n == this.data.size()) {
            for (int i = 0; i < n; i++) {
                var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(i);
                key.readPacket(buf);
            }
        } else {
            for (int i = 0; i < n; i++) {
                int index = buf.readByte();
                var key = (DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ? super Object>) this.data.get(index);
                key.readDeltaPacket(buf);
            }
        }
    }
}
