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

package dev.galacticraft.machinelib.impl.menu.sync.simple;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class IntMenuSyncHandler implements MenuSyncHandler {
    private final IntSupplier supplier;
    private final IntConsumer consumer;
    private int value;

    public IntMenuSyncHandler(IntSupplier supplier, IntConsumer consumer) {
        this.supplier = supplier;
        this.consumer = consumer;
    }

    @Override
    public boolean needsSyncing() {
        return this.value != this.supplier.getAsInt();
    }

    @Override
    public void sync(@NotNull RegistryFriendlyByteBuf buf) {
        this.value = this.supplier.getAsInt();
        buf.writeInt(this.value);
    }

    @Override
    public void read(@NotNull RegistryFriendlyByteBuf buf) {
        this.value = buf.readInt();
        this.consumer.accept(this.value);
    }
}
