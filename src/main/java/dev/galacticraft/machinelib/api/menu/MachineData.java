package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

public abstract class MachineData {
    protected final List<DeltaPacketSerializable<? super RegistryFriendlyByteBuf, ?>> data;
    protected final List<? super Object> delta;

    public MachineData() {
        this.data = new ArrayList<>();
        this.delta = new ArrayList<>();
    }

    public <T, S extends DeltaPacketSerializable<? super RegistryFriendlyByteBuf, T>> void register(@NotNull S serializable, T value) {
        this.data.add(serializable);
        this.delta.add(value);
    }

    public <T> void register(StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Supplier<T> getter, Consumer<T> setter) {
        this.data.add(new StreamCodecPacketSerializable<>(codec, getter, setter));
        this.delta.add(null);
    }

    public void registerInt(IntSupplier getter, IntConsumer setter) {
        this.data.add(new IntPacketSerializable(getter, setter));
        this.delta.add(new int[1]);
    }
    public <E extends Enum<E>> void registerEnum(E[] world, Supplier<E> getter, Consumer<E> setter) {
        this.data.add(new EnumPacketSerializable(world, getter, setter));
        this.delta.add(new int[1]);
    }

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


    private record IntPacketSerializable(IntSupplier getter,
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
    }

    private record LongPacketSerializable(LongSupplier getter,
                                          LongConsumer setter) implements DeltaPacketSerializable<ByteBuf, long[]> {
        @Override
        public boolean hasChanged(long[] previous) {
            return previous[0] != this.getter.getAsLong();
        }

        @Override
        public void copyInto(long[] other) {
            other[0] = this.getter.getAsLong();
        }

        @Override
        public void readPacket(@NotNull ByteBuf buf) {
            this.setter.accept(buf.readLong());
        }

        @Override
        public void writePacket(@NotNull ByteBuf buf) {
            buf.writeLong(this.getter.getAsLong());
        }
    }

    private static final class StreamCodecPacketSerializable<T> implements DeltaPacketSerializable<RegistryFriendlyByteBuf, Void> {
        private final StreamCodec<? super RegistryFriendlyByteBuf, T> codec;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private T previous = null;

        private StreamCodecPacketSerializable(StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Supplier<T> getter, Consumer<T> setter) {
            this.codec = codec;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasChanged(Void ignored) {
            return !Objects.equals(this.previous, this.getter.get());
        }

        @Override
        public void copyInto(Void other) {
            this.previous = this.getter.get();
        }

        @Override
        public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
            this.setter.accept(this.codec.decode(buf));
        }

        @Override
        public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
            this.codec.encode(buf, this.getter.get());
        }
    }

    private static final class EnumPacketSerializable<E extends Enum<E>> implements DeltaPacketSerializable<RegistryFriendlyByteBuf, Void> {
        private final E[] world;
        private final Supplier<E> getter;
        private final Consumer<E> setter;
        private E previous = null;

        private EnumPacketSerializable(E[] world, Supplier<E> getter, Consumer<E> setter) {
            this.world = world;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasChanged(Void ignored) {
            return !Objects.equals(this.previous, this.getter.get());
        }

        @Override
        public void copyInto(Void other) {
            this.previous = this.getter.get();
        }

        @Override
        public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
            int i = buf.readByte();
            if (i == -1) {
                this.setter.accept(null);
            } else {
                this.setter.accept(this.world[i]);
            }
        }

        @Override
        public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
            E value = this.getter.get();
            if (value == null) {
                buf.writeByte(-1);
            } else {
                buf.writeByte(value.ordinal());
            }
        }
    }
}
