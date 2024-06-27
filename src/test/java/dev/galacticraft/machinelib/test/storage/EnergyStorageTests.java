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

package dev.galacticraft.machinelib.test.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import dev.galacticraft.machinelib.test.MinecraftTest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.LongTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnergyStorageTests implements MinecraftTest {
    private static final long CAPACITY = 2000;
    private MachineEnergyStorage storage;

    @BeforeEach
    public void setup() {
        this.storage = new MachineEnergyStorageImpl(CAPACITY, 0, 0, true, true);
    }

    @Test
    void canExtract() {
        this.storage.setEnergy(1000);

        assertTrue(this.storage.canExtract(1000));
        assertTrue(this.storage.canExtract(500));
        assertFalse(this.storage.canExtract(1001));
    }

    @Test
    void canInsert() {
        this.storage.setEnergy(1000);

        assertTrue(this.storage.canInsert(1000));
        assertTrue(this.storage.canInsert(500));
        assertFalse(this.storage.canInsert(1001));
    }

    @Test
    void tryExtract() {
        this.storage.setEnergy(1000);

        assertEquals(1000, this.storage.tryExtract(1000));
        assertEquals(500, this.storage.tryExtract(500));
        assertEquals(1000, this.storage.tryExtract(1001));
    }

    @Test
    void tryInsert() {
        this.storage.setEnergy(1000);

        assertEquals(1000, this.storage.tryInsert(1000));
        assertEquals(500, this.storage.tryInsert(500));
        assertEquals(1000, this.storage.tryInsert(1001));
    }

    @Test
    void extract() {
        this.storage.setEnergy(1000);
        assertEquals(1000, this.storage.extract(1000));
        assertEquals(0, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertEquals(500, this.storage.extract(500));
        assertEquals(500, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertEquals(1000, this.storage.extract(1001));
        assertEquals(0, this.storage.getAmount());
    }

    @Test
    void insert() {
        this.storage.setEnergy(1000);
        assertEquals(1000, this.storage.insert(1000));
        assertEquals(2000, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertEquals(500, this.storage.insert(500));
        assertEquals(1500, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertEquals(1000, this.storage.insert(1001));
        assertEquals(2000, this.storage.getAmount());
    }

    @Test
    void extractExact() {
        this.storage.setEnergy(1000);
        assertTrue(this.storage.extractExact(1000));
        assertEquals(0, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertTrue(this.storage.extractExact(500));
        assertEquals(500, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertFalse(this.storage.extractExact(1001));
        assertEquals(1000, this.storage.getAmount());
    }

    @Test
    void insertExact() {
        this.storage.setEnergy(1000);
        assertTrue(this.storage.insertExact(1000));
        assertEquals(2000, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertTrue(this.storage.insertExact(500));
        assertEquals(1500, this.storage.getAmount());

        this.storage.setEnergy(1000);
        assertFalse(this.storage.insertExact(1001));
        assertEquals(1000, this.storage.getAmount());
    }

    @Test
    void getAmount() {
        this.storage.setEnergy(1000);
        assertEquals(1000, this.storage.getAmount());
    }

    @Test
    void getCapacity() {
        assertEquals(CAPACITY, this.storage.getCapacity());
    }

    @Test
    void isFull() {
        this.storage.setEnergy(1000);
        assertFalse(this.storage.isFull());

        this.storage.setEnergy(2000);
        assertTrue(this.storage.isFull());
    }

    @Test
    void isEmpty() {
        this.storage.setEnergy(1000);
        assertFalse(this.storage.isEmpty());

        this.storage.setEnergy(0);
        assertTrue(this.storage.isEmpty());
    }

    @Test
    void nbtSerialization() {
        this.storage.setEnergy(1000);

        LongTag tag = this.storage.createTag();
        this.storage.setEnergy(0);
        this.storage.readTag(tag);

        assertEquals(1000, this.storage.getAmount());
    }

    @Test
    void packetSerialization() {
        this.storage.setEnergy(1000);
        ByteBuf buf = Unpooled.buffer();

        this.storage.writePacket(buf);
        this.storage.setEnergy(0);
        this.storage.readPacket(buf);

        assertEquals(1000, this.storage.getAmount());
    }
}
