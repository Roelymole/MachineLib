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

package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Iterator;

public class BaseSlottedStorage<Resource, Slot extends ResourceSlot<Resource>> implements SlottedStorageAccess<Resource, Slot> {
    protected final @NotNull Slot @NotNull [] slots;

    public BaseSlottedStorage(@NotNull Slot @NotNull [] slots) {
        this.slots = slots;
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean isFull() {
        for (Slot slot : this.slots) {
            if (!slot.isFull()) return false;
        }
        return true;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource, components)) {
                inserted += slot.insert(resource, components, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        return this.insert(resource, components, amount - inserted) + inserted;
    }

    @Override
    public boolean contains(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, components, amount)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource, components)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, components, amount - extracted, context);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public int size() {
        return this.slots.length;
    }

    @Override
    public SlottedStorageAccess<Resource, Slot> subStorage(int start, int len) {
        Slot[] slots1 = (Slot[]) Array.newInstance(this.slots.getClass().componentType(), len);
        System.arraycopy(this.slots, start, slots1,  0, len);
        return new BaseSlottedStorage<>(slots1);
    }

    @Override
    public SlottedStorageAccess<Resource, Slot> subStorage(int... slots) {
        Slot[] slots1 = (Slot[]) Array.newInstance(this.slots.getClass().componentType(), slots.length);
        for (int i = 0; i < slots.length; i++) {
            slots1[i] = this.slots[slots[i]];
        }
        return new BaseSlottedStorage<>(slots1);
    }

    @Override
    public @Nullable Resource getResource(int slot) {
        return this.slots[slot].getResource();
    }

    @Override
    public long getAmount(int slot) {
        return this.slots[slot].getAmount();
    }

    @Override
    public @Nullable DataComponentPatch getComponents(int slot) {
        return this.slots[slot].getComponents();
    }

    @Override
    public long getCapacity(int slot) {
        return this.slots[slot].getCapacity();
    }

    @Override
    public long getCapacityFor(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components) {
        return this.slots[slot].getCapacityFor(resource, components);
    }

    @Override
    public long getRealCapacity(int slot) {
        return this.slots[slot].getRealCapacity();
    }

    @Override
    public @NotNull ResourceFilter<Resource> getFilter(int slot) {
        return this.slots[slot].getFilter();
    }

    @Override
    public boolean isEmpty(int slot) {
        return this.slots[slot].isEmpty();
    }

    @Override
    public boolean isFull(int slot) {
        return this.slots[slot].isFull();
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource) {
        return this.slots[slot].canInsert(resource);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components) {
        return this.slots[slot].canInsert(resource, components);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].canInsert(resource, amount);
    }

    @Override
    public boolean canInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        return this.slots[slot].canInsert(resource, components, amount);
    }

    @Override
    public long tryInsert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].tryInsert(resource, amount);
    }

    @Override
    public long tryInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        return this.slots[slot].tryInsert(resource, components, amount);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].insert(resource, amount);
    }

    @Override
    public long insert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        return this.slots[slot].insert(resource, components, amount);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource) {
        return this.slots[slot].contains(resource);
    }

    @Override
    public boolean contains(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components) {
        return this.slots[slot].contains(resource, components);
    }

    @Override
    public boolean canExtract(int slot, long amount) {
        return this.slots[slot].canExtract(amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].canExtract(resource, amount);
    }

    @Override
    public boolean canExtract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        return this.slots[slot].canExtract(resource, components, amount);
    }

    @Override
    public long tryExtract(int slot, long amount) {
        return this.slots[slot].tryExtract(amount);
    }

    @Override
    public long tryExtract(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].tryExtract(resource, amount);
    }

    @Override
    public long tryExtract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        return this.slots[slot].tryExtract(resource, components, amount);
    }

    @Override
    public @Nullable Resource extractOne(int slot) {
        return this.slots[slot].extractOne();
    }

    @Override
    public boolean extractOne(int slot, @NotNull Resource resource) {
        return this.slots[slot].extractOne(resource);
    }

    @Override
    public boolean extractOne(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components) {
        return this.slots[slot].extractOne(resource, components);
    }

    @Override
    public long extract(int slot, long amount) {
        return this.slots[slot].extract(amount);
    }

    @Override
    public long extract(int slot, @NotNull Resource resource, long amount) {
        return this.slots[slot].extract(resource, amount);
    }

    @Override
    public long extract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        return this.slots[slot].extract(resource, components, amount);
    }

    @Override
    public boolean isEmpty(int start, int len) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean isFull(int start, int len) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (!slot.isFull()) return false;
        }
        return true;
    }

    @Override
    public boolean canInsert(int start, int len, @NotNull Resource resource) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.canInsert(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.canInsert(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(int start, int len, @NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.tryInsert(resource, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public boolean canInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public long tryInsert(int start, int len, @NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.tryInsert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long tryInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(int start, int len, @NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.insert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            inserted += slot.insert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insertMatching(int start, int len, @NotNull Resource resource, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.contains(resource)) {
                inserted += slot.insert(resource, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        return this.insert(start, len, resource, amount - inserted) + inserted;
    }

    @Override
    public long insertMatching(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.contains(resource, components)) {
                inserted += slot.insert(resource, components, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        return this.insert(start, len, resource, components, amount - inserted) + inserted;
    }

    @Override
    public boolean contains(int start, int len, @NotNull Resource resource) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.contains(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(int start, int len, @NotNull Resource resource, long amount) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.canExtract(resource, amount)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.canExtract(resource, components, amount)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(int start, int len, @NotNull Resource resource, long amount) {
        long extracted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            extracted += slot.tryExtract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public boolean extractOne(int start, int len, @NotNull Resource resource) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.extractOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            if (slot.extractOne(resource, components)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            extracted += slot.tryExtract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(int start, int len, @NotNull Resource resource, long amount) {
        long extracted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            extracted += slot.extract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (int i = start; i < start + len; i++) {
            Slot slot = this.slots[i];
            extracted += slot.extract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @NotNull
    @Override
    public Iterator<Slot> iterator() {
        return Iterators.forArray(this.slots);
    }
}
