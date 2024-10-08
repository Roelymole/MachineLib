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

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.machine.MachineRenderData;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.model.sprite.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.sprite.TextureProvider;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class MachineBakedModel implements FabricBakedModel, BakedModel {
    private static final ItemTransforms ITEM_TRANSFORMATION = new ItemTransforms(
            new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
            new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
            new ItemTransform(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
            new ItemTransform(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
            ItemTransform.NO_TRANSFORM,
            new ItemTransform(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f)),
            new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0.2f, 0), new Vector3f(0.25f, 0.25f, 0.25f)),
            new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f))
    );

    private final TextureProvider.BoundTextureProvider provider;
    private final MachineTextureBase.Bound base;

    public MachineBakedModel(TextureProvider.BoundTextureProvider provider, MachineTextureBase.Bound base) {
        this.provider = provider;
        this.base = base;
    }

    private boolean transform(MachineRenderData renderData, @NotNull BlockState state, @NotNull MutableQuadView quad) {
        BlockFace face = BlockFace.from(state.getValue(BlockStateProperties.HORIZONTAL_FACING), quad.nominalFace());
        assert face != null;

        IOFace machineFace = renderData == null ? new IOFace() : renderData.getIOConfig().get(face);
        quad.spriteBake(getSprite(face,
                        renderData,
                        machineFace.getType(), machineFace.getFlow()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.color(-1, -1, -1, -1);
        return true;
    }

    private boolean transformItem(IOConfig config, @NotNull MutableQuadView quad) {
        BlockFace face = BlockFace.from(Direction.NORTH, quad.nominalFace());
        IOFace IOFace = config.get(face);
        assert face != null;
        quad.spriteBake(getSprite(face,
                        config,
                        IOFace.getType(), IOFace.getFlow()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.color(-1, -1, -1, -1);
        return true;
    }

    public TextureAtlasSprite getSprite(@NotNull BlockFace face, @Nullable MachineRenderData renderData, @NotNull ResourceType type, @NotNull ResourceFlow flow) {
        if (type == ResourceType.NONE) return this.provider.getSprite(renderData, face);

        switch (flow) {
            case INPUT -> {
                switch (type) {
                    case ENERGY -> {
                        return this.base.machineEnergyIn();
                    }
                    case ITEM -> {
                        return this.base.machineItemIn();
                    }
                    case FLUID -> {
                        return this.base.machineFluidIn();
                    }
                    case ANY -> {
                        return this.base.machineAnyIn();
                    }
                }
            }
            case OUTPUT -> {
                switch (type) {
                    case ENERGY -> {
                        return this.base.machineEnergyOut();
                    }
                    case ITEM -> {
                        return this.base.machineItemOut();
                    }
                    case FLUID -> {
                        return this.base.machineFluidOut();
                    }
                    case ANY -> {
                        return this.base.machineAnyOut();
                    }
                }
            }
            case BOTH -> {
                switch (type) {
                    case ENERGY -> {
                        return this.base.machineEnergyBoth();
                    }
                    case ITEM -> {
                        return this.base.machineItemBoth();
                    }
                    case FLUID -> {
                        return this.base.machineFluidBoth();
                    }
                    case ANY -> {
                        return this.base.machineAnyBoth();
                    }
                }
            }
        }

        return this.provider.getSprite(renderData, face);
    }

    public TextureProvider.BoundTextureProvider getProvider() {
        return provider;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // TODO: block entity can be null when loading the world, I don't think that's suppose to happen
        if (blockView.getBlockEntityRenderData(pos) instanceof MachineRenderData renderData) {
            context.pushTransform(quad -> transform(renderData, state, quad));
            for (Direction direction : Constant.Cache.DIRECTIONS) {
                context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
            }
            context.popTransform();
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        assert stack.getItem() instanceof BlockItem;
        assert ((BlockItem) stack.getItem()).getBlock() instanceof MachineBlock;
        CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

        IOConfig config = new IOConfig();
        if (!customData.isEmpty()) {
            CompoundTag beTag = customData.getUnsafe();
            if (beTag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND)) {
                CompoundTag confTag = beTag.getCompound(Constant.Nbt.CONFIGURATION);
                if (confTag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND)) {
                    config.readTag(confTag.getList(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND));
                }
            }
        }

        context.pushTransform(quad -> transformItem(config, quad));
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.provider.getParticle();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ITEM_TRANSFORMATION;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}