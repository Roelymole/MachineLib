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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a storage for items in a machine.
 * @see ResourceStorage
 */
public interface MachineItemStorage extends ResourceStorage<Item, ItemResourceSlot>, Container {
    static @NotNull MachineItemStorage create(ItemResourceSlot @NotNull ... slots) {
        if (slots.length == 0) return empty();
        return new MachineItemStorageImpl(slots);
    }

    static @NotNull Supplier<MachineItemStorage> spec(ItemResourceSlot.Spec @NotNull ... slots) {
        if (slots.length == 0) return MachineItemStorage::empty;
        return () -> {
            ItemResourceSlot[] slots1 = new ItemResourceSlot[slots.length];
            for (int i = 0; i < slots.length; i++) {
                slots1[i] = slots[i].build();
            }
            return new MachineItemStorageImpl(slots1);
        };
    }

    @Contract(" -> new")
    static @NotNull MachineItemStorage.Spec builder() {
        return new Spec();
    }

    @Contract(pure = true)
    static @NotNull MachineItemStorage empty() {
        return MachineItemStorageImpl.EMPTY;
    }

    // overridden to set the variant type
    @Override
    @Nullable ExposedStorage<Item, ItemVariant> createExposedStorage(@NotNull ResourceFlow flow);

    boolean consumeOne(@NotNull Item resource);

    boolean consumeOne(@NotNull Item resource, @Nullable DataComponentPatch components);

    long consume(@NotNull Item resource, long amount);

    long consume(@NotNull Item resource, @Nullable DataComponentPatch components, long amount);

    final class Spec implements Supplier<MachineItemStorage> {
        private final List<ItemResourceSlot.Spec> slots = new ArrayList<>();

        private Spec() {
        }

        @Contract("_ -> this")
        public @NotNull MachineItemStorage.Spec add(ItemResourceSlot.Spec slot) {
            this.slots.add(slot);
            return this;
        }

        @Contract("_, _, _ -> this")
        public @NotNull MachineItemStorage.Spec add3x3Grid(InputType type, int xOffset, int yOffset) {
            return this.addGrid(type, xOffset, yOffset, 3, 3);
        }

        @Contract("_, _, _, _, _ -> this")
        public @NotNull MachineItemStorage.Spec addGrid(InputType type, int xOffset, int yOffset, int width, int height) {
            assert width > 0 && height > 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    this.add(ItemResourceSlot.builder(type).pos(x * 18 + xOffset, y * 18 + yOffset));
                }
            }
            return this;
        }

        @Override
        public MachineItemStorage get() {
            if (this.slots.isEmpty()) return empty();
            ItemResourceSlot[] slots1 = new ItemResourceSlot[slots.size()];
            for (int i = 0; i < slots.size(); i++) {
                slots1[i] = slots.get(i).build();
            }
            return new MachineItemStorageImpl(slots1);
        }
    }
}
