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

package dev.galacticraft.machinelib.impl.compat.waila;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.impl.Constant;
import mcp.mobius.waila.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Optional;

public class MachineLibWailaPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addBlockData((IDataProvider<MachineBlockEntity>) (data, accessor, config) -> {
            data.raw().put("security", accessor.getTarget().getSecurity().createTag());
            data.raw().put("redstone", accessor.getTarget().getRedstoneMode().createTag());
        }, MachineBlock.class);

        registrar.addComponent(new IBlockComponentProvider() {
            @Override
            public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                SecuritySettings security = new SecuritySettings();
                RedstoneMode redstone = RedstoneMode.readTag(accessor.getData().raw().get("redstone"));
                security.readTag(accessor.getData().raw().getCompound("security"));

                tooltip.addLine(Component.translatable("ui.machinelib.machine.redstone_mode.tooltip", redstone.getName()).setStyle(Constant.Text.RED_STYLE));
                if (security.getOwner() != null) {
                    Optional<GameProfile> profile = SkullBlockEntity.fetchGameProfile(security.getOwner()).getNow(null);
                    if (profile != null && profile.isPresent()) {
                        tooltip.addLine(Component.translatable("ui.machinelib.machine.security.owner", Component.literal(profile.get().getName()).setStyle(Constant.Text.WHITE_STYLE)).setStyle(Constant.Text.AQUA_STYLE));
                    }
                }
            }
        }, TooltipPosition.BODY, MachineBlock.class);
    }
}
