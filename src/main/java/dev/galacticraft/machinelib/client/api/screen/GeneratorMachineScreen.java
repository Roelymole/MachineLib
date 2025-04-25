/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.client.api.screen;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.block.entity.GeneratorMachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.menu.GeneratorMachineMenu;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.client.api.util.GraphicsUtil;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import dev.galacticraft.machinelib.impl.network.c2s.AccessLevelPayload;
import dev.galacticraft.machinelib.impl.network.c2s.RedstoneModePayload;
import dev.galacticraft.machinelib.impl.network.c2s.SideConfigurationClickPayload;
import dev.galacticraft.machinelib.impl.network.c2s.TankInteractionPayload;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Handles most of the boilerplate code for machine screens.
 * Handles the rendering of tanks, configuration panels and capacitors.
 *
 * @param <Machine> The type of machine block entity.
 * @param <Menu> The type of machine menu.
 */
@Environment(EnvType.CLIENT)
public class GeneratorMachineScreen<Machine extends GeneratorMachineBlockEntity, Menu extends GeneratorMachineMenu<Machine>> extends MachineScreen<Machine, Menu> {
    /**
     * Creates a new screen from the given screen handler.
     *
     * @param menu The screen handler to create the screen from.
     * @param title The title of the screen.
     * @param texture The texture of the background screen.
     */
    protected GeneratorMachineScreen(@NotNull Menu menu, @NotNull Component title, @NotNull ResourceLocation texture) {
        super(menu, title, texture);
    }

    /**
     * Appends additional information to the capacitor's tooltip.
     *
     * @param lines The list to append to.
     */
    @Override
    public void appendEnergyTooltip(List<Component> lines) {
        super.appendEnergyTooltip(lines);
        lines.add(DisplayUtil.createEnergyGenerationTooltip(this.menu.getEnergyGeneration()));
    }
}
