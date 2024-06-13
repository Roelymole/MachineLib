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

import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.menu.sync.MachineIOConfigSyncHandler;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class MachineIOConfigImpl implements MachineIOConfig {
    private final @NotNull MachineIOFace[] faces;

    public MachineIOConfigImpl(@NotNull MachineIOFace[] faces) {
        this.faces = faces;
    }

    public MachineIOConfigImpl() {
        this.faces = new MachineIOFace[6];
        for (int i = 0; i < 6; i++) {
            this.faces[i] = MachineIOFace.blank();
        }
    }

    @Override
    public @NotNull MachineIOFace get(@Nullable BlockFace face) {
        return this.faces[face.ordinal()];
    }

    @Override
    public @NotNull ListTag createTag() {
        ListTag nbt = new ListTag();
        for (MachineIOFace face : this.faces) {
            nbt.add(face.createTag());
        }
        return nbt;
    }

    @Override
    public void readTag(@NotNull ListTag tag) {
        for (int i = 0; i < tag.size(); i++) {
            this.faces[i].readTag(tag.getCompound(i));
        }
    }

    @Override
    public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
        for (MachineIOFace face : this.faces) {
            face.writePacket(buf);
        }
    }

    @Override
    public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
        for (MachineIOFace face : this.faces) {
            face.readPacket(buf);
        }
    }

    @Contract(" -> new")
    @Override
    public @NotNull MenuSyncHandler createSyncHandler() {
        return new MachineIOConfigSyncHandler(this);
    }
}
