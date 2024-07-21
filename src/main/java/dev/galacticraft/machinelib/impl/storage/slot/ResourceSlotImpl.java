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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

// assertions made:
// if AMOUNT > 0 then RESOURCE is NOT NULL (and the inverse - if RESOURCE is NOT NULL then AMOUNT > 0)
// if the RESOURCE is NULL, then the COMPONENT PATCH is EMPTY (COMPONENTS are NEVER NULL)
// EVERY aborted transaction will unwind - if it skips then MODIFICATIONS will be off
public abstract class ResourceSlotImpl<Resource> extends SnapshotParticipant<ResourceSlotImpl.Snapshot<Resource>> implements ResourceSlot<Resource> {
    protected static final String RESOURCE_KEY = "Resource";
    protected static final String AMOUNT_KEY = "Amount";
    protected static final String COMPONENTS_KEY = "Components";
    protected final long capacity;
    private final TransferType transferType;
    private final ResourceFilter<Resource> externalFilter;
    protected ResourceStorage<Resource, ?> parent;

    protected @Nullable Resource resource = null;
    protected @NotNull DataComponentPatch components = DataComponentPatch.EMPTY;
    protected long amount = 0;

    private long modifications = 1;

    protected ResourceSlotImpl(TransferType transferType, ResourceFilter<Resource> externalFilter, long capacity) {
        this.transferType = transferType;
        this.externalFilter = externalFilter;
        this.capacity = capacity;
    }

    @Override
    public TransferType transferMode() {
        return this.transferType;
    }

    @Override
    public @Nullable Resource getResource() {
        assert this.isSane();
        return this.resource;
    }

    @Override
    public long getAmount() {
        assert this.isSane();
        return this.amount;
    }

    @Override
    public @NotNull DataComponentPatch getComponents() {
        assert this.isSane();
        return this.components;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public @NotNull ResourceFilter<Resource> getFilter() {
        return this.externalFilter;
    }

    @Override
    public boolean isEmpty() {
        assert this.isSane();
        return this.amount == 0;
    }

    @Override
    public boolean isFull() {
        assert this.isSane();
        return this.amount == this.getRealCapacity();
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components) {
        assert this.isSane();
        return this.amount < this.getCapacityFor(resource, components) && this.canAccept(resource, components);
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();
        return this.amount + amount <= this.getCapacityFor(resource, components) && this.canAccept(resource, components);
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        return this.canAccept(resource, components) ? Math.min(this.amount + amount, this.getCapacityFor(resource, components)) - this.amount : 0;
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = this.tryInsert(resource, components, amount);
        if (inserted > 0) {
            this.resource = resource;
            this.components = components;
            this.amount += inserted;
            this.markModified();
            return inserted;
        }
        return 0;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        return this.insert(resource, components, amount);
    }

    @Override
    public boolean contains(@NotNull Resource resource) {
        assert this.isSane();
        return this.resource == resource;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        assert this.isSane();
        return this.resource == resource && (components == null || this.components.equals(components));
    }

    @Override
    public boolean canExtract(long amount) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        return this.amount >= amount;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        return this.contains(resource, components) && this.amount >= amount;
    }

    @Override
    public long tryExtract(long amount) {
        return Math.min(this.amount, amount);
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        return this.contains(resource, components) ? Math.min(this.amount, amount) : 0;
    }

    @Override
    public @Nullable Resource extractOne() {
        if (!this.isEmpty()) {
            Resource res = this.resource;
            if (--this.amount == 0) {
                this.setEmpty();
            }
            this.markModified();
            return res;
        }
        return null;
    }

    @Override
    public boolean extractOne(@Nullable Resource resource, @Nullable DataComponentPatch components) {
        if (resource == null ? !this.isEmpty() : this.contains(resource, components)) {
            if (--this.amount == 0) {
                this.setEmpty();
            }
            this.markModified();
            return true;
        }
        return false;
    }

    @Override
    public long extract(long amount) {
        long extracted = this.tryExtract(amount);

        return doExtraction(extracted);
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = this.tryExtract(resource, components, amount);

        return doExtraction(extracted);
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long inserted = this.tryInsert(resource, components, amount);

        if (inserted > 0) {
            this.updateSnapshots(context);
            this.resource = resource;
            this.components = components;
            this.amount += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long extracted = this.tryExtract(resource, components, amount);

        if (extracted > 0) {
            this.updateSnapshots(context);
            this.amount -= extracted;
            if (this.amount == 0) {
                this.setEmpty();
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public long getModifications() {
        return this.modifications;
    }

    @Override
    public void markModified() {
        this.modifications++;
        if (this.parent != null) this.parent.markModified();
    }

    @Override
    public void markModified(@Nullable TransactionContext context) {
        this.modifications++;
        if (this.parent != null) this.parent.markModified(context);
    }

    @Override
    protected Snapshot<Resource> createSnapshot() {
        return new Snapshot<>(this.resource, this.amount, this.components, this.modifications);
    }

    @Override
    protected void readSnapshot(Snapshot<Resource> snapshot) {
        this.resource = snapshot.resource;
        this.amount = snapshot.amount;
        this.components = snapshot.components;
        this.modifications = snapshot.modifications;
        assert this.isSane();
    }

    @Override
    public void updateSnapshots(TransactionContext context) {
        if (context != null) {
            super.updateSnapshots(context);
        }

        this.markModified(context);
    }

    protected void setEmpty() {
        this.resource = null;
        this.components = DataComponentPatch.EMPTY;
        this.amount = 0;
    }

    @Override
    public void set(@Nullable Resource resource, @NotNull DataComponentPatch components, long amount) {
        this.resource = resource;
        this.components = components;
        this.amount = amount;
        assert this.isSane();
    }

    @Override
    public void set(@Nullable Resource resource, long amount) {
        this.resource = resource;
        this.components = DataComponentPatch.EMPTY;
        this.amount = amount;
        assert this.isSane();
    }

    @Override
    public void _setParent(ResourceStorage<Resource, ?> parent) {
        assert this.parent == null;
        this.parent = parent;
    }

    @Contract(pure = true)
    private boolean canAccept(@NotNull Resource resource) {
        return this.canAccept(resource, DataComponentPatch.EMPTY);
    }

    @Contract(pure = true)
    private boolean canAccept(@NotNull Resource resource, @NotNull DataComponentPatch components) {
        return this.resource == null || (this.resource == resource && this.components.equals(components));
    }

    @VisibleForTesting
    public boolean isSane() {
        return (this.resource == null && this.components.isEmpty() && this.amount == 0) || (this.resource != null && this.amount > 0);
    }

    private long doExtraction(long extracted) {
        if (extracted > 0) {
            this.amount -= extracted;
            if (this.amount == 0) {
                this.setEmpty();
            }
            this.markModified();
            return extracted;
        }
        return 0;
    }

    protected record Snapshot<Resource>(@Nullable Resource resource, long amount,
                                        @NotNull DataComponentPatch components, long modifications) {
        @SuppressWarnings("ProtectedMemberInFinalClass") // can't be private
        protected Snapshot {
            assert (resource == null && components.isEmpty() && amount == 0) || (resource != null && amount > 0);
        }
    }
}
