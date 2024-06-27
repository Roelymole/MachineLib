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

package dev.galacticraft.machinelib.test.storage.interop;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class FluidResourceSlotTransactionTests implements JUnitTest {
    private static final long CAPACITY = FluidConstants.BUCKET * 16;
    private FluidResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), CAPACITY, ResourceFilters.any());
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    @Nested
    final class TransactionCancelledTests {
        @Test
        public void extraction() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Fluids.WATER, components, FluidConstants.BUCKET * 8);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 8, slot.extract(Fluids.WATER, components, FluidConstants.BUCKET * 8, transaction));

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
                assertEquals(FluidConstants.BUCKET * 5, slot.insert(Fluids.WATER, components, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());
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
                assertEquals(FluidConstants.BUCKET * 5, slot.insert(Fluids.WATER, components, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());
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
            slot.set(Fluids.WATER, components, FluidConstants.BUCKET * 8);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 8, slot.extract(Fluids.WATER, components, FluidConstants.BUCKET * 8, transaction));

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
                assertEquals(FluidConstants.BUCKET * 5, slot.insert(Fluids.WATER, components, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, slot.getResource());
                assertEquals(components, slot.getComponents());
                assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());

                transaction.commit();
            }

            assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());
        }

        @Test
        public void exchange() {
            DataComponentPatch components = Utils.generateComponents();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidConstants.BUCKET * 5, slot.insert(Fluids.WATER, components, FluidConstants.BUCKET * 5, transaction));

                assertEquals(Fluids.WATER, slot.getResource());
                assertEquals(components, slot.getComponents());

                assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());
                transaction.commit();
            }

            assertEquals(FluidConstants.BUCKET * 5, slot.getAmount());
        }
    }
}
