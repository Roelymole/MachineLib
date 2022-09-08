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

package dev.galacticraft.impl.machine.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.client.screen.Tank;
import dev.galacticraft.api.machine.storage.MachineFluidStorage;
import dev.galacticraft.api.machine.storage.display.TankDisplay;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.api.screen.StorageSyncHandler;
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.machine.ModCount;
import dev.galacticraft.impl.machine.storage.slot.FluidSlot;
import dev.galacticraft.impl.machine.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public class MachineFluidStorageImpl implements MachineFluidStorage {
    private final int size;
    private final boolean[] allowsGas;
    private final TankDisplay[] displays;
    private final @NotNull FluidSlot[] inventory;
    private final @NotNull SlotType<Fluid, FluidVariant>[] types;
    private final boolean @NotNull [] extraction;
    private final boolean @NotNull [] insertion;

    private final @NotNull ModCount modCount = ModCount.root();
    private final @NotNull ExposedStorage<Fluid, FluidVariant> view = ExposedStorage.of(this, false, false);

    public MachineFluidStorageImpl(int size, SlotType<Fluid, FluidVariant>[] types, long[] counts, boolean[] allowsGas, TankDisplay[] displays) {
        this.size = size;
        this.allowsGas = allowsGas;
        this.displays = displays;
        this.inventory = new FluidSlot[this.size];
        this.extraction = new boolean[this.size];
        this.insertion = new boolean[this.size];

        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = new FluidSlot(counts[i], this.modCount);
            if (types[i].getFlow() == ResourceFlow.INPUT) {
                this.insertion[i] = true;
                this.extraction[i] = false;
            } else if (types[i].getFlow() == ResourceFlow.OUTPUT) {
                this.insertion[i] = false;
                this.extraction[i] = true;
            } else if (types[i].getFlow() == ResourceFlow.BOTH) {
                this.insertion[i] = true;
                this.extraction[i] = true;
            }
        }
        this.types = types;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public long getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public long getModCountUnsafe() {
        return this.modCount.getModCountUnsafe();
    }

    @Override
    public long getSlotModCount(int slot) {
        return this.getSlot(slot).getModCount();
    }

    @Override
    public long getSlotModCountUnsafe(int slot) {
        return this.getSlot(slot).getModCountUnsafe();
    }

    @Override
    public @NotNull ResourceSlot<Fluid, FluidVariant, FluidStack> getSlot(int slot) {
        return this.inventory[slot];
    }

    @Override
    public boolean isEmpty() {
        for (FluidSlot fluidSlot : this.inventory) {
            if (!fluidSlot.isResourceBlank()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return this.extraction[slot];
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return this.insertion[slot];
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Iterators.forArray(this.inventory); // we do not need to iterate over the inner slots' iterator as there's only one slot.
    }

    @Override
    public @NotNull FluidStack extract(int slot, @NotNull TagKey<Fluid> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        if (this.getSlot(slot).getResource().getFluid().builtInRegistryHolder().is(tag)) {
            return this.extract(slot, amount, context);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public long insert(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, amount);
        if (!this.canAccept(slot, variant) || amount == 0) return 0;
        ResourceSlot<Fluid, FluidVariant, FluidStack> invSlot = this.getSlot(slot);
        if (invSlot.isResourceBlank()) {
            amount = Math.min(amount, invSlot.getCapacity(variant));
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.setStack(variant, amount, transaction);
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.getAmount());
                if (inserted > 0) {
                    invSlot.setAmount(invSlot.getAmount() + inserted, transaction);
                    transaction.commit();
                    return inserted;
                }
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull FluidVariant variant) {
        if (!this.allowsGases(slot) && FluidVariantAttributes.isLighterThanAir(variant)) return false;
        return this.types[slot].willAccept(variant);
    }

    @Override
    public long count(@NotNull Fluid fluid) {
        long count = 0;
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluidSlot.getResource().getFluid() == fluid) {
                count += fluidSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public long count(@NotNull FluidVariant fluid) {
        long count = 0;
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluidSlot.getResource().equals(fluid)) {
                count += fluidSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean containsAny(@NotNull Collection<Fluid> fluids) {
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluids.contains(fluidSlot.getResource().getFluid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Tag writeNbt() {
        ListTag list = new ListTag();
        for (FluidSlot fluidSlot : this.inventory) {
            CompoundTag compound = fluidSlot.getResource().toNbt();
            compound.putLong(MLConstant.Nbt.AMOUNT, fluidSlot.getAmount());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
        if (nbt instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                this.inventory[i].setStackUnsafe(FluidVariant.fromNbt(compound), compound.getLong(MLConstant.Nbt.AMOUNT), true);
            }
        }
    }

    @Override
    public void clearContent() {
        assert !Transaction.isOpen();
        for (FluidSlot fluidSlot : this.inventory) {
            fluidSlot.setStackUnsafe(FluidVariant.blank(), 0, true);
        }
    }

    @Override
    public ExposedStorage<Fluid, FluidVariant> view() {
        return this.view;
    }

    @Override
    public void setSlotUnsafe(int slot, FluidVariant variant, long amount, boolean markDirty) {
        assert !Transaction.isOpen();
        this.inventory[slot].setStackUnsafe(variant, amount, markDirty);
    }

    @Override
    public long getCapacity(int slot) {
        return this.getSlot(slot).getCapacity();
    }

    @Override
    public SlotType<Fluid, FluidVariant> @NotNull [] getTypes() {
        return this.types;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return new StorageSyncHandler() {
            private long modCount = -1;

            @Override
            public boolean needsSyncing() {
                return MachineFluidStorageImpl.this.getModCount() != this.modCount;
            }

            @Override
            public void sync(FriendlyByteBuf buf) {
                this.modCount = MachineFluidStorageImpl.this.modCount.getModCount();
                for (FluidSlot slot : MachineFluidStorageImpl.this.inventory) {
                    slot.getResource().toPacket(buf);
                    buf.writeVarLong(slot.getAmount());
                }
            }

            @Override
            public void read(FriendlyByteBuf buf) {
                for (FluidSlot slot : MachineFluidStorageImpl.this.inventory) {
                    slot.setStackUnsafe(FluidVariant.fromPacket(buf), buf.readVarLong(), false);
                }
            }
        };
    }

    @Override
    public boolean allowsGases(int slot) {
        return this.allowsGas[slot];
    }

    @Override
    public <M extends MachineBlockEntity> void addTanks(MachineScreenHandler<M> handler) {
        TankDisplay[] tankDisplays = this.displays;
        ExposedStorage<Fluid, FluidVariant> of = ExposedStorage.ofPlayer(this, true, true);
        for (int i = 0; i < tankDisplays.length; i++) {
            TankDisplay tankDisplay = tankDisplays[i];
            handler.addTank(Tank.create(of, i, tankDisplay.x(), tankDisplay.y(), tankDisplay.height()));
        }
    }
}
