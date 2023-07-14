/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.menu.sync.MenuSynchronizable;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.impl.storage.EmptyMachineItemStorage;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface MachineItemStorage extends ResourceStorage<Item, ItemResourceSlot>, MenuSynchronizable, Container {
    static @NotNull MachineItemStorage create(ItemResourceSlot @NotNull ... slots) {
        if (slots.length == 0) return empty();
        return new MachineItemStorageImpl(slots);
    }

    static @NotNull Supplier<MachineItemStorage> of(ItemResourceSlot.Builder @NotNull ... slots) {
        if (slots.length == 0) return MachineItemStorage::empty;
        return () -> {
            ItemResourceSlot[] slots1 = new ItemResourceSlot[slots.length];
            for (int i = 0; i < slots.length; i++) {
                slots1[i] = slots[i].build();
            }
            return new MachineItemStorageImpl(slots1);
        };
    }

    @Contract(pure = true)
    static @NotNull MachineItemStorage empty() {
        return EmptyMachineItemStorage.INSTANCE;
    }

    final class Builder implements Supplier<MachineItemStorage> {
        private final List<ItemResourceSlot.Builder> slots = new ArrayList<>();

        private Builder() {
        }

        @Contract("_ -> this")
        public @NotNull Builder add(ItemResourceSlot.Builder slot) {
            this.slots.add(slot);
            return this;
        }



        public @NotNull MachineItemStorage build() {
            if (this.slots.isEmpty()) return empty();
            ItemResourceSlot[] slots1 = new ItemResourceSlot[slots.size()];
            for (int i = 0; i < slots.size(); i++) {
                slots1[i] = slots.get(i).build();
            }
            return new MachineItemStorageImpl(slots1);
        }

        @Override
        public MachineItemStorage get() {
            return this.build();
        }
    }
}
