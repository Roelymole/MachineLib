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
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemResourceSlotInsertionTests implements JUnitTest {
    private static final int CAPACITY = 64;
    protected ItemResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any());
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    @Nested
    final class InsertionFailureTests {
        @Test
        public void full() {
            slot.set(Items.GOLD_INGOT, CAPACITY);

            assertFalse(slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.insert(Items.GOLD_INGOT, 16));
        }

        @Test
        public void incorrectType() {
            slot.set(Items.IRON_INGOT, 16);

            assertFalse(slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(slot.getAmount(), 16);
        }

        @Test
        public void insertTag() {
            slot.set(Items.GOLD_INGOT, 16);

            assertFalse(slot.canInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, slot.tryInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, slot.insert(Items.GOLD_INGOT, Utils.generateComponents(), 16));

            assertEquals(slot.getAmount(), 16);
        }

        @Test
        public void containedTag() {
            slot.set(Items.GOLD_INGOT, Utils.generateComponents(), 16);

            assertFalse(slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(0, slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(slot.getAmount(), 16);
        }

        @Test
        public void mismatchedTag() {
            slot.set(Items.GOLD_INGOT, Utils.generateComponents(), 16);

            assertFalse(slot.canInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, slot.tryInsert(Items.GOLD_INGOT, Utils.generateComponents(), 16));
            assertEquals(0, slot.insert(Items.GOLD_INGOT, Utils.generateComponents(), 16));

            assertEquals(slot.getAmount(), 16);
        }
    }

    @Nested
    final class InsertionSuccessTests {
        @Test
        public void empty() {
            assertTrue(slot.canInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(16, slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(16, slot.getAmount());
        }

        @Test
        public void toCapacity() {
            assertTrue(slot.canInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, slot.tryInsert(Items.GOLD_INGOT, CAPACITY));
            assertEquals(CAPACITY, slot.insert(Items.GOLD_INGOT, CAPACITY));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, slot.tryInsert(Items.GOLD_INGOT, CAPACITY + 8));
            assertEquals(CAPACITY, slot.insert(Items.GOLD_INGOT, CAPACITY + 8));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void preFill() {
            slot.set(Items.GOLD_INGOT, 16);
            assertTrue(slot.canInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, slot.tryInsert(Items.GOLD_INGOT, 48));
            assertEquals(48, slot.insert(Items.GOLD_INGOT, 48));

            assertEquals(16 + 48, slot.getAmount());
        }

        @Test
        public void preFill_tag() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Items.GOLD_INGOT, components, 16);
            assertTrue(slot.canInsert(Items.GOLD_INGOT, components, 48));
            assertEquals(48, slot.tryInsert(Items.GOLD_INGOT, components, 48));
            assertEquals(48, slot.insert(Items.GOLD_INGOT, components, 48));

            assertEquals(16 + 48, slot.getAmount());
        }

        @Test
        public void preFill_overCapacity() {
            slot.set(Items.GOLD_INGOT, 50);
            assertEquals(14, slot.tryInsert(Items.GOLD_INGOT, 16));
            assertEquals(14, slot.insert(Items.GOLD_INGOT, 16));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void preFill_overCapacity_tag() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Items.GOLD_INGOT, components, 50);
            assertEquals(14, slot.tryInsert(Items.GOLD_INGOT, components, 16));
            assertEquals(14, slot.insert(Items.GOLD_INGOT, components, 16));

            assertEquals(CAPACITY, slot.getAmount());
        }
    }
}
