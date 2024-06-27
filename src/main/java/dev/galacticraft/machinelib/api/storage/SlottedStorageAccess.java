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

package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A storage that can store multiple of multiple instances of one type of resource (e.g., 10 sticks and 3 snowballs).
 *
 * @param <Resource> The type of resource (e.g., item) this storage can store. Must be comparable by identity.
 * @param <Slot> The type of slot this storage contains.
 * @see StorageAccess
 */
public interface SlottedStorageAccess<Resource, Slot extends StorageAccess<Resource>> extends StorageAccess<Resource>, Iterable<Slot> {
    /**
     * {@return the number of slots in this storage}
     */
    int size();

    /**
     * {@return a slotted storage that is a subset of this storage}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     */
    SlottedStorageAccess<Resource, Slot> subStorage(int start, int len);

    /**
     * {@return a slotted storage that is a subset of this storage}
     *
     * @param slots the indices of the slots to include
     */
    SlottedStorageAccess<Resource, Slot> subStorage(int... slots);

    // START SLOT METHODS

    /**
     * {@return the resource stored in the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getResource()
     */
    @Nullable Resource getResource(int slot);

    /**
     * {@return the amount of the resource stored in the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getAmount()
     */
    long getAmount(int slot);

    /**
     * {@return the components of the resource stored in the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getComponents()
     */
    @Nullable DataComponentPatch getComponents(int slot);

    /**
     * {@return the capacity of the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getCapacity()
     */
    long getCapacity(int slot);

    /**
     * {@return the capacity of the slot at the given index for a specific resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check the capacity for
     * @param components the components of the resource to check the capacity for
     * @see ResourceSlot#getCapacityFor(Object, DataComponentPatch)
     */
    long getCapacityFor(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components);

    /**
     * {@return the real capacity of the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getRealCapacity()
     */
    long getRealCapacity(int slot);

    /**
     * {@return the filter for the slot at the given index}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#getFilter()
     */
    @NotNull ResourceFilter<Resource> getFilter(int slot);

    /**
     * {@return whether the slot at the given index is empty}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#isEmpty()
     */
    boolean isEmpty(int slot);

    /**
     * {@return whether the slot at the given index is full}
     *
     * @param slot the index of the slot
     * @see ResourceSlot#isFull()
     */
    boolean isFull(int slot);

    /**
     * {@return whether the slot at the given index can accept the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @see ResourceSlot#canInsert(Object)
     */
    boolean canInsert(int slot, @NotNull Resource resource);

    /**
     * {@return whether the slot at the given index can accept the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @see ResourceSlot#canInsert(Object, DataComponentPatch)
     */
    boolean canInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components);

    /**
     * {@return whether the slot at the given index can accept the given amount of the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canInsert(Object, long)
     */
    boolean canInsert(int slot, @NotNull Resource resource, long amount);

    /**
     * {@return whether the slot at the given index can accept the given amount of the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canInsert(Object, DataComponentPatch, long)
     */
    boolean canInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * {@return the amount of the given resource that the slot at the given index can accept}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryInsert(Object, DataComponentPatch, long)
     */
    long tryInsert(int slot, @NotNull Resource resource, long amount);

    /**
     * {@return the amount of the given resource that the slot at the given index can accept}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryInsert(Object, DataComponentPatch, long)
     */
    long tryInsert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Inserts the given amount of the given resource into the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#insert(Object, DataComponentPatch, long)
     */
    long insert(int slot, @NotNull Resource resource, long amount);

    /**
     * Inserts the given amount of the given resource into the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to insert
     * @param components the components of the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#insert(Object, DataComponentPatch, long)
     */
    long insert(int slot, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * {@return whether the slot at the given index contains the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @see ResourceSlot#contains(Object)
     */
    boolean contains(int slot, @NotNull Resource resource);

    /**
     * {@return whether the slot at the given index contains the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @see ResourceSlot#contains(Object, DataComponentPatch)
     */
    boolean contains(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * {@return whether the slot at the given index can extract the given amount resources}
     *
     * @param slot the index of the slot
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canExtract(Object, long)
     */
    boolean canExtract(int slot, long amount);

    /**
     * {@return whether the slot at the given index can extract the given amount of the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canExtract(Object, long)
     */
    boolean canExtract(int slot, @NotNull Resource resource, long amount);

    /**
     * {@return whether the slot at the given index can extract the given amount of the given resource}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canExtract(Object, DataComponentPatch, long)
     */
    boolean canExtract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * {@return the amount of the given resource that the slot at the given index can extract}
     *
     * @param slot the index of the slot
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryExtract(long)
     */
    long tryExtract(int slot, long amount);

    /**
     * {@return the amount of the given resource that the slot at the given index can extract}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryExtract(Object, DataComponentPatch, long)
     */
    long tryExtract(int slot, @NotNull Resource resource, long amount);

    /**
     * {@return the amount of the given resource that the slot at the given index can extract}
     *
     * @param slot the index of the slot
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryExtract(Object, DataComponentPatch, long)
     */
    long tryExtract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * Extracts a single resource from the slot at the given index.
     *
     * @param slot the index of the slot
     * @return the extracted resource, or {@code null} if the slot is empty
     * @see ResourceSlot#extractOne()
     */
    @Nullable Resource extractOne(int slot);

    /**
     * Extracts the given amount of resources from the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to extract
     * @return whether a resource was extracted
     * @see ResourceSlot#extract(long)
     */
    boolean extractOne(int slot, @NotNull Resource resource);

    /**
     * Extracts the given amount of resources from the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to extract
     * @param components the components of the resource to extract
     * @return whether a resource was extracted
     * @see ResourceSlot#extract(long)
     */
    boolean extractOne(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * Extracts the given amount of resources from the slot at the given index.
     *
     * @param slot the index of the slot
     * @param amount the amount of the resource to extract
     * @return the amount of resources extracted
     * @see ResourceSlot#extract(long)
     */
    long extract(int slot, long amount);

    /**
     * Extracts the given amount of resources from the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to extract
     * @param amount the amount of the resource to extract
     * @return the amount of resources extracted
     * @see ResourceSlot#extract(long)
     */
    long extract(int slot, @NotNull Resource resource, long amount);

    /**
     * Extracts the given amount of resources from the slot at the given index.
     *
     * @param slot the index of the slot
     * @param resource the resource to extract
     * @param components the components of the resource to extract
     * @param amount the amount of the resource to extract
     * @return the amount of resources extracted
     * @see ResourceSlot#extract(long)
     */
    long extract(int slot, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    // END SLOT METHODS

    // START RANGE METHODS

    /**
     * {@return whether all slots in the given range of slots are empty}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @see ResourceSlot#isEmpty()
     */
    boolean isEmpty(int start, int len);

    /**
     * {@return whether all slots in the given range of slots are full}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @see ResourceSlot#isFull()
     */
    boolean isFull(int start, int len);

    /**
     * {@return whether any of the slots in the given range of slots can accept the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @see ResourceSlot#canInsert(Object)
     */
    boolean canInsert(int start, int len, @NotNull Resource resource);

    /**
     * {@return whether any of the slots in the given range of slots can accept the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @see ResourceSlot#canInsert(Object, DataComponentPatch)
     */
    boolean canInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components);

    /**
     * {@return whether any combination of the slots in the given range of slots can accept the given amount of the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canInsert(Object, long)
     */
    boolean canInsert(int start, int len, @NotNull Resource resource, long amount);

    /**
     * {@return whether any combination of the slots in the given range of slots can accept the given amount of the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canInsert(Object, DataComponentPatch, long)
     */
    boolean canInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * {@return the amount of the given resource that the slots in the given range of slots can accept}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryInsert(Object, DataComponentPatch, long)
     */
    long tryInsert(int start, int len, @NotNull Resource resource, long amount);

    /**
     * {@return the amount of the given resource that the slots in the given range of slots can accept}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryInsert(Object, DataComponentPatch, long)
     */
    long tryInsert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Inserts the given amount of the given resource into the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#insert(Object, DataComponentPatch, long)
     */
    long insert(int start, int len, @NotNull Resource resource, long amount);

    /**
     * Inserts the given amount of the given resource into the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to insert
     * @param components the components of the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#insert(Object, DataComponentPatch, long)
     */
    long insert(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * Inserts the given amount of the given resource into the slots in the given range of slots.
     * If the resource is already present in the range, the resources are first inserted onto the existing resources.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#contains(Object)
     */
    long insertMatching(int start, int len, @NotNull Resource resource, long amount);

    /**
     * Inserts the given amount of the given resource into the slots in the given range of slots.
     * If the resource is already present in the range, the resources are first inserted onto the existing resources.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to insert
     * @param components the components of the resource to insert
     * @param amount the amount of the resource to insert
     * @return the amount of the resource that was inserted
     * @see ResourceSlot#contains(Object, DataComponentPatch)
     */
    long insertMatching(int start, int len, @NotNull Resource resource, @NotNull DataComponentPatch components, long amount);

    /**
     * {@return whether any of the slots in the given range of slots contains the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @see ResourceSlot#contains(Object)
     */
    boolean contains(int start, int len, @NotNull Resource resource);

    /**
     * {@return whether any of the slots in the given range of slots contains the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @see ResourceSlot#contains(Object, DataComponentPatch)
     */
    boolean contains(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * {@return whether any of the slots in the given range of slots can extract the given amount resources}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canExtract(Object, long)
     */
    boolean canExtract(int start, int len, @NotNull Resource resource, long amount);

    /**
     * {@return whether any of the slots in the given range of slots can extract the given amount of the given resource}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#canExtract(Object, DataComponentPatch, long)
     */
    boolean canExtract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * {@return the amount of the given resource that the slots in the given range of slots can extract}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryExtract(Object, long)
     */
    long tryExtract(int start, int len, @NotNull Resource resource, long amount);

    /**
     * {@return the amount of the given resource that the slots in the given range of slots can extract}
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to check
     * @param components the components of the resource to check
     * @param amount the amount of the resource to check
     * @see ResourceSlot#tryExtract(Object, DataComponentPatch, long)
     */
    long tryExtract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    /**
     * Extracts a single resource from the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to extract
     * @return the extracted resource, or {@code null} if no resource was extracted
     * @see ResourceSlot#extractOne(Object)
     */
    boolean extractOne(int start, int len, @NotNull Resource resource);

    /**
     * Extracts a single resource from the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to extract
     * @param components the components of the resource to extract
     * @return whether a resource was extracted
     * @see ResourceSlot#extractOne(Object, DataComponentPatch)
     */
    boolean extractOne(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components);

    /**
     * Extracts the given amount of resources from the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param amount the amount of the resource to extract
     * @return the amount of resources extracted
     * @see ResourceSlot#extract(Object, long)
     */
    long extract(int start, int len, @NotNull Resource resource, long amount);

    /**
     * Extracts the given amount of resources from the slots in the given range of slots.
     *
     * @param start the start of the range (inclusive)
     * @param len the length of the range
     * @param resource the resource to extract
     * @param components the components of the resource to extract
     * @param amount the amount of the resource to extract
     * @return the amount of resources extracted
     * @see ResourceSlot#extract(Object, DataComponentPatch, long)
     */
    long extract(int start, int len, @NotNull Resource resource, @Nullable DataComponentPatch components, long amount);

    // END RANGE METHODS
}
