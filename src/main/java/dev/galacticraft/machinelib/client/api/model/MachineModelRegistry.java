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

package dev.galacticraft.machinelib.client.api.model;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import dev.galacticraft.machinelib.client.api.model.sprite.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.sprite.SingleTextureProvider;
import dev.galacticraft.machinelib.client.api.model.sprite.TextureProvider;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.client.impl.model.MachineModelRegistryImpl;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry for {@link MachineBakedModel} sprite providers.
 */
public final class MachineModelRegistry {
    public static final String MARKER = "machinelib:generate";

    /**
     * Registers a sprite provider for a block.
     *
     * @param id the id to register the provider for
     * @param codec the provider to register
     */
    public static void register(@NotNull ResourceLocation id, @NotNull Codec<? extends TextureProvider<?>> codec) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(codec);

        MachineModelRegistryImpl.FACTORIES.put(id, codec);
    }

    public static void registerBase(@NotNull ResourceLocation id, @NotNull MachineTextureBase bundle) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(bundle);

        MachineModelRegistryImpl.TEXTURE_BASES.put(id, bundle);
    }

    /**
     * {@return the registered provider, or null if none is registered}
     *
     * @param providerId the provider id to get the provider for
     */
    public static @Nullable Codec<? extends TextureProvider<?>> getProviderFactory(@NotNull ResourceLocation providerId) {
        return MachineModelRegistryImpl.FACTORIES.get(providerId);
    }

    /**
     * {@return the registered provider, or {@link SingleTextureProvider#MISSING_CODEC} if none is registered}
     *
     * @param providerId the provider id to get the provider for
     */
    public static @NotNull Codec<? extends TextureProvider<?>> getProviderFactoryOrDefault(@NotNull ResourceLocation providerId) {
        return MachineModelRegistryImpl.FACTORIES.getOrDefault(providerId, SingleTextureProvider.CODEC);
    }
}
