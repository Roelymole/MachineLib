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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@ApiStatus.Internal
public final class MachineEnergyStorageImpl extends SnapshotParticipant<Long> implements MachineEnergyStorage {
    public final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final @Nullable EnergyStorage[] exposedStorages = new EnergyStorage[3];
    public long amount = 0;
    private BlockEntity parent;

    public MachineEnergyStorageImpl(long capacity, long maxInput, long maxOutput) {
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;

        this.exposedStorages[ResourceFlow.INPUT.ordinal()] = this.maxInput > 0 ? ExposedEnergyStorage.create(this, this.maxInput, 0) : null;
        this.exposedStorages[ResourceFlow.OUTPUT.ordinal()] = this.maxOutput > 0 ? ExposedEnergyStorage.create(this, 0, this.maxOutput) : null;
        this.exposedStorages[ResourceFlow.BOTH.ordinal()] = this.maxInput > 0 || this.maxOutput > 0 ? ExposedEnergyStorage.create(this, this.maxInput, this.maxOutput) : null;
    }

    @Override
    protected Long createSnapshot() {
        return this.amount;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.amount = snapshot;
    }

    @Override
    public boolean canExtract(long amount) {
        return this.amount >= amount;
    }

    @Override
    public boolean canInsert(long amount) {
        return amount <= this.capacity - this.amount;
    }

    @Override
    public long tryExtract(long amount) {
        return Math.min(this.amount, amount);
    }

    @Override
    public long tryInsert(long amount) {
        return Math.min(amount, this.capacity - this.amount);
    }

    @Override
    public long extract(long amount) {
        long extracted = this.tryExtract(amount);

        if (extracted > 0) {
            this.amount -= extracted;
            this.markModified();
            return extracted;
        }
        return 0;
    }

    @Override
    public long insert(long amount) {
        long inserted = this.tryInsert(amount);

        if (inserted > 0) {
            this.amount += inserted;
            this.markModified();
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean extractExact(long amount) {
        if (this.canExtract(amount)) {
            this.amount -= amount;
            this.markModified();
            return true;
        }
        return false;
    }

    @Override
    public boolean insertExact(long amount) {
        if (this.canInsert(amount)) {
            this.amount += amount;
            this.markModified();
            return true;
        }
        return false;
    }

    @Override
    public long insert(long amount, @NotNull TransactionContext transaction) {
        long inserted = this.tryInsert(amount);

        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.amount += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(long amount, @NotNull TransactionContext transaction) {
        long extracted = this.tryExtract(amount);

        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.amount -= extracted;
            return extracted;
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFull() {
        return this.amount >= this.capacity;
    }

    @Override
    public boolean isEmpty() {
        return this.amount == 0;
    }

    @Override
    public void setEnergy(long amount, @Nullable TransactionContext context) {
        if (context != null) this.updateSnapshots(context);
        this.setEnergy(amount);
    }

    @Override
    public void setEnergy(long amount) {
        assert amount >= 0;
        this.amount = amount;
    }

    @Override
    public @Nullable EnergyStorage getExposedStorage(@NotNull ResourceFlow flow) {
        return this.exposedStorages[flow.ordinal()];
    }

    @Override
    public long externalInsertionRate() {
        return this.maxInput;
    }

    @Override
    public long externalExtractionRate() {
        return this.maxOutput;
    }

    @Override
    public void setParent(BlockEntity parent) {
        this.parent = parent;
    }

    @Override
    public boolean isValid() {
        return this.parent == null || !this.parent.isRemoved();
    }

    @Override
    public @NotNull LongTag createTag() {
        return LongTag.valueOf(this.amount);
    }

    @Override
    public void readTag(@NotNull LongTag tag) {
        this.amount = tag.getAsLong();
    }

    @Override
    public void writePacket(@NotNull ByteBuf buf) {
        buf.writeLong(this.amount);
    }

    @Override
    public void readPacket(@NotNull ByteBuf buf) {
        this.amount = buf.readLong();
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.markModified();
    }

    @Override
    public long getModifications() {
        return this.amount;
    }

    private void markModified() {
        if (this.parent != null) this.parent.setChanged();
    }

    @Override
    public boolean hasChanged(long[] previous) {
        return previous[0] != this.amount;
    }

    @Override
    public void copyInto(long[] other) {
        other[0] = this.amount;
    }

    @Override
    public long @Nullable [] createEquivalent() {
        return new long[1];
    }
}
