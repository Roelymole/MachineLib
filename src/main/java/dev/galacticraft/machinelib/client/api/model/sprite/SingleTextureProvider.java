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
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record SingleTextureProvider(Material material) implements TextureProvider<SingleTextureProvider.Bound> {
    public static final Codec<SingleTextureProvider> CODEC = MATERIAL_CODEC.xmap(SingleTextureProvider::new, SingleTextureProvider::material);

    public static final TextureProvider<?> MISSING_TEXTURE = new SingleTextureProvider(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("missingno")));
    public static final Codec<TextureProvider<?>> MISSING_CODEC = Codec.unit(MISSING_TEXTURE);

    @Override
    public Bound bind(Function<Material, TextureAtlasSprite> atlas) {
        return new Bound(atlas.apply(this.material));
    }

    public record Bound(TextureAtlasSprite texture) implements BoundTextureProvider {
        @Override
        public TextureAtlasSprite getSprite(@Nullable BlockState state, @NotNull BlockFace face) {
            return this.texture;
        }

        @Override
        public TextureAtlasSprite getParticle() {
            return this.texture;
        }
    }
}
