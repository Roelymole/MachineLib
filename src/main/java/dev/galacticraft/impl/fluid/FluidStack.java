/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.impl.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, null, 0);
    private final @NotNull Fluid fluid;
    private @Nullable NbtCompound nbt;
    private long amount;
    private boolean empty;

    public FluidStack(@NotNull Fluid fluid, @Nullable NbtCompound nbt, long amount) {
        this.fluid = fluid;
        this.nbt = nbt;
        this.amount = amount;
        if (amount == 0 || this.fluid == Fluids.EMPTY) {
            this.empty = true;
        }
        this.empty = this.testEmpty();
    }

    public FluidStack(FluidVariant variant, long amount) {
        this(variant.getFluid(), variant.copyNbt(), amount);
    }

    public void setAmount(long amount) {
        this.amount = amount;
        this.empty = this.testEmpty();
    }

    @Contract(pure = true)
    public @Nullable NbtCompound getNbt() {
        return this.nbt;
    }

    @Contract()
    public @NotNull NbtCompound getOrCreateNbt() {
        if (this.nbt == null) {
            this.nbt = new NbtCompound();
        }
        return this.nbt;
    }

    private boolean testEmpty() {
        if (this == EMPTY) {
            return true;
        } else if (this.getFluid() != Fluids.EMPTY) {
            return this.amount <= 0;
        } else {
            return true;
        }
    }

    public Fluid getFluid() {
        return this.empty ? Fluids.EMPTY : this.fluid;
    }

    public long getAmount() {
        return this.empty ? 0 : this.amount;
    }

    public boolean isEmpty() {
        return this.empty;
    }
}
