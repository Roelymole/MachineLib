package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.impl.network.c2s.AccessLevelPacket;
import dev.galacticraft.machinelib.impl.network.c2s.RedstoneModePacket;
import dev.galacticraft.machinelib.impl.network.c2s.SideConfigurationClickPacket;
import dev.galacticraft.machinelib.impl.network.c2s.TankInteractionPacket;
import dev.galacticraft.machinelib.impl.network.s2c.MenuSyncPacket;
import dev.galacticraft.machinelib.impl.network.s2c.SideConfigurationUpdatePacket;
import lol.bai.badpackets.api.play.PlayPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MachineLibPackets {
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(AccessLevelPacket.TYPE, AccessLevelPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(RedstoneModePacket.TYPE, RedstoneModePacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(SideConfigurationClickPacket.TYPE, SideConfigurationClickPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(TankInteractionPacket.TYPE, TankInteractionPacket::apply);
    }

    public static void registerClient() {
        PlayPackets.registerClientReceiver(SideConfigurationUpdatePacket.TYPE, (context, payload) -> payload.apply(context));
        PlayPackets.registerClientReceiver(MenuSyncPacket.TYPE, (context, payload) -> payload.apply(context));
    }

    public static void registerChannels() {
        // c2s
        PlayPackets.registerServerChannel(AccessLevelPacket.TYPE, AccessLevelPacket.CODEC);
        PlayPackets.registerServerChannel(RedstoneModePacket.TYPE, RedstoneModePacket.CODEC);
        PlayPackets.registerServerChannel(SideConfigurationClickPacket.TYPE, SideConfigurationClickPacket.CODEC);
        PlayPackets.registerServerChannel(TankInteractionPacket.TYPE, TankInteractionPacket.CODEC);

        // s2c
        PlayPackets.registerClientChannel(SideConfigurationUpdatePacket.TYPE, SideConfigurationUpdatePacket.CODEC);
        PlayPackets.registerClientChannel(MenuSyncPacket.TYPE, MenuSyncPacket.CODEC);
    }
}
