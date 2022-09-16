/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.api.gas;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Defines some common gases for convenience.
 */
@ApiStatus.Experimental
public final class Gases {
    @Contract(value = " -> fail", pure = true)
    private Gases() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final ResourceLocation HYDROGEN_ID = Constant.id("hydrogen");
    /**
     * Hydrogen gas.
     */
    public static final Fluid HYDROGEN = GasFluid.create(
            Component.translatable(Constant.TranslationKey.HYDROGEN),
            Constant.id("gas/hydrogen"), "H2"
    );
    public static final ResourceLocation NITROGEN_ID = Constant.id("nitrogen");
    /**
     * Nitrogen gas.
     */
    public static final Fluid NITROGEN = GasFluid.create(
            Component.translatable(Constant.TranslationKey.NITROGEN),
            Constant.id("gas/nitrogen"), "N2"
    );
    public static final ResourceLocation OXYGEN_ID = Constant.id("oxygen");
    /**
     * Oxygen gas.
     */
    public static final Fluid OXYGEN = GasFluid.create(
            Component.translatable(Constant.TranslationKey.OXYGEN),
            Constant.id("gas/oxygen"), "O2"
    );
    public static final ResourceLocation CARBON_DIOXIDE_ID = Constant.id("carbon_dioxide");
    /**
     * Carbon dioxide.
     */
    public static final Fluid CARBON_DIOXIDE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.CARBON_DIOXIDE),
            Constant.id("gas/carbon_dioxide"), "CO2"
    );
    public static final ResourceLocation WATER_VAPOR_ID = Constant.id("water_vapor");
    /**
     * Water vapor.
     */
    public static final Fluid WATER_VAPOR = GasFluid.create(
            Component.translatable(Constant.TranslationKey.WATER_VAPOR),
            Constant.id("gas/water_vapor"), "H2O"
    );
    public static final ResourceLocation METHANE_ID = Constant.id("methane");
    /**
     * Methane.
     */
    public static final Fluid METHANE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.METHANE),
            Constant.id("gas/methane"), "CH4"
    );
    public static final ResourceLocation HELIUM_ID = Constant.id("helium");
    /**
     * Helium.
     */
    public static final Fluid HELIUM = GasFluid.create(
            Component.translatable(Constant.TranslationKey.HELIUM),
            Constant.id("gas/helium"), "He"
    );
    public static final ResourceLocation ARGON_ID = Constant.id("argon");
    /**
     * Argon.
     */
    public static final Fluid ARGON = GasFluid.create(
            Component.translatable(Constant.TranslationKey.ARGON),
            Constant.id("gas/argon"), "Ar"
    );
    public static final ResourceLocation NEON_ID = Constant.id("neon");
    /**
     * Neon.
     */
    public static final Fluid NEON = GasFluid.create(
            Component.translatable(Constant.TranslationKey.NEON),
            Constant.id("gas/neon"), "Ne"
    );
    public static final ResourceLocation KRYPTON_ID = Constant.id("krypton");
    /**
     * Krypton.
     */
    public static final Fluid KRYPTON = GasFluid.create(
            Component.translatable(Constant.TranslationKey.KRYPTON),
            Constant.id("gas/krypton"), "Kr"
    );
    public static final ResourceLocation NITROUS_OXIDE_ID = Constant.id("nitrous_oxide");
    /**
     * Nitrous oxide.
     */
    public static final Fluid NITROUS_OXIDE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.NITROUS_OXIDE),
            Constant.id("gas/nitrous_oxide"), "N2O"
    );
    public static final ResourceLocation CARBON_MONOXIDE_ID = Constant.id("carbon_monoxide");
    /**
     * Carbon monoxide.
     */
    public static final Fluid CARBON_MONOXIDE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.CARBON_MONOXIDE),
            Constant.id("gas/carbon_monoxide"), "CO"
    );
    public static final ResourceLocation XENON_ID = Constant.id("xenon");
    /**
     * Xenon.
     */
    public static final Fluid XENON = GasFluid.create(
            Component.translatable(Constant.TranslationKey.XENON),
            Constant.id("gas/xenon"), "Xe"
    );
    public static final ResourceLocation OZONE_ID = Constant.id("ozone");
    /**
     * Ozone.
     */
    public static final Fluid OZONE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.OZONE),
            Constant.id("gas/ozone"), "O3"
    );
    public static final ResourceLocation NITROUS_DIOXIDE_ID = Constant.id("nitrous_dioxide");
    /**
     * Nitrous dioxide.
     */
    public static final Fluid NITROUS_DIOXIDE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.NITROUS_DIOXIDE),
            Constant.id("gas/nitrous_dioxide"), "NO2"
    );
    public static final ResourceLocation IODINE_ID = Constant.id("iodine");
    /**
     * Iodine.
     */
    public static final Fluid IODINE = GasFluid.create(
            Component.translatable(Constant.TranslationKey.IODINE),
            Constant.id("gas/iodine"), "I2"
    );

    @Contract(pure = true)
    public static void init() {}

    static {
        Registry.register(Registry.FLUID, HYDROGEN_ID, HYDROGEN);
        Registry.register(Registry.FLUID, NITROGEN_ID, NITROGEN);
        Registry.register(Registry.FLUID, OXYGEN_ID, OXYGEN);
        Registry.register(Registry.FLUID, CARBON_DIOXIDE_ID, CARBON_DIOXIDE);
        Registry.register(Registry.FLUID, WATER_VAPOR_ID, WATER_VAPOR);
        Registry.register(Registry.FLUID, METHANE_ID, METHANE);
        Registry.register(Registry.FLUID, HELIUM_ID, HELIUM);
        Registry.register(Registry.FLUID, ARGON_ID, ARGON);
        Registry.register(Registry.FLUID, NEON_ID, NEON);
        Registry.register(Registry.FLUID, KRYPTON_ID, KRYPTON);
        Registry.register(Registry.FLUID, NITROUS_OXIDE_ID, NITROUS_OXIDE);
        Registry.register(Registry.FLUID, CARBON_MONOXIDE_ID, CARBON_MONOXIDE);
        Registry.register(Registry.FLUID, XENON_ID, XENON);
        Registry.register(Registry.FLUID, OZONE_ID, OZONE);
        Registry.register(Registry.FLUID, NITROUS_DIOXIDE_ID, NITROUS_DIOXIDE);
        Registry.register(Registry.FLUID, IODINE_ID, IODINE);
    }
}
