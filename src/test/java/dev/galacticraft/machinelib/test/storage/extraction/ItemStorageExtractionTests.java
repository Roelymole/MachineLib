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

package dev.galacticraft.machinelib.test.storage.extraction;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
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

public class ItemStorageExtractionTests implements JUnitTest {
    protected MachineItemStorage group;

    @BeforeEach
    public void setup() {
        this.group = MachineItemStorage.create(ItemResourceSlot.create(InputType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.group.getSlot(0)).isSane());
    }

    @Nested
    final class ExtractionFailureTests {
        @Test
        public void empty() {
            assertTrue(group.isEmpty());

            assertFalse(group.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, 1));

            assertFalse(group.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));

            assertFalse(group.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
        }

        @Test
        public void incorrectType() {
            group.getSlot(0).set(Items.IRON_INGOT, 1);

            assertFalse(group.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, 1));
            assertFalse(group.extractOne(Items.GOLD_INGOT));

            assertFalse(group.isEmpty());
        }

        @Test
        public void extractionTag() {
            group.getSlot(0).set(Items.GOLD_INGOT, 1);

            assertFalse(group.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertFalse(group.extractOne(Items.GOLD_INGOT, Utils.generateComponents()));

            assertFalse(group.isEmpty());
        }

        @Test
        public void containedTag() {
            group.getSlot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 1);

            assertFalse(group.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertFalse(group.extractOne(Items.GOLD_INGOT, DataComponentPatch.EMPTY));

            assertFalse(group.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            group.getSlot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 1);

            assertFalse(group.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, group.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertFalse(group.extractOne(Items.GOLD_INGOT, Utils.generateComponents()));

            assertFalse(group.isEmpty());
        }
    }

    @Nested
    final class ExtractionSuccessTests {
        @Test
        public void exact_typed() {
            group.getSlot(0).set(Items.GOLD_INGOT, 1);

            assertTrue(group.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, group.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, group.extract(Items.GOLD_INGOT, 1));

            assertTrue(group.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            group.getSlot(0).set(Items.GOLD_INGOT, components, 1);

            assertTrue(group.canExtract(Items.GOLD_INGOT, components, 1));
            assertEquals(1, group.tryExtract(Items.GOLD_INGOT, components, 1));
            assertEquals(1, group.extract(Items.GOLD_INGOT, components, 1));

            assertTrue(group.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            group.getSlot(0).set(Items.GOLD_INGOT, 1);

            assertTrue(group.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(1, group.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(1, group.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));

            assertTrue(group.isEmpty());
        }

        @Test
        public void excess_typed() {
            group.getSlot(0).set(Items.GOLD_INGOT, 48);

            assertTrue(group.canExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, group.extract(Items.GOLD_INGOT, 16));

            assertEquals(group.getAmount(0), 48 - 16);
        }

        @Test
        public void excess_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            group.getSlot(0).set(Items.GOLD_INGOT, components, 48);

            assertTrue(group.canExtract(Items.GOLD_INGOT, components, 16));
            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, components, 16));
            assertEquals(16, group.extract(Items.GOLD_INGOT, components, 16));

            assertEquals(group.getAmount(0), 48 - 16);
        }

        @Test
        public void excess_typed_emptyNbt() {
            group.getSlot(0).set(Items.GOLD_INGOT, 48);

            assertTrue(group.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));
            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));
            assertEquals(16, group.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));

            assertEquals(group.getAmount(0), 48 - 16);
        }

        @Test
        public void insufficient_typed() {
            group.getSlot(0).set(Items.GOLD_INGOT, 16);

            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, 64));
            assertEquals(16, group.extract(Items.GOLD_INGOT, 64));

            assertTrue(group.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            group.getSlot(0).set(Items.GOLD_INGOT, components, 16);

            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, components, 64));
            assertEquals(16, group.extract(Items.GOLD_INGOT, components, 64));

            assertTrue(group.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            group.getSlot(0).set(Items.GOLD_INGOT, 16);

            assertEquals(16, group.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 64));
            assertEquals(16, group.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 64));

            assertTrue(group.isEmpty());
        }
    }
}
