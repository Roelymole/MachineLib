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
import dev.galacticraft.machinelib.test.Utils;
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
    protected MachineFluidStorage group;

    @BeforeEach
    public void setup() {
        this.group = MachineFluidStorage.create(FluidResourceSlot.create(InputType.STORAGE, TankDisplay.create(0, 0), CAPACITY, ResourceFilters.any()));
    }

    @AfterEach
    public void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.group.getSlot(0)).isSane());
    }

    @Nested
    final class InsertionFailureTests {
        @Test
        public void full() {
            group.getSlot(0).set(Fluids.WATER, CAPACITY);

            assertFalse(group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, group.insert(Fluids.WATER, FluidConstants.BUCKET));
        }

        @Test
        public void incorrectType() {
            group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(group.canInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, group.tryInsert(Fluids.LAVA, FluidConstants.BUCKET));
            assertEquals(0, group.insert(Fluids.LAVA, FluidConstants.BUCKET));

            assertEquals(group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void insertTag() {
            group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);

            assertFalse(group.canInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, group.tryInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, group.insert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertEquals(group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void containedTag() {
            group.getSlot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(0, group.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(group.getAmount(0), FluidConstants.BUCKET);
        }

        @Test
        public void mismatchedTag() {
            group.getSlot(0).set(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET);

            assertFalse(group.canInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, group.tryInsert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));
            assertEquals(0, group.insert(Fluids.WATER, Utils.generateComponents(), FluidConstants.BUCKET));

            assertEquals(group.getAmount(0), FluidConstants.BUCKET);
        }
    }

    @Nested
    final class InsertionSuccessTests {
        @Test
        public void empty() {
            assertTrue(group.canInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, group.tryInsert(Fluids.WATER, FluidConstants.BUCKET));
            assertEquals(FluidConstants.BUCKET, group.insert(Fluids.WATER, FluidConstants.BUCKET));

            assertEquals(FluidConstants.BUCKET, group.getAmount(0));
        }

        @Test
        public void toCapacity() {
            assertTrue(group.canInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, group.tryInsert(Fluids.WATER, CAPACITY));
            assertEquals(CAPACITY, group.insert(Fluids.WATER, CAPACITY));

            assertEquals(CAPACITY, group.getAmount(0));
        }

        @Test
        public void overCapacity() {
            assertEquals(CAPACITY, group.tryInsert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));
            assertEquals(CAPACITY, group.insert(Fluids.WATER, CAPACITY + FluidConstants.BOTTLE));

            assertEquals(CAPACITY, group.getAmount(0));
        }

        @Test
        public void preFill() {
            group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET);
            assertTrue(group.canInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, group.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, group.insert(Fluids.WATER, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, group.getAmount(0));
        }

        @Test
        public void preFill_tag() {
            DataComponentPatch components = Utils.generateComponents();
            group.getSlot(0).set(Fluids.WATER, components, FluidConstants.BUCKET);
            assertTrue(group.canInsert(Fluids.WATER, components, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, group.tryInsert(Fluids.WATER, components, FluidConstants.BUCKET * 6));
            assertEquals(FluidConstants.BUCKET * 6, group.insert(Fluids.WATER, components, FluidConstants.BUCKET * 6));

            assertEquals(FluidConstants.BUCKET * 7, group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity() {
            group.getSlot(0).set(Fluids.WATER, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, group.tryInsert(Fluids.WATER, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, group.insert(Fluids.WATER, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, group.getAmount(0));
        }

        @Test
        public void preFill_overCapacity_tag() {
            DataComponentPatch components = Utils.generateComponents();
            group.getSlot(0).set(Fluids.WATER, components, FluidConstants.BUCKET * 12);
            assertEquals(FluidConstants.BUCKET * 4, group.tryInsert(Fluids.WATER, components, FluidConstants.BUCKET * 7));
            assertEquals(FluidConstants.BUCKET * 4, group.insert(Fluids.WATER, components, FluidConstants.BUCKET * 7));

            assertEquals(CAPACITY, group.getAmount(0));
        }
    }
}
