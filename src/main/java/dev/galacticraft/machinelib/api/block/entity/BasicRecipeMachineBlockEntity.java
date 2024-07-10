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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A machine block entity that processes recipes.
 *
 * @param <C> The type of inventory the recipe type uses.
 * @param <R> The type of recipe the machine uses.
 */
public abstract class BasicRecipeMachineBlockEntity<C extends Container, R extends Recipe<C>> extends RecipeMachineBlockEntity<C, R> {
    /**
     * An inventory for use in finding vanilla recipes for this machine.
     */
    protected final @NotNull C craftingInv;

    protected final SlottedStorageAccess<Item, ItemResourceSlot> inputSlots;
    protected final SlottedStorageAccess<Item, ItemResourceSlot> outputSlots;

    /**
     * Constructs a new machine block entity that processes recipes.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     * @param inputSlot The index of the recipe input slot.
     * @param outputSlot The index of the recipe output slot.
     */
    protected BasicRecipeMachineBlockEntity(BlockEntityType<? extends BasicRecipeMachineBlockEntity<C, R>> type,
                                            BlockPos pos, BlockState state, RecipeType<R> recipeType, StorageSpec spec, int inputSlot, int outputSlot) {
        this(type, pos, state, recipeType, spec, inputSlot, 1, outputSlot);
    }

    /**
     * Constructs a new machine block entity that processes recipes.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     * @param inputSlots The index of the first recipe input slot.
     * @param inputSlotsLen The number of recipe input slots.
     * @param outputSlot The index of the recipe output slot.
     */
    protected BasicRecipeMachineBlockEntity(BlockEntityType<? extends BasicRecipeMachineBlockEntity<C, R>> type,
                                            BlockPos pos, BlockState state, RecipeType<R> recipeType, StorageSpec spec, int inputSlots, int inputSlotsLen, int outputSlot) {
        this(type, pos, state, recipeType, spec, inputSlots, inputSlotsLen, outputSlot, 1);
    }

    /**
     * Constructs a new machine block entity that processes recipes.
     *
     * @param type The type of block entity.
     * @param pos The position of the machine in the level.
     * @param state The block state of the machine.
     * @param recipeType The type of recipe to be processed.
     * @param inputSlots The index of the first recipe input slot.
     * @param inputSlotsLen The number of recipe input slots.
     * @param outputSlots The index of the first recipe output slot.
     * @param outputSlotsLen The number of recipe output slots.
     */
    protected BasicRecipeMachineBlockEntity(BlockEntityType<? extends BasicRecipeMachineBlockEntity<C, R>> type,
                                            BlockPos pos, BlockState state, RecipeType<R> recipeType, StorageSpec spec, int inputSlots, int inputSlotsLen, int outputSlots, int outputSlotsLen) {
        super(type, pos, state, recipeType, spec);

        this.inputSlots = this.itemStorage().subStorage(inputSlots, inputSlotsLen);
        this.outputSlots = this.itemStorage().subStorage(outputSlots, outputSlotsLen);

        this.craftingInv = this.createCraftingInv();
    }

    protected abstract C createCraftingInv();

    /**
     * Creates an inventory for use in finding vanilla recipes for this machine.
     * NOTE: This inventory can assume that it is never modified - do not modify it!
     *
     * @return The crafting inventory of the machine.
     */
    @Override
    @Contract(pure = true)
    protected @NotNull C craftingInv() {
        return this.craftingInv;
    }

    /**
     * Inserts the active recipe's output into the machine's inventory.
     *
     * @param recipe The recipe to output.
     */
    @Override
    protected void outputStacks(@NotNull RecipeHolder<R> recipe) {
        ItemStack assembled = recipe.value().assemble(this.craftingInv(), this.level.registryAccess());
        this.outputSlots.insertMatching(assembled.getItem(), assembled.getComponentsPatch(), assembled.getCount());
    }

    /**
     * Checks if the machine can output stacks for the given recipe.
     *
     * @param recipe The recipe to check.
     * @return {@code true} if the machine can output stacks for the recipe, {@code false} otherwise.
     */
    @Override
    protected boolean canOutputStacks(@NotNull RecipeHolder<R> recipe) {
        ItemStack assembled = recipe.value().assemble(this.craftingInv(), this.level.registryAccess());
        return this.inputSlots.canInsert(assembled.getItem(), assembled.getComponentsPatch(), assembled.getCount());
    }

    /**
     * Extracts the recipe's input from the machine's inventory.
     *
     * @param recipe The recipe to extract.
     */
    @Override
    protected void extractCraftingMaterials(@NotNull RecipeHolder<R> recipe) {
        for (ItemResourceSlot slot : this.inputSlots) {
            slot.consumeOne();
        }
    }
}
