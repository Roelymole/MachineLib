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

import dev.galacticraft.machinelib.api.component.MLDataComponents;
import dev.galacticraft.machinelib.api.config.Config;
import dev.galacticraft.machinelib.impl.network.MachineLibPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public final class MachineLib implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constant.MOD_NAME);
    public static final Config CONFIG = Config.loadFrom(FabricLoader.getInstance().getConfigDir().resolve("machinelib.json").toFile());

    @Override
    public void onInitialize() {
        MachineLibPackets.registerChannels();
        MachineLibPackets.registerServer();
        MLDataComponents.init();

        if (CONFIG.enableColoredVanillaFluidNames()) {
            FluidVariantAttributes.enableColoredVanillaFluidNames();
        }
    }
}
