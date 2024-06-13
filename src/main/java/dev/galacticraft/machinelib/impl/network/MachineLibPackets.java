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
