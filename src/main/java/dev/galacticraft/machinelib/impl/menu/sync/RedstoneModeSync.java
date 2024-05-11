package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncSender;
import dev.galacticraft.machinelib.client.api.menu.sync.MenuSyncReceiver;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class RedstoneModeSync implements MenuSyncSender {
    private final MachineBlockEntity machine;
    private RedstoneMode mode;

    public RedstoneModeSync(MachineMenu<MachineBlockEntity> menu) {
        this.machine = menu.machine;
        this.mode = menu.machine.getRedstoneMode();
    }

    @Override
    public boolean needsSyncing() {
        return this.mode != this.machine.getRedstoneMode();
    }

    @Override
    public void sync(RegistryFriendlyByteBuf buf) {
        this.mode = this.machine.getRedstoneMode();

        buf.writeEnum(this.mode);
    }

    public static class Receiver implements MenuSyncReceiver<MachineBlockEntity, MachineMenu<MachineBlockEntity>> {
        public static final Receiver INSTANCE = new Receiver();

        private Receiver() {}

        @Override
        public void receive(MachineMenu<MachineBlockEntity> menu, RegistryFriendlyByteBuf buf) {
            menu.machine.setRedstoneMode(buf.readEnum(RedstoneMode.class));
        }
    }
}
