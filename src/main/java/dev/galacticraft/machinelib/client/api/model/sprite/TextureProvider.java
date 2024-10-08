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

package dev.galacticraft.machinelib.client.api.model.sprite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.api.machine.MachineRenderData;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface TextureProvider<B extends TextureProvider.BoundTextureProvider> {
    Codec<Material> MATERIAL_CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("atlas").orElse(TextureAtlas.LOCATION_BLOCKS).forGetter(Material::atlasLocation),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(Material::texture)
            ).apply(instance, Material::new)),
            ResourceLocation.CODEC.comapFlatMap(tex -> DataResult.success(new Material(TextureAtlas.LOCATION_BLOCKS, tex)), Material::atlasLocation)
    );

    B bind(Function<Material, TextureAtlasSprite> atlas);

    interface BoundTextureProvider {
        TextureAtlasSprite getSprite(@Nullable MachineRenderData renderData, @NotNull BlockFace face);

        TextureAtlasSprite getParticle();
    }
}
