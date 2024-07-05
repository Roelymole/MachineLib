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

package dev.galacticraft.machinelib.test.storage.insertion;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.util.Utils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemStorageInsertionTests implements MinecraftTest {
    private static final int CAPACITY = 64;
    protected MachineItemStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = MachineItemStorage.create(ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.storage.slot(0)).isSane());
    }

    @Nested
    final class InsertionFailureTests {
        @Test
        public void full() {
            storage.slot(0).set(Items.GOLD_INGOT, CAPACITY);

            assertFalse(storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.insert(Items.GOLD_INGOT, 16));
        }

        @Test
        public void incorrectType() {
            storage.slot(0).set(Items.IRON_INGOT, 16);

            assertFalse(storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(storage.slot(0).getAmount(), 16);
        }

        @Test
        public void insertTag() {
            storage.slot(0).set(Items.GOLD_INGOT, 16);

            assertFalse(storage.canInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, storage.tryInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, storage.insert(Items.GOLD_INGOT, Utils.generateComponents(), 16));

            assertEquals(storage.slot(0).getAmount(), 16);
        }

        @Test
        public void containedTag() {
            storage.slot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 16);

            assertFalse(storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(storage.slot(0).getAmount(), 16);
        }

        @Test
        public void mismatchedTag() {
            storage.slot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 16);

            assertFalse(storage.canInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, storage.tryInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, storage.insert(Items.GOLD_INGOT, Utils.generateComponents(), 16));

            assertEquals(storage.slot(0).getAmount(), 16);
        }
    }

    @Nested
    final class InsertionSuccessTests {
        @Test
        public void empty() {
            assertTrue(storage.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(16, storage.slot(0).getAmount());
        }

        @Test
        public void toCapacity() {
            assertTrue(storage.canInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, storage.tryInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, storage.insert(Items.GOLD_INGOT, CAPACITY));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, storage.tryInsert(Items.GOLD_INGOT, CAPACITY + 8));
            assertEquals(CAPACITY, storage.insert(Items.GOLD_INGOT, CAPACITY + 8));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void preFill() {
            storage.slot(0).set(Items.GOLD_INGOT, 16);
            assertTrue(storage.canInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, storage.tryInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, storage.insert(Items.GOLD_INGOT, 48));

            assertEquals(16 + 48, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_tag() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Items.GOLD_INGOT, components, 16);
            assertTrue(storage.canInsert(Items.GOLD_INGOT, components, 48));
            assertEquals(48, storage.tryInsert(Items.GOLD_INGOT, components, 48));
            assertEquals(48, storage.insert(Items.GOLD_INGOT, components, 48));

            assertEquals(16 + 48, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_overCapacity() {
            storage.slot(0).set(Items.GOLD_INGOT, 50);
            assertEquals(14, storage.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(14, storage.insert(Items.GOLD_INGOT, 16));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_overCapacity_tag() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Items.GOLD_INGOT, components, 50);
            assertEquals(14, storage.tryInsert(Items.GOLD_INGOT, components, 16));
            assertEquals(14, storage.insert(Items.GOLD_INGOT, components, 16));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }
    }
}
