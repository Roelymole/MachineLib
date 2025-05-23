/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.item;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class FixedItemBackedFluidStorage extends ItemBackedFluidStorage {
    private final long maxIn;
    private final long maxOut;
    private final long capacity;

    public FixedItemBackedFluidStorage(ItemStack stack, ContainerItemContext context, ResourceFilter<Fluid> filter, long maxIn, long maxOut, long capacity) {
        super(stack, context, filter);
        this.maxIn = maxIn;
        this.maxOut = maxOut;
        this.capacity = capacity;
    }

    @Override
    protected long getRawMaxInput() {
        return this.maxIn;
    }

    @Override
    protected long getRawMaxOutput() {
        return this.maxOut;
    }

    @Override
    protected long getRawCapacity() {
        return this.capacity;
    }
}
