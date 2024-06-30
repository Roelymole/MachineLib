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

package dev.galacticraft.machinelib.impl.storage.builder;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.test.MinecraftTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidResourceSlotBuilderTest implements MinecraftTest {
    FluidResourceSlot.Builder builder;
    
    @BeforeEach
    void setup() {
        this.builder = FluidResourceSlot.builder(InputType.STORAGE);
    }

    @Test
    void invalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> builder.capacity(0).build());
    }

    @Test
    void capacity() {
        FluidResourceSlot slot = builder.capacity(FluidConstants.BUCKET * 64).build();

        assertEquals(FluidConstants.BUCKET * 64, slot.getCapacity());
    }

    @Test
    void hidden() {
        builder.hidden();

        assertThrows(UnsupportedOperationException.class, () -> builder.x(0));
        assertThrows(UnsupportedOperationException.class, () -> builder.y(0));
        assertThrows(UnsupportedOperationException.class, () -> builder.width(0));
        assertThrows(UnsupportedOperationException.class, () -> builder.height(0));

        assertNull(builder.build().getDisplay());
        assertTrue(builder.build().isHidden());
    }

    @Test
    void hiddenPost() {
        builder.x(10).hidden();

        assertThrows(UnsupportedOperationException.class, () -> builder.build());
    }

    @Test
    void height() {
        FluidResourceSlot slot = builder.height(16).build();

        assertEquals(16, slot.getDisplay().height());
    }

    @Test
    void invalidHeight() {
        builder.height(-4);

        assertThrows(IllegalArgumentException.class, () -> builder.build());
    }

    @Test
    void width() {
        FluidResourceSlot slot = builder.width(16).build();

        assertEquals(16, slot.getDisplay().width());
    }

    @Test
    void invalidWidth() {
        builder.width(-4);

        assertThrows(IllegalArgumentException.class, () -> builder.build());
    }

    @Test
    void displayPosition() {
        FluidResourceSlot slot = builder.pos(11, 43).build();

        assertEquals(11, slot.getDisplay().x());
        assertEquals(43, slot.getDisplay().y());
    }

    @Test
    void displayPositionXY() {
        FluidResourceSlot slot = builder.x(5).y(7).build();

        assertEquals(5, slot.getDisplay().x());
        assertEquals(7, slot.getDisplay().y());
    }

    @Test
    void defaultFilter() {
        FluidResourceSlot slot = builder.build();

        assertSame(ResourceFilters.any(), slot.getFilter());
    }

    @Test
    void filter() {
        FluidResourceSlot slot = builder.filter(ResourceFilters.none()).build();

        assertSame(ResourceFilters.none(), slot.getFilter());
        assertEquals(1, slot.insert(Fluids.WATER, 1)); // filters only affect players
    }
}
