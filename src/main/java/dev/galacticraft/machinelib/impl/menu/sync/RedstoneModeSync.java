/*
 * Copyright (c) 2021-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
