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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.util.EnergySource;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A machine block entity that processes recipes.
 *
 * @param <I> The type of inventory the recipe type uses.
 * @param <R> The type of recipe the machine uses.
 */
public abstract class GeneratorMachineBlockEntity extends MachineBlockEntity {
    /**
     * The progress of the machine's current recipe.
     */
    private long energyGeneration = 0;
    private final EnergySource energySource = new EnergySource(this);

    /**
     * Constructs a new machine block entity that generates energy.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     */
    protected GeneratorMachineBlockEntity(@NotNull BlockEntityType<? extends GeneratorMachineBlockEntity> type, @NotNull BlockPos pos, BlockState state, StorageSpec spec) {
        super(type, pos, state, spec);
    }

    /**
     * {@return the machine status to use when the machine is working on a certain recipe}
     */
    @Contract(pure = true)
    protected abstract @NotNull MachineStatus workingStatus();

    /**
     * Tests if the necessary resources to run this machine are available.
     * This can be energy, fuel, or any other resource (or nothing!).
     *
     * @return {@code null} if the machine can run, or a {@link MachineStatus machine status} describing why it cannot.
     * @see #extractResourcesToWork()
     */
    protected abstract @Nullable MachineStatus hasResourcesToWork();

    /**
     * Extracts the necessary resources to run this machine.
     * This can be energy, fuel, or any other resource (or nothing!).
     *
     * @see #hasResourcesToWork()
     */
    protected abstract long calculateEnergyGeneration();

    @Override
    protected void tickDisabled(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.energyGeneration = 0;
    }

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("push_energy");
        this.energySource.trySpreadEnergy(level, pos, state);
        profiler.popPush("resources");
        MachineStatus status = this.hasResourcesToWork();
        profiler.pop();
        if (status == null) {
            if (this.energyStorage().isFull()) return MachineStatuses.CAPACITOR_FULL;
            profiler.push("transaction");
            this.energyGeneration = this.calculateEnergyGeneration();
            this.energyStorage().insert(this.energyGeneration);
            profiler.pop();
            return this.workingStatus();
        }
        return status;
    }

    /**
     * {@return the machine's current energy generation}
     */
    @Contract(pure = true)
    public long getEnergyGeneration() {
        return this.energyGeneration;
    }

    /**
     * Sets and returns the current energy generation of the machine.
     *
     * @param energyGeneration The energy generation to set.
     */
    @Contract(mutates = "this")
    public void setEnergyGeneration(long energyGeneration) {
        this.energyGeneration = energyGeneration;
    }

    // @Override
    // protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
    //     super.saveAdditional(tag, lookup);
    //     tag.putLong(Constant.Nbt.ENERGY_GENERATION, this.getEnergyGeneration());
    // }

    // @Override
    // public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
    //     super.loadAdditional(tag, lookup);
    //     this.energyGeneration = tag.getLong(Constant.Nbt.ENERGY_GENERATION);
    // }
}
