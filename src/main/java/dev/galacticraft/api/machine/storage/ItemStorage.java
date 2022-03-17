/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.machine.storage.ItemStorageImpl;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface ItemStorage extends ResourceStorage<Item, ItemVariant, ItemStack> {
    class Builder {
        private int size = 0;
        private final List<SlotType<Item, ItemVariant>> types = new ArrayList<>();
        private final IntList counts = new IntArrayList();

        public Builder() {}

        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type) {
            return this.addSlot(type, 64);
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type, int maxCount) {
            maxCount = Math.min(maxCount, 64);
            this.size++;
            this.types.add(type);
            this.counts.add(maxCount);
            return this;
        }

        @Contract(pure = true, value = " -> new")
        public @NotNull ItemStorageImpl build() {
            return new ItemStorageImpl(this.size, this.types.toArray(new SlotType[0]), this.counts.toIntArray());
        }
    }
}
