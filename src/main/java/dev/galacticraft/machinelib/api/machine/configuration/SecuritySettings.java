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

package dev.galacticraft.machinelib.api.machine.configuration;

import dev.galacticraft.machinelib.api.misc.DeltaPacketSerializable;
import dev.galacticraft.machinelib.api.misc.Serializable;
import dev.galacticraft.machinelib.api.misc.PacketSerializable;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a security setting of a machine.
 */
public class SecuritySettings implements Serializable<CompoundTag>, DeltaPacketSerializable<FriendlyByteBuf, SecuritySettings> {
    StreamCodec<FriendlyByteBuf, SecuritySettings> CODEC = PacketSerializable.createCodec(SecuritySettings::new);

    /**
     * The profile of the player who owns the linked machine.
     */
    protected @Nullable UUID owner = null;
    protected @Nullable String username = null;

    /**
     * The access level of the linked machine.
     */
    protected @NotNull AccessLevel accessLevel = AccessLevel.PUBLIC;

    public void tryUpdate(@NotNull Player player) {
        if (this.owner == null) {
            this.owner = player.getUUID();
        }

        if (player.getUUID() == this.owner) {
            this.username = player.getGameProfile().getName();
        }
    }

    /**
     * Returns whether the player is the owner of the linked machine.
     *
     * @param player The player to check.
     * @return Whether the player is the owner of the linked machine.
     */
    @Contract(pure = true)
    public boolean isOwner(@NotNull Player player) {
        boolean b = player.getUUID() == this.owner;
        if (b) {
            this.username = player.getGameProfile().getName();
        }
        return b;
    }

    public @Nullable String getUsername() {
        return this.username;
    }

    public boolean hasAccess(@NotNull Player player) {
        return switch (this.accessLevel) {
            case PUBLIC -> true;
            case TEAM -> this.isOwner(player); // todo: teams
            case PRIVATE -> this.isOwner(player);
        };
    }

    /**
     * Returns the access level of the linked machine.
     *
     * @return The access level of the linked machine.
     */
    public @NotNull AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level of the linked machine.
     *
     * @param accessLevel The access level to set.
     */
    public void setAccessLevel(@NotNull AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the uuid of the player that owns the linked machine.
     *
     * @return the uuid of the player that owns the linked machine.
     */
    public @Nullable UUID getOwner() {
        return this.owner;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag nbt = new CompoundTag();
        if (this.owner != null) {
            nbt.putUUID(Constant.Nbt.OWNER, this.owner);
            if (this.username!= null) {
                nbt.putString(Constant.Nbt.USERNAME, this.username);
            }
        }
        nbt.putString(Constant.Nbt.ACCESS_LEVEL, this.accessLevel.getSerializedName());
        return nbt;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.contains(Constant.Nbt.OWNER)) {
            this.owner = tag.getUUID(Constant.Nbt.OWNER);
        }
        if (tag.contains(Constant.Nbt.USERNAME)) {
            this.username = tag.getString(Constant.Nbt.USERNAME);
        }

        if (tag.contains(Constant.Nbt.ACCESS_LEVEL)) {
            this.accessLevel = AccessLevel.fromString(tag.getString(Constant.Nbt.ACCESS_LEVEL));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        buf.writeByte(this.accessLevel.ordinal());
        byte bits = 0b0000;
        if (this.owner != null) bits |= 0b0001;
        if (this.username != null) bits |= 0b0010;

        if (this.owner == null) {
            buf.writeByte(0b0000);
        } else {
            buf.writeByte(bits);
            buf.writeUUID(this.owner);
            if (this.username != null) buf.writeUtf(this.username);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        byte bits = buf.readByte();

        if (bits == 0b0000) {
            this.owner = null;
            this.username = null;
        } else {
            this.owner = buf.readUUID();
            if ((bits & 0b0010) != 0) this.username = buf.readUtf();
        }
    }

    @Override
    public void writeDeltaPacket(@NotNull FriendlyByteBuf buf, SecuritySettings previous) {
        byte ref = 0b00000;
        byte nullRef = 0b00000;
        if (!Objects.equals(previous.owner, this.owner)) {
            ref |= 0b00001;
            if (this.username == null) nullRef |= 0b00001;
        }
        if (!Objects.equals(previous.username, this.username)) {
            ref |= 0b00010;
            if (this.username == null) nullRef |= 0b00010;
        }
        if (previous.accessLevel != this.accessLevel) {
            ref |= 0b01000;
        }

        buf.writeByte(ref);
        buf.writeByte(nullRef);

        if (!Objects.equals(previous.owner, this.owner)) {
            previous.owner = this.owner;
            if (previous.owner != null) {
                buf.writeUUID(previous.owner);
            }
        }
        if (!Objects.equals(previous.username, this.username)) {
            previous.username = this.username;
            if (previous.username != null) {
                buf.writeUtf(previous.username);
            }
        }
        if (previous.accessLevel != this.accessLevel) {
            previous.accessLevel = this.accessLevel;
            buf.writeByte(previous.accessLevel.ordinal());
        }
    }

    @Override
    public void readDeltaPacket(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        byte nullRef = buf.readByte();
        ref ^= nullRef;

        if ((ref & 0b00001) != 0) {
            this.owner = buf.readUUID();
        } else if ((nullRef & 0b00001) != 0) {
            this.owner = null;
        }
        if ((ref & 0b00010) != 0) {
            this.username = buf.readUtf();
        } else if ((nullRef & 0b00010) != 0) {
            this.username = null;
        }
        if ((ref & 0b01000) != 0) {
            this.accessLevel = AccessLevel.getByOrdinal(buf.readByte());
        }
    }

    @Override
    public boolean hasChanged(SecuritySettings previous) {
        return !Objects.equals(previous.owner, this.owner) || !Objects.equals(previous.username, this.username) || previous.accessLevel != this.accessLevel;
    }

    @Override
    public void copyInto(SecuritySettings other) {
        other.owner = this.owner;
        other.username = this.username;
        other.accessLevel = this.accessLevel;
    }
}
