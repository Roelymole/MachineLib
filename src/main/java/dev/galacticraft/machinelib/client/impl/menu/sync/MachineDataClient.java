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
