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

package dev.galacticraft.machinelib.impl.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.impl.menu.sync.MachineConfigurationSyncHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class MachineConfigurationImpl implements MachineConfiguration {
    private final MachineIOConfig configuration;
    private final SecuritySettings security;
    private @NotNull RedstoneMode redstone;

    public MachineConfigurationImpl() {
        this(SecuritySettings.create(), MachineIOConfig.create(), RedstoneMode.IGNORE);
    }

    public MachineConfigurationImpl(SecuritySettings security, MachineIOConfig configuration, RedstoneMode mode) {
        this.configuration = configuration;
        this.security = security;
        this.redstone = mode;
    }

    @Override
    public @NotNull MachineIOConfig getIOConfiguration() {
        return this.configuration;
    }

    @Override
    public @NotNull SecuritySettings getSecurity() {
        return this.security;
    }

    @Override
    public @NotNull RedstoneMode getRedstoneMode() {
        return this.redstone;
    }

    @Override
    public void setRedstoneMode(@NotNull RedstoneMode redstone) {
        this.redstone = redstone;
    }

    @Override
    public @NotNull Codec<MachineConfiguration> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                this.security.codec().fieldOf("security").forGetter(MachineConfiguration::getSecurity),
                this.configuration.codec().fieldOf("configuration").forGetter(MachineConfiguration::getIOConfiguration),
                RedstoneMode.CODEC.lenientOptionalFieldOf("redstone", RedstoneMode.IGNORE).forGetter(MachineConfiguration::getRedstoneMode)
        ).apply(instance, MachineConfigurationImpl::new));
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MachineConfiguration> networkCodec() {
        return StreamCodec.composite(
                this.security.networkCodec(), MachineConfiguration::getSecurity,
                this.configuration.networkCodec(), MachineConfiguration::getIOConfiguration,
                RedstoneMode.STREAM_CODEC, MachineConfiguration::getRedstoneMode,
                MachineConfigurationImpl::new
        );
    }

    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineConfigurationSyncHandler(this);
    }
}
