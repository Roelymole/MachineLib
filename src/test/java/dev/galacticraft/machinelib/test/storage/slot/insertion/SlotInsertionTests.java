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

package dev.galacticraft.machinelib.test.storage.slot.insertion;

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

public abstract class SlotInsertionTests<Resource, Slot extends ResourceSlot<Resource>> implements MinecraftTest {
    protected static final long CAPACITY = 64;
    private static final long NORMAL_UNIT = 4;
    private static final long SMALL_UNIT = 1;
    
    private final Resource resource0;
    private final Resource resource1;
    
    protected Slot slot;

    public SlotInsertionTests(Resource resource0, Resource resource1) {
        this.resource0 = resource0;
        this.resource1 = resource1;
    }

    @BeforeEach
    public void setup() {
        this.slot = this.createSlot();
    }
    
    protected abstract Slot createSlot();

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    @Nested
    final class InsertionFailureTests {
        @Test
        public void full() {
            slot.set(resource0, CAPACITY);

            assertFalse(slot.canInsert(resource0, NORMAL_UNIT));
            assertEquals(0, slot.tryInsert(resource0, NORMAL_UNIT));
            assertEquals(0, slot.insert(resource0, NORMAL_UNIT));
        }

        @Test
        public void incorrectType() {
            slot.set(resource0, NORMAL_UNIT);

            assertFalse(slot.canInsert(resource1, NORMAL_UNIT));
            assertEquals(0, slot.tryInsert(resource1, NORMAL_UNIT));
            assertEquals(0, slot.insert(resource1, NORMAL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT);
        }

        @Test
        public void insertTag() {
            slot.set(resource0, NORMAL_UNIT);

            assertFalse(slot.canInsert(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.tryInsert(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.insert(resource0, Utils.generateComponents(), NORMAL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT);
        }

        @Test
        public void containedTag() {
            slot.set(resource0, Utils.generateComponents(), NORMAL_UNIT);

            assertFalse(slot.canInsert(resource0, NORMAL_UNIT));
            assertEquals(0, slot.tryInsert(resource0, NORMAL_UNIT));
            assertEquals(0, slot.insert(resource0, NORMAL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT);
        }

        @Test
        public void mismatchedTag() {
            slot.set(resource0, Utils.generateComponents(), NORMAL_UNIT);

            assertFalse(slot.canInsert(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.tryInsert(resource0, Utils.generateComponents(), NORMAL_UNIT));
            assertEquals(0, slot.insert(resource0, Utils.generateComponents(), NORMAL_UNIT));

            assertEquals(slot.getAmount(), NORMAL_UNIT);
        }
    }

    @Nested
    final class InsertionSuccessTests {
        @Test
        public void empty() {
            assertTrue(slot.canInsert(resource0, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.tryInsert(resource0, NORMAL_UNIT));
            assertEquals(NORMAL_UNIT, slot.insert(resource0, NORMAL_UNIT));

            assertEquals(NORMAL_UNIT, slot.getAmount());
        }

        @Test
        public void toCapacity() {
            assertTrue(slot.canInsert(resource0, CAPACITY));
            assertEquals(CAPACITY, slot.tryInsert(resource0, CAPACITY));
            assertEquals(CAPACITY, slot.insert(resource0, CAPACITY));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, slot.tryInsert(resource0, CAPACITY + SMALL_UNIT));
            assertEquals(CAPACITY, slot.insert(resource0, CAPACITY + SMALL_UNIT));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void preFill() {
            slot.set(resource0, NORMAL_UNIT);
            assertTrue(slot.canInsert(resource0, NORMAL_UNIT * 6));
            assertEquals(NORMAL_UNIT * 6, slot.tryInsert(resource0, NORMAL_UNIT * 6));
            assertEquals(NORMAL_UNIT * 6, slot.insert(resource0, NORMAL_UNIT * 6));

            assertEquals(NORMAL_UNIT * 7, slot.getAmount());
        }

        @Test
        public void preFill_tag() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT);
            assertTrue(slot.canInsert(resource0, components, NORMAL_UNIT * 6));
            assertEquals(NORMAL_UNIT * 6, slot.tryInsert(resource0, components, NORMAL_UNIT * 6));
            assertEquals(NORMAL_UNIT * 6, slot.insert(resource0, components, NORMAL_UNIT * 6));

            assertEquals(NORMAL_UNIT * 7, slot.getAmount());
        }

        @Test
        public void preFill_overCapacity() {
            slot.set(resource0, NORMAL_UNIT * 12);
            assertEquals(NORMAL_UNIT * 4, slot.tryInsert(resource0, NORMAL_UNIT * 7));
            assertEquals(NORMAL_UNIT * 4, slot.insert(resource0, NORMAL_UNIT * 7));

            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        public void preFill_overCapacity_tag() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT * 12);
            assertEquals(NORMAL_UNIT * 4, slot.tryInsert(resource0, components, NORMAL_UNIT * 7));
            assertEquals(NORMAL_UNIT * 4, slot.insert(resource0, components, NORMAL_UNIT * 7));

            assertEquals(CAPACITY, slot.getAmount());
        }
    }
}
