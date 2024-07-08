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

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public record IntPacketSerializable(IntSupplier getter,
                                    IntConsumer setter) implements DeltaPacketSerializable<ByteBuf, int[]> {
    @Override
    public boolean hasChanged(int[] previous) {
        return previous[0] != this.getter.getAsInt();
    }

    @Override
    public void copyInto(int[] other) {
        other[0] = this.getter.getAsInt();
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        this.setter.accept(buf.readInt());
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        buf.writeInt(this.getter.getAsInt());
    }

    @Override
    public int[] createEquivalent() {
        return new int[1];
    }
}
