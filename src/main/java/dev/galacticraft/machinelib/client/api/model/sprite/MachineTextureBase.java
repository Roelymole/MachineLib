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

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record MachineTextureBase(Material machineEnergyIn, Material machineEnergyOut, Material machineEnergyBoth,
                                 Material machineItemIn, Material machineItemOut, Material machineItemBoth,
                                 Material machineFluidIn, Material machineFluidOut, Material machineFluidBoth,
                                 Material machineAnyIn, Material machineAnyOut, Material machineAnyBoth
) {

    public static MachineTextureBase prefixed(String id, String prefix) {
        return new MachineTextureBase(
                mat(id, prefix + "_energy_input"), mat(id, prefix + "_energy_output"), mat(id, prefix + "_energy_both"),
                mat(id, prefix + "_item_input"), mat(id, prefix + "_item_output"), mat(id, prefix + "_item_both"),
                mat(id, prefix + "_fluid_input"), mat(id, prefix + "_fluid_output"), mat(id, prefix + "_fluid_both"),
                mat(id, prefix + "_any_input"), mat(id, prefix + "_any_output"), mat(id, prefix + "_any_both")
        );
    }

    public Bound bind(Function<Material, TextureAtlasSprite> atlas) {
        return new Bound(
                atlas.apply(this.machineEnergyIn), atlas.apply(this.machineEnergyOut), atlas.apply(this.machineEnergyBoth),
                atlas.apply(this.machineItemIn), atlas.apply(this.machineItemOut), atlas.apply(this.machineItemBoth),
                atlas.apply(this.machineFluidIn), atlas.apply(this.machineFluidOut), atlas.apply(this.machineFluidBoth),
                atlas.apply(this.machineAnyIn), atlas.apply(this.machineAnyOut), atlas.apply(this.machineAnyBoth)
        );
    }

    public record Bound(TextureAtlasSprite machineEnergyIn, TextureAtlasSprite machineEnergyOut, TextureAtlasSprite machineEnergyBoth,
                        TextureAtlasSprite machineItemIn, TextureAtlasSprite machineItemOut, TextureAtlasSprite machineItemBoth,
                        TextureAtlasSprite machineFluidIn, TextureAtlasSprite machineFluidOut, TextureAtlasSprite machineFluidBoth,
                        TextureAtlasSprite machineAnyIn, TextureAtlasSprite machineAnyOut, TextureAtlasSprite machineAnyBoth) {

    }

    private static Material mat(String namespace, String location) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(namespace, location));
    }
}
