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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record FrontSidedSpriteProvider(Material front, Material base, Material side) implements TextureProvider<FrontSidedSpriteProvider.Bound> {
    public static final Codec<FrontSidedSpriteProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MATERIAL_CODEC.fieldOf("front").forGetter(FrontSidedSpriteProvider::front),
            MATERIAL_CODEC.fieldOf("base").forGetter(FrontSidedSpriteProvider::base),
            MATERIAL_CODEC.fieldOf("side").forGetter(FrontSidedSpriteProvider::side)
    ).apply(instance, FrontSidedSpriteProvider::new));

    @Override
    public Bound bind(Function<Material, TextureAtlasSprite> atlas) {
        return new Bound(atlas.apply(this.front), atlas.apply(this.base), atlas.apply(this.side));
    }

    public record Bound(TextureAtlasSprite front, TextureAtlasSprite base, TextureAtlasSprite side) implements TextureProvider.BoundTextureProvider {
        @Override
        public TextureAtlasSprite getSprite(@Nullable BlockState state, @NotNull BlockFace face) {
            if (face == BlockFace.FRONT) return this.front;
            if (face.side()) return this.side;
            return this.base;
        }

        @Override
        public TextureAtlasSprite getParticle() {
            return this.base;
        }
    }
}
