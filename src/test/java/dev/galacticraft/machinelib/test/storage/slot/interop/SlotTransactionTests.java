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

package dev.galacticraft.machinelib.test.storage.slot.interop;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class SlotTransactionTests<Resource, Slot extends ResourceSlot<Resource>> implements MinecraftTest {
    protected static final long CAPACITY = 64;
    private static final long NORMAL_UNIT = 4;
    private static final long SMALL_UNIT = 1;

    private final Resource resource0;
    private final Resource resource1;
    
    private Slot slot;

    protected SlotTransactionTests(Resource resource0, Resource resource1) {
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
    final class TransactionCancelledTests {
        @Test
        public void extraction() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.extract(resource0, components, NORMAL_UNIT, transaction));

                assertTrue(slot.isEmpty());
                assertNull(slot.getResource());
                assertTrue(slot.getComponents().isEmpty());
                assertEquals(0, slot.getAmount());
            }

            assertFalse(slot.isEmpty());
        }

        @Test
        public void insertion() {
            DataComponentPatch components = Utils.generateComponents();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.insert(resource0, components, NORMAL_UNIT, transaction));

                assertEquals(resource0, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(NORMAL_UNIT, slot.getAmount());
            }

            assertTrue(slot.isEmpty());
            assertNull(slot.getResource());
            assertTrue(slot.getComponents().isEmpty());
            assertEquals(0, slot.getAmount());
        }

        @Test
        public void exchange() {
            DataComponentPatch components = Utils.generateComponents();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.insert(resource0, components, NORMAL_UNIT, transaction));

                assertEquals(resource0, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(NORMAL_UNIT, slot.getAmount());
            }

            assertTrue(slot.isEmpty());
            assertNull(slot.getResource());
            assertTrue(slot.getComponents().isEmpty());
            assertEquals(0, slot.getAmount());
        }
    }

    @Nested
    final class TransactionCommittedTests {
        @Test
        public void extraction() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(resource0, components, NORMAL_UNIT);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.extract(resource0, components, NORMAL_UNIT, transaction));

                assertTrue(slot.isEmpty());
                assertNull(slot.getResource());
                assertTrue(slot.getComponents().isEmpty());
                assertEquals(0, slot.getAmount());

                transaction.commit();
            }

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insertion() {
            DataComponentPatch components = Utils.generateComponents();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.insert(resource0, components, NORMAL_UNIT, transaction));

                assertEquals(resource0, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(NORMAL_UNIT, slot.getAmount());

                transaction.commit();
            }

            assertEquals(NORMAL_UNIT, slot.getAmount());
        }

        @Test
        public void exchange() {
            DataComponentPatch components = Utils.generateComponents();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(NORMAL_UNIT, slot.insert(resource0, components, NORMAL_UNIT, transaction));

                assertEquals(resource0, slot.getResource());
                assertEquals(components, slot.getComponents());

                assertEquals(NORMAL_UNIT, slot.getAmount());
                transaction.commit();
            }

            assertEquals(NORMAL_UNIT, slot.getAmount());
        }
    }
}
