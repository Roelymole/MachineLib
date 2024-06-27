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
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.JUnitTest;
import dev.galacticraft.machinelib.test.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FluidResourceSlotExtractionTests implements JUnitTest {
    protected FluidResourceSlot slot;

    @BeforeEach
    public void setup() {
        this.slot = FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.any());
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

            assertFalse(slot.canExtract(FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(FluidConstants.BUCKET));
            assertEquals(0, slot.extract(FluidConstants.BUCKET));

            assertFalse(slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertFalse(slot.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertFalse(slot.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertNull(slot.extractOne());
        }

        @Test
        public void incorrectType() {
            slot.set(Fluids.LAVA, FluidConstants.BUCKET);

            assertFalse(slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, FluidConstants.BUCKET));
            assertFalse(slot.extractOne(Fluids.WATER));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void extractionTag() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(slot.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertFalse(slot.extractOne(Fluids.WATER, Utils.generateComponents()));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void containedTag() {
            slot.set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(slot.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertFalse(slot.extractOne(Fluids.WATER, DataComponentPatch.EMPTY));

            assertFalse(slot.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            slot.set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(slot.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, slot.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertFalse(slot.extractOne(Fluids.WATER, Utils.generateComponents()));

            assertFalse(slot.isEmpty());
        }
    }

    @Nested
    final class ExtractionSuccessTests {
        @Test
        public void exact_any() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.tryExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.extract(FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Fluids.WATER, components, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.tryExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.extract(Fluids.WATER, components, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, slot.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void excess_any() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.extract(FluidConstants.BOTTLE));

            assertEquals(slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, FluidConstants.BOTTLE));

            assertEquals(slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Fluids.WATER, components, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, components, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, components, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, components, FluidConstants.BOTTLE));

            assertEquals(slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_emptyNbt() {
            slot.set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(slot.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));

            assertEquals(slot.getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void insufficient_any() {
            slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, slot.extract(FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed() {
            slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            slot.set(Fluids.WATER, components, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, components, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            slot.set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, slot.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, slot.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertTrue(slot.isEmpty());
        }
    }
}
