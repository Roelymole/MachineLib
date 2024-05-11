package dev.galacticraft.machinelib.client.api.menu.sync;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface MenuSyncReceiver<Machine extends MachineBlockEntity, Menu extends MachineMenu<? super Machine>> {
    void receive(Menu menu, RegistryFriendlyByteBuf buf);
}
