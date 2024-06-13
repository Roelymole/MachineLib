package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.impl.network.c2s.AccessLevelPacket;
import dev.galacticraft.machinelib.impl.network.c2s.RedstoneModePacket;
import dev.galacticraft.machinelib.impl.network.c2s.SideConfigurationClickPacket;
import dev.galacticraft.machinelib.impl.network.c2s.TankInteractionPacket;
import dev.galacticraft.machinelib.impl.network.s2c.MenuSyncPacket;
import dev.galacticraft.machinelib.impl.network.s2c.SideConfigurationUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MachineLibPackets {
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(AccessLevelPacket.TYPE, AccessLevelPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(RedstoneModePacket.TYPE, RedstoneModePacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(SideConfigurationClickPacket.TYPE, SideConfigurationClickPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(TankInteractionPacket.TYPE, TankInteractionPacket::apply);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SideConfigurationUpdatePacket.TYPE, SideConfigurationUpdatePacket::apply);
        ClientPlayNetworking.registerGlobalReceiver(MenuSyncPacket.TYPE, MenuSyncPacket::apply);
    }

    public static void registerChannels() {
        // c2s
        PayloadTypeRegistry.playC2S().register(AccessLevelPacket.TYPE, AccessLevelPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RedstoneModePacket.TYPE, RedstoneModePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SideConfigurationClickPacket.TYPE, SideConfigurationClickPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TankInteractionPacket.TYPE, TankInteractionPacket.CODEC);

        // s2c
        PayloadTypeRegistry.playS2C().register(SideConfigurationUpdatePacket.TYPE, SideConfigurationUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MenuSyncPacket.TYPE, MenuSyncPacket.CODEC);
    }
}
