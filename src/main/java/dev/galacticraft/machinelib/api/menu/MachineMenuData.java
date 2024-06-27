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

package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.impl.menu.sync.EnumPacketSerializable;
import dev.galacticraft.machinelib.impl.menu.sync.IntPacketSerializable;
import dev.galacticraft.machinelib.impl.menu.sync.LongPacketSerializable;
import dev.galacticraft.machinelib.impl.menu.sync.StreamCodecPacketSerializable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * A class that manages the synchronization of machine menu data between the client and server.
 * Use {@link #register(DeltaPacketSerializable, Object)} to register
 * a new piece of interior-mutable data to be synchronized.
 * Use the other methods to register field-mutable, but interior-immutable data.
 *
 * @see DeltaPacketSerializable
 */
public abstract class MachineMenuData {
    /**
     * The data to be synchronized.
     */
    protected final List<DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ?>> data;

    /**
     * The delta values of the data to be synchronized. Indices correspond to the data list.
     */
    protected final List<? super Object> delta;

    protected MachineMenuData() {
        this.data = new ArrayList<>();
        this.delta = new ArrayList<>();
    }

    /**
     * Registers a new piece of data to be synchronized.
     *
     * @param serializable the data to be synchronized
     * @param value the initial value of the data
     * @param <T> the type of the data
     */
    public <T> void register(@NotNull DeltaPacketSerializable<? super RegistryFriendlyByteBuf, T> serializable, T value) {
        this.data.add(serializable);
        this.delta.add(value);
    }

    /**
     * Registers an immutable codec-serializable piece of data to be synchronized.
     *
     * @param codec the codec to serialize the data
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     * @param <T> the type of the data
     */
    public <T> void register(StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Supplier<T> getter, Consumer<T> setter) {
        this.data.add(new StreamCodecPacketSerializable<>(codec, getter, setter));
        this.delta.add(null);
    }

    /**
     * Registers an integer field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerInt(IntSupplier getter, IntConsumer setter) {
        this.data.add(new IntPacketSerializable(getter, setter));
        this.delta.add(new int[1]);
    }

    /**
     * Registers an enum field to be synchronized.
     *
     * @param world the enum values
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     * @param <E> the type of the enum
     */
    public <E extends Enum<E>> void registerEnum(E[] world, Supplier<E> getter, Consumer<E> setter) {
        this.data.add(new EnumPacketSerializable<>(world, getter, setter));
        this.delta.add(null);
    }

    /**
     * Registers a long field to be synchronized.
     *
     * @param getter provides the current value of the data
     * @param setter sets the value of the data
     */
    public void registerLong(LongSupplier getter, LongConsumer setter) {
        this.data.add(new LongPacketSerializable(getter, setter));
        this.delta.add(new long[1]);
    }

    @ApiStatus.Internal
    public abstract void synchronize();

    @ApiStatus.Internal
    public abstract void synchronizeFull();

    @ApiStatus.Internal
    public abstract void handle(RegistryFriendlyByteBuf buf);

}
