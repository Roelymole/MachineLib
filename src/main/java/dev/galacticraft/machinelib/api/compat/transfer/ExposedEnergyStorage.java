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

package dev.galacticraft.machinelib.api.compat.transfer;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.impl.storage.exposed.ExposedEnergyStorageImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

/**
 * An {@link EnergyStorage} that can be configured to restrict input and output.
 */
public interface ExposedEnergyStorage extends EnergyStorage {
    /**
     * Creates a new exposed energy storage.
     *
     * @param parent The parent storage object.
     * @param maxInsertion The maximum amount of energy that can be inserted in one transaction.
     * @param maxExtraction The maximum amount of energy that can be extracted in one transaction.
     * @return A new exposed energy storage.
     */
    @Contract("_, _, _ -> new")
    static @NotNull ExposedEnergyStorage create(@NotNull MachineEnergyStorage parent, long maxInsertion, long maxExtraction) {
        return new ExposedEnergyStorageImpl(parent, maxInsertion, maxExtraction);
    }
}
