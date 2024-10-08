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

package dev.galacticraft.machinelib.client.impl.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.client.api.model.sprite.TextureProvider;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MachineModelLoadingPlugin implements PreparableModelLoadingPlugin<Map<ResourceLocation, JsonObject>>, ModelResolver {
    public static final MachineModelLoadingPlugin INSTANCE = new MachineModelLoadingPlugin();
    private final Map<ResourceLocation, UnbakedModel> pendingItemModels = new HashMap<>();
    private Map<ResourceLocation, JsonObject> data = null;

    @Override
    public @Nullable UnbakedModel resolveModel(ModelResolver.Context context) {
        assert this.data != null;
        JsonObject json = this.data.remove(context.id());
        if (json != null) {
            Codec<? extends TextureProvider<?>> codec = MachineModelRegistry.getProviderFactoryOrDefault(ResourceLocation.parse(GsonHelper.getAsString(json, MachineModelRegistry.MARKER)));
            DataResult<? extends Pair<? extends TextureProvider<?>, JsonElement>> sprites = codec.decode(JsonOps.INSTANCE, json.getAsJsonObject("data"));
            JsonElement baseId = json.get("base");
            ResourceLocation base = baseId == null ? context.id().withPath("base") : ResourceLocation.parse(baseId.getAsString());
            MachineUnbakedModel model = new MachineUnbakedModel(sprites.getOrThrow().getFirst(), base);
            this.pendingItemModels.put(ResourceLocation.fromNamespaceAndPath(context.id().getNamespace(), context.id().getPath().replace("machine/", "item/")), model);
            return model;
        }
        return this.pendingItemModels.remove(context.id());
    }

    @Override
    public void onInitializeModelLoader(Map<ResourceLocation, JsonObject> data, ModelLoadingPlugin.Context pluginContext) {
        this.data = data;
        this.pendingItemModels.clear();
        pluginContext.resolveModel().register(this);
    }
}
