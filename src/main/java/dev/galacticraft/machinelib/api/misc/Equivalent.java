package dev.galacticraft.machinelib.api.misc;

import org.jetbrains.annotations.NotNull;

public interface Equivalent<T> {
    boolean hasChanged(T previous);

    void copyInto(T other);
}
