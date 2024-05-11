package dev.galacticraft.machinelib.impl.network.c2s;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RedstoneModePacket(RedstoneMode mode) implements CustomPacketPayload {
    public static final Type<RedstoneModePacket> TYPE = new Type<>(Constant.id("redstone_mode"));
    public static final StreamCodec<ByteBuf, RedstoneModePacket> CODEC = RedstoneMode.CODEC.map(RedstoneModePacket::new, RedstoneModePacket::mode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(ServerPlayNetworking.Context context) {
        if (context.player().containerMenu instanceof MachineMenu<?> menu) {
            MachineBlockEntity machine = menu.machine;
            machine.setRedstoneMode(mode);
        }
    }
}
