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
import dev.galacticraft.machinelib.api.transfer.TransferType;
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

public class ItemStorageExtractionTests implements MinecraftTest {
    protected MachineItemStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = MachineItemStorage.create(ItemResourceSlot.create(TransferType.STORAGE, ItemSlotDisplay.create(0, 0), ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.storage.slot(0)).isSane());
    }

    @Nested
    final class ExtractionFailureTests {
        @Test
        public void empty() {
            assertTrue(storage.isEmpty());

            assertFalse(storage.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, 1));

            assertFalse(storage.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));

            assertFalse(storage.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
        }

        @Test
        public void incorrectType() {
            storage.slot(0).set(Items.IRON_INGOT, 1);

            assertFalse(storage.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, 1));
            assertFalse(storage.extractOne(Items.GOLD_INGOT));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void extractionTag() {
            storage.slot(0).set(Items.GOLD_INGOT, 1);

            assertFalse(storage.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertFalse(storage.extractOne(Items.GOLD_INGOT, Utils.generateComponents()));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void containedTag() {
            storage.slot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 1);

            assertFalse(storage.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertFalse(storage.extractOne(Items.GOLD_INGOT, DataComponentPatch.EMPTY));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            storage.slot(0).set(Items.GOLD_INGOT, Utils.generateComponents(), 1);

            assertFalse(storage.canExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.tryExtract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertEquals(0, storage.extract(Items.GOLD_INGOT, Utils.generateComponents(), 1));
            assertFalse(storage.extractOne(Items.GOLD_INGOT, Utils.generateComponents()));

            assertFalse(storage.isEmpty());
        }
    }

    @Nested
    final class ExtractionSuccessTests {
        @Test
        public void exact_typed() {
            storage.slot(0).set(Items.GOLD_INGOT, 1);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, storage.tryExtract(Items.GOLD_INGOT, 1));
            assertEquals(1, storage.extract(Items.GOLD_INGOT, 1));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Items.GOLD_INGOT, components, 1);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, components, 1));
            assertEquals(1, storage.tryExtract(Items.GOLD_INGOT, components, 1));
            assertEquals(1, storage.extract(Items.GOLD_INGOT, components, 1));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            storage.slot(0).set(Items.GOLD_INGOT, 1);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(1, storage.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));
            assertEquals(1, storage.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 1));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void excess_typed() {
            storage.slot(0).set(Items.GOLD_INGOT, 48);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, 16));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, 16));

            assertEquals(storage.slot(0).getAmount(), 48 - 16);
        }

        @Test
        public void excess_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Items.GOLD_INGOT, components, 48);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, components, 16));
            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, components, 16));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, components, 16));

            assertEquals(storage.slot(0).getAmount(), 48 - 16);
        }

        @Test
        public void excess_typed_emptyNbt() {
            storage.slot(0).set(Items.GOLD_INGOT, 48);

            assertTrue(storage.canExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));
            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 16));

            assertEquals(storage.slot(0).getAmount(), 48 - 16);
        }

        @Test
        public void insufficient_typed() {
            storage.slot(0).set(Items.GOLD_INGOT, 16);

            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, 64));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, 64));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Items.GOLD_INGOT, components, 16);

            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, components, 64));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, components, 64));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            storage.slot(0).set(Items.GOLD_INGOT, 16);

            assertEquals(16, storage.tryExtract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 64));
            assertEquals(16, storage.extract(Items.GOLD_INGOT, DataComponentPatch.EMPTY, 64));

            assertTrue(storage.isEmpty());
        }
    }
}
