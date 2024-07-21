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
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlotImpl;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.util.Utils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FluidStorageExtractionTest implements MinecraftTest {
    protected MachineFluidStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = MachineFluidStorage.create(FluidResourceSlot.create(TransferType.STORAGE, TankDisplay.create(0, 0), FluidConstants.BUCKET * 16, ResourceFilters.any()));
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

            assertFalse(storage.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertFalse(storage.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertFalse(storage.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertFalse(storage.extractOne(Fluids.WATER));
        }

        @Test
        public void incorrectType() {
            storage.slot(0).set(Fluids.LAVA, FluidConstants.BUCKET);

            assertFalse(storage.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, FluidConstants.BUCKET));
            assertFalse(storage.extractOne(Fluids.WATER));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void extractionTag() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(storage.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertFalse(storage.extractOne(Fluids.WATER, Utils.generateComponents()));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void containedTag() {
            storage.slot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(storage.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertFalse(storage.extractOne(Fluids.WATER, DataComponentPatch.EMPTY));

            assertFalse(storage.isEmpty());
        }

        @Test
        public void mismatchedTag() {
            storage.slot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(storage.canExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.tryExtract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.extract(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertFalse(storage.extractOne(Fluids.WATER, Utils.generateComponents()));

            assertFalse(storage.isEmpty());
        }
    }

    @Nested
    final class ExtractionSuccessTests {
        @Test
        public void exact_typed() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void exact_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Fluids.WATER, components, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.tryExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.extract(Fluids.WATER, components, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void exact_typed_emptyNbt() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void excess_typed() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, FluidConstants.BOTTLE));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Fluids.WATER, components, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, components, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, components, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, components, FluidConstants.BOTTLE));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void excess_typed_emptyNbt() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertTrue(storage.canExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BOTTLE));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET - FluidConstants.BOTTLE);
        }

        @Test
        public void insufficient_typed() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void insufficient_typed_nbt() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Fluids.WATER, components, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, components, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, components, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }

        @Test
        public void insufficient_typed_emptyNbt() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BOTTLE);

            assertEquals(FluidConstants.BOTTLE, storage.tryExtract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BOTTLE, storage.extract(Fluids.WATER, DataComponentPatch.EMPTY, FluidConstants.BUCKET));

            assertTrue(storage.isEmpty());
        }
    }
}
