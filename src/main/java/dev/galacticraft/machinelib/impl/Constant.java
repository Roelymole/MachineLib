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

package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@ApiStatus.Internal
public interface Constant {
    String MOD_ID = "machinelib";
    String MOD_NAME = "MachineLib";

    @Contract(pure = true, value = "_ -> new")
    static @NotNull ResourceLocation id(@NotNull String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

    interface Text {
        Style DARK_GRAY_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
        Style GOLD_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
        Style GREEN_STYLE = Style.EMPTY.withColor(ChatFormatting.GREEN);
        Style RED_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
        Style BLUE_STYLE = Style.EMPTY.withColor(ChatFormatting.BLUE);
        Style AQUA_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
        Style GRAY_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
        Style DARK_RED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
        Style LIGHT_PURPLE_STYLE = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
        Style YELLOW_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW);
        Style WHITE_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);
    }

    interface Nbt {
        String DISABLE_DROPS = "NoDrop";
        String OWNER = "Owner";
        String PROGRESS = "Progress";
        String ACCESS_LEVEL = "AccessLevel";
        String SECURITY = "Security";
        String CONFIGURATION = "Configuration";
        String ENERGY = "Energy";
        String REDSTONE_MODE = "RedstoneMode";
        String ENERGY_STORAGE = "EnergyStorage";
        String ITEM_STORAGE = "ItemStorage";
        String FLUID_STORAGE = "FluidStorage";
        String STATE = "State";
        String ACTIVE = "Active";
    }

    interface ScreenTexture {
        ResourceLocation MACHINE_CONFIG_PANELS = Constant.id("textures/gui/machine_panels.png");
        ResourceLocation OVERLAY_BARS = Constant.id("textures/gui/overlay_bars.png");
    }

    interface TextureCoordinate {
        int OVERLAY_TEX_WIDTH = 64;
        int OVERLAY_TEX_HEIGHT = 64;

        int OVERLAY_WIDTH = 16;
        int OVERLAY_HEIGHT = 48;

        int ENERGY_BACKGROUND_X = 0;
        int ENERGY_BACKGROUND_Y = 0;
        int ENERGY_X = 16;
        int ENERGY_Y = 0;

        int OXYGEN_BACKGROUND_X = 32;
        int OXYGEN_BACKGROUND_Y = 0;
        int OXYGEN_X = 48;
        int OXYGEN_Y = 0;

        /**
         * The width of a configuration panel.
         */
        int PANEL_WIDTH = 100;
        /**
         * The height of a configuration panel.
         */
        int PANEL_HEIGHT = 92;
        /**
         * The width of a configuration tab.
         */
        int TAB_WIDTH = 22;
        /**
         * The height of a configuration tab.
         */

        int TAB_HEIGHT = 22;
        int BUTTON_U = 0;
        int BUTTON_V = 208;
        int BUTTON_HOVERED_V = 224;
        int BUTTON_PRESSED_V = 240;
        int BUTTON_WIDTH = 16;
        int BUTTON_HEIGHT = 16;
        int ICON_WIDTH = 16;
        int ICON_HEIGHT = 16;
        int ICON_LOCK_PRIVATE_U = 221;
        int ICON_LOCK_PRIVATE_V = 47;
        int ICON_LOCK_PARTY_U = 204;
        int ICON_LOCK_PARTY_V = 64;
        int ICON_LOCK_PUBLIC_U = 204;
        int ICON_LOCK_PUBLIC_V = 47;
        int TAB_REDSTONE_U = 203;
        int TAB_REDSTONE_V = 0;
        int TAB_CONFIG_U = 203;
        int TAB_CONFIG_V = 23;
        int TAB_STATS_U = 226;
        int TAB_STATS_V = 0;
        int TAB_SECURITY_U = 226;
        int TAB_SECURITY_V = 23;
        int PANEL_REDSTONE_U = 0;
        int PANEL_REDSTONE_V = 0;
        int PANEL_CONFIG_U = 0;
        int PANEL_CONFIG_V = 93;
        int PANEL_STATS_U = 101;
        int PANEL_STATS_V = 0;
        int PANEL_SECURITY_U = 101;
        int PANEL_SECURITY_V = 93;
        int OWNER_FACE_SIZE = 32;
        int PANEL_UPPER_HEIGHT = 20;
    }

    interface TranslationKey {
        String PRESS_SHIFT = "tooltip.machinelib.press_shift";

        String STATUS = "ui.machinelib.machine.status";
        String STATUS_NOT_ENOUGH_ENERGY = "status.machinelib.not_enough_energy";
        String STATUS_INVALID_RECIPE = "status.machinelib.invalid_recipe";
        String STATUS_OUTPUT_FULL = "status.machinelib.output_full";
        String STATUS_CAPACITOR_FULL = "status.machinelib.capacitor_full";
        String STATUS_ACTIVE = "status.machinelib.active";
        String STATUS_IDLE = "status.machinelib.idle";

        String CURRENT_ENERGY = "ui.machinelib.machine.current_energy";

        String SECURITY = "ui.machinelib.machine.security";
        String ACCESS_LEVEL = "ui.machinelib.machine.security.access";
        String PUBLIC_ACCESS = "ui.machinelib.machine.security.access.public";
        String TEAM_ACCESS = "ui.machinelib.machine.security.access.team";
        String PRIVATE_ACCESS = "ui.machinelib.machine.security.access.private";
        String ACCESS_DENIED = "ui.machinelib.machine.security.access_denied";
        String OWNER = "ui.machinelib.machine.security.owner";

        String REDSTONE_MODE = "ui.machinelib.machine.redstone_mode";
        String IGNORE_REDSTONE = "ui.machinelib.machine.redstone_mode.ignored";
        String LOW_REDSTONE = "ui.machinelib.machine.redstone_mode.low";
        String HIGH_REDSTONE = "ui.machinelib.machine.redstone_mode.high";
        String REDSTONE_STATE = "ui.machinelib.machine.redstone_mode.state";
        String REDSTONE_STATUS = "ui.machinelib.machine.redstone_mode.status";
        String REDSTONE_ACTIVE = "ui.machinelib.machine.redstone_mode.status.enabled";
        String REDSTONE_DISABLED = "ui.machinelib.machine.redstone_mode.status.disabled";

        String CONFIGURATION = "ui.machinelib.machine.configuration";

        String STATISTICS = "ui.machinelib.machine.statistics";

        String FRONT = "ui.machinelib.face.front";
        String RIGHT = "ui.machinelib.face.right";
        String BACK = "ui.machinelib.face.back";
        String LEFT = "ui.machinelib.face.left";
        String TOP = "ui.machinelib.face.top";
        String BOTTOM = "ui.machinelib.face.bottom";

        String IN = "ui.machinelib.resource.flow.in";
        String OUT = "ui.machinelib.resource.flow.out";
        String BOTH = "ui.machinelib.resource.flow.both";

        String NONE = "ui.machinelib.resource.type.none";
        String ANY = "ui.machinelib.resource.type.any";
        String ENERGY = "ui.machinelib.resource.type.energy";
        String ITEM = "ui.machinelib.resource.type.item";
        String FLUID = "ui.machinelib.resource.type.fluid";

        String TANK_CONTENTS = "ui.machinelib.machine.tank.contents";
        String TANK_AMOUNT = "ui.machinelib.machine.tank.contents.amount";
        String TANK_CAPACITY = "ui.machinelib.machine.tank.contents.capacity";
        String TANK_EMPTY = "ui.machinelib.machine.tank.contents.empty";

        String UNIT_GJ = "ui.machinelib.units.energy.base";
        String UNIT_MEGA_GJ = "ui.machinelib.units.energy.mega";
        String UNIT_BUCKET = "ui.machinelib.units.fluid.base";
        String UNIT_MILLIBUCKET = "ui.machinelib.units.fluid.milli";

        String UNKNOWN_STATUS = "status.machinelib.unknown";
        String DISABLED = "status.machinelib.disabled";
        String UNKNOWN = "tooltip.machinelib.machine.unknown";
    }

    interface Filter {
        @SuppressWarnings("rawtypes")
        Predicate ALWAYS = o -> true;

        @Contract(pure = true)
        @SuppressWarnings("unchecked")
        static <T> Predicate<T> always() {
            return (Predicate<T>) ALWAYS;
        }
    }

    interface Cache {
        Direction[] DIRECTIONS = Direction.values();
        BlockFace[] BLOCK_FACES = BlockFace.values();
        ResourceType[] RESOURCE_TYPES = ResourceType.values();
    }
}
