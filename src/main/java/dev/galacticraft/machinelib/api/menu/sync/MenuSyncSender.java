package dev.galacticraft.machinelib.api.menu.sync;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface MenuSyncSender {
    boolean needsSyncing();

    void sync(RegistryFriendlyByteBuf buf);
}
