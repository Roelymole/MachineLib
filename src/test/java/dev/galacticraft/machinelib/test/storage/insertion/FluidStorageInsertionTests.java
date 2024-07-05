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
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
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

public class FluidStorageInsertionTests implements MinecraftTest {
    private static final long CAPACITY = FluidConstants.BUCKET * 16;
    protected MachineFluidStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = MachineFluidStorage.create(FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), CAPACITY, ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.storage.slot(0)).isSane());
    }

    @Nested
    final class InsertionFailureTests {
        @Test
        public void full() {
            storage.slot(0).set(Fluids.WATER, CAPACITY);

            assertFalse(storage.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.insert(Fluids.WATER, FluidConstants.BUCKET));
        }

        @Test
        public void incorrectType() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(storage.canInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, storage.tryInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, storage.insert(Fluids.LAVA, FluidConstants.BUCKET));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET);
        }

        @Test
        public void insertTag() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(storage.canInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.tryInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.insert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET);
        }

        @Test
        public void containedTag() {
            storage.slot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(storage.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, storage.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET);
        }

        @Test
        public void mismatchedTag() {
            storage.slot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(storage.canInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.tryInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, storage.insert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertEquals(storage.slot(0).getAmount(), FluidConstants.BUCKET);
        }
    }

    @Nested
    final class InsertionSuccessTests {
        @Test
        public void empty() {
            assertTrue(storage.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, storage.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(FluidConstants.BUCKET, storage.slot(0).getAmount());
        }

        @Test
        public void toCapacity() {
            assertTrue(storage.canInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, storage.tryInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, storage.insert(Fluids.WATER, CAPACITY));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, storage.tryInsert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));
            assertEquals(CAPACITY, storage.insert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void preFill() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET);
            assertTrue(storage.canInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, storage.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, storage.insert(Fluids.WATER, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_tag() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Fluids.WATER, components, FluidConstants.BUCKET);
            assertTrue(storage.canInsert(Fluids.WATER, components, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, storage.tryInsert(Fluids.WATER, components, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, storage.insert(Fluids.WATER, components, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_overCapacity() {
            storage.slot(0).set(Fluids.WATER, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, storage.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, storage.insert(Fluids.WATER, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }

        @Test
        public void preFill_overCapacity_tag() {
            DataComponentPatch components = Utils.generateComponents();
            storage.slot(0).set(Fluids.WATER, components, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, storage.tryInsert(Fluids.WATER, components, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, storage.insert(Fluids.WATER, components, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, storage.slot(0).getAmount());
        }
    }
}
