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

package dev.galacticraft.machinelib.test.storage.slot.extraction;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.Utils;
import net.minecraft.core.component.DataComponentPatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class SlotExtractionTests<Resource, Slot extends ResourceSlot<Resource>> implements MinecraftTest {
    protected static final long CAPACITY = 64;
    private static final long NORMAL_UNIT = 4;
    private static final long SMALL_UNIT = 1;
    
    private final Resource resource0;
    private final Resource resource1;
    
    protected Slot slot;

    protected SlotExtractionTests(Resource resource0, Resource resource1) {
        this.resource0 = resource0;
        this.resource1 = resource1;
    }

    protected abstract Slot createSlot();

    @BeforeEach
    public void setup() {
        this.slot = this.createSlot();
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    @Nested
    final class ExtractionFailureTests {
        @Test
        public void empty() {
            assertTrue(slot.isEmpty());

            assertFalse(slot.canExtract(NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(NORMAL_UNIT));
            assertEquals(0, slot.extract(NORMAL_UNIT));

            assertFalse(slot.canExtract(resource0, NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, NORMAL_UNIT));

            assertFalse(slot.canExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));

            assertFalse(slot.canExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, Utils.generateComponents(), NORMAL_UNIT));

            assertNull(slot.extractOne());
        }

        @Test
        public void incorrectType() {
            slot.set(resource1, NORMAL_UNIT);

            assertFalse(slot.canExtract(resource0, NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, NORMAL_UNIT));
            assertFalse(slot.extractOne(resource0));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void extractionTag() {
            slot.set(resource0, NORMAL_UNIT);

            assertFalse(slot.canExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertFalse(slot.extractOne(resource0, Utils.generateComponents()));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void containedTag() {
            slot.set(resource0, Utils.generateComponents(), NORMAL_UNIT);

            assertFalse(slot.canExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertFalse(slot.extractOne(resource0, DataComponentPatch.EMPTY));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            slot.set(resource0, Utils.generateComponents(), NORMAL_UNIT);

            assertFalse(slot.canExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.tryExtract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.extract(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertFalse(slot.extractOne(resource0, Utils.generateComponents()));

            assertFalse(slot.isEmpty());
        }
    }

    @Nested
    final class ExtractionSuccessTests {
        @Test
        public void exact_any() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.tryExtract(NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.extract(NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.tryExtract(resource0, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.extract(resource0, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, components, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.tryExtract(resource0, components, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.extract(resource0, components, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.tryExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.extract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void excess_any() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.tryExtract(SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(SMALL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT - SMALL_UNIT);
        }

        @Test
        public void excess_typed() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, SMALL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT - SMALL_UNIT);
        }

        @Test
        public void excess_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, components, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, components, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, components, SMALL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT - SMALL_UNIT);
        }

        @Test
        public void excess_typed_emptyNbt() {
            slot.set(resource0, NORMAL_UNIT);

            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, DataComponentPatch.EMPTY, SMALL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, DataComponentPatch.EMPTY, SMALL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT - SMALL_UNIT);
        }

        @Test
        public void insufficient_any() {
            slot.set(resource0, SMALL_UNIT);

            assertEquals(SMALL_UNIT, slot.tryExtract(NORMAL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed() {
            slot.set(resource0, SMALL_UNIT);

            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, NORMAL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, SMALL_UNIT);

            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, components, NORMAL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, components, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            slot.set(resource0, DataComponentPatch.EMPTY, SMALL_UNIT);

            assertEquals(SMALL_UNIT, slot.tryExtract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));
            assertEquals(SMALL_UNIT, slot.extract(resource0, DataComponentPatch.EMPTY, NORMAL_UNIT));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void overcapacity() {
            slot.set(resource0, slot.getCapacity() * 3);

            assertEquals(slot.getCapacity() * 2, slot.tryExtract(resource0, slot.getCapacity() * 2));
            assertEquals(slot.getCapacity() * 2, slot.extract(resource0, slot.getCapacity() * 2));

            assertTrue(slot.isFull());
        }
    }
}
