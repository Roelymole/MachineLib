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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.machine.MachineRenderData;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.*;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.AdjacentBlockApiCache;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.StorageHelper;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import dev.galacticraft.machinelib.impl.network.s2c.BaseMachineUpdatePayload;
import dev.galacticraft.machinelib.impl.network.s2c.SideConfigurationUpdatePayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A block entity that represents a machine.
 * <p>
 * This class handles three different types of storage and IO configurations:
 * {@link MachineEnergyStorage energy}, {@link MachineItemStorage item} and {@link MachineFluidStorage fluid} storage.
 *
 * @see MachineBlock
 * @see MachineMenu
 * @see MachineScreen
 */
public abstract class MachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<RegistryFriendlyByteBuf>, RenderDataBlockEntity {
    /**
     * The {@link MachineType type} of this machine.
     * It controls the storage configurations and applicable statuses for this machine.
     *
     * @see #getMachineType()
     */
    private final MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type;

    private final IOConfig configuration;
    private final SecuritySettings security;
    private @NotNull RedstoneMode redstone = RedstoneMode.IGNORE;

    /**
     * The {@link MachineState state} of this machine.
     * Stores the status and redstone power data
     *
     * @see #getState()
     */
    private final MachineState state;

    /**
     * The energy storage for this machine.
     *
     * @see #energyStorage()
     */
    private final @NotNull MachineEnergyStorage energyStorage;

    /**
     * The item storage for this machine.
     *
     * @see #itemStorage()
     */
    private final @NotNull MachineItemStorage itemStorage;

    /**
     * The fluid storage for this machine.
     *
     * @see #fluidStorage()
     */
    private final @NotNull MachineFluidStorage fluidStorage;

    /**
     * The text of the machine, to be passed to the screen handler factory for display.
     * <p>
     * By default, this is the name of the block.
     */
    @ApiStatus.Internal
    private final @NotNull Component name;

    /**
     * Caches energy storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<EnergyStorage> energyCache = null;
    /**
     * Caches fluid storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<Storage<FluidVariant>> fluidCache = null;
    /**
     * Caches item storages available from adjacent blocks.
     */
    @ApiStatus.Internal
    private @Nullable AdjacentBlockApiCache<Storage<ItemVariant>> itemCache = null;
    /**
     * Whether the machine will not drop items when broken.
     * <p>
     * Used for machines that are placed in structures to prevent players from obtaining too many resources for free.
     * Set via NBT.
     *
     * @see Constant.Nbt#DISABLE_DROPS
     */
    @ApiStatus.Internal
    private boolean disableDrops = false;

    /**
     * Whether the machine is currently active/working.
     * This covers both working/state active and redstone activity control.
     *
     * @see #isActive()
     */
    private boolean active = false;

    /**
     * Constructs a new machine block entity with the text automatically derived from the passed {@link BlockState}.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @see MachineBlockEntity#MachineBlockEntity(MachineType, BlockPos, BlockState, Component)
     */
    protected MachineBlockEntity(@NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type, @NotNull BlockPos pos, BlockState state) {
        this(type, pos, state, state.getBlock().getName().setStyle(Constant.Text.DARK_GRAY_STYLE));
    }

    /**
     * Constructs a new machine block entity.
     *
     * @param type  The type of block entity.
     * @param pos   The position of the machine in the level.
     * @param state The block state of the machine.
     * @param name  The text of the machine, to be passed to the screen handler.
     */
    protected MachineBlockEntity(@NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type, @NotNull BlockPos pos, BlockState state, @NotNull Component name) {
        super(type.getBlockEntityType(), pos, state);
        this.type = type;
        this.name = name;

        IOFace[] faces = new IOFace[6];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = new InternalIOFace(i);
        }
        this.configuration = new IOConfig(faces);
        this.security = new InternalSecuritySettings();

        this.state = new InternalMachineState();

        this.energyStorage = type.createEnergyStorage();
        this.energyStorage.setListener(this::setChanged);
        this.itemStorage = type.createItemStorage();
        this.itemStorage.setListener(this::setChanged);
        this.fluidStorage = type.createFluidStorage();
        this.fluidStorage.setListener(this::setChanged);
    }

    /**
     * Registers the transfer handlers for this machine.
     * <p>
     * This needs to be called for every block entity type that extends this class.
     * Otherwise, in-world resource transfer will not work.
     *
     * @param blocks the blocks to register.
     */
    public static void registerComponents(@NotNull Block... blocks) {
        EnergyStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedEnergyStorage(BlockFace.from(state, context));
            }
            return null;
        }, blocks);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedItemStorage(BlockFace.from(state, context));
            }
            return null;
        }, blocks);
        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity != null) {
                return ((MachineBlockEntity) blockEntity).getExposedFluidStorage(BlockFace.from(state, context));
            }
            return null;
        }, blocks);
    }

    /**
     * {@return the machine type of this block entity}
     */
    public @NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> getMachineType() {
        return this.type;
    }

    /**
     * The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can insert into items in its inventory (per transaction).
     * @see #drainPowerToSlot(int)
     * @see #getEnergyItemExtractionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemInsertionRate() {
        return (long) (this.energyStorage.getCapacity() / 160.0);
    }

    /**
     * The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     *
     * @return The maximum amount of energy that the machine can extract from items in its inventory (per transaction).
     * @see #chargeFromSlot(int)
     * @see #getEnergyItemInsertionRate()
     */
    @Contract(pure = true)
    public long getEnergyItemExtractionRate() {
        return (long) (this.energyStorage.getCapacity() / 160.0);
    }

    /**
     * Sets the redstone mode of this machine.
     *
     * @param redstone the redstone level level to use.
     * @see #getRedstoneMode()
     */
    @Contract(mutates = "this")
    public void setRedstoneMode(@NotNull RedstoneMode redstone) {
        this.redstone = redstone;
        this.setChanged();
    }

    /**
     * {@return the energy storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * {@return the item storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * {@return the fluid storage of this machine}
     */
    @Contract(pure = true)
    public final @NotNull MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * {@return the security settings of this machine} Used to determine who can interact with this machine.
     */
    @Contract(pure = true)
    public final @NotNull SecuritySettings getSecurity() {
        return this.security;
    }

    /**
     * {@return how the machine reacts when it interacts with redstone}
     */
    @Contract(pure = true)
    public final @NotNull RedstoneMode getRedstoneMode() {
        return this.redstone;
    }

    /**
     * {@return the IO configuration of this machine}
     */
    @Contract(pure = true)
    public final @NotNull IOConfig getIOConfig() {
        return this.configuration;
    }

    /**
     * {@return the state of this machine}
     */
    public @NotNull MachineState getState() {
        return this.state;
    }

    /**
     * {@return whether this machine will drop items when broken}
     */
    @Contract(pure = true)
    public boolean areDropsDisabled() {
        return this.disableDrops;
    }


    /**
     * {@return whether the current machine is enabled}
     * @see RedstoneMode
     */
    public boolean isDisabled() {
        return !this.redstone.isActive(this.state.isPowered());
    }

    /**
     * Updates the machine every tick.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller) for server-side logic that can be disabled (not called) arbitrarily.
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller) for the server-side logic that is always called.
     */
    public final void tickBase(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.setBlockState(state);
        profiler.push("constant");
        this.tickConstant(level, pos, state, profiler);
        profiler.pop();
        if (this.isDisabled()) {
            if (this.active) {
                MachineBlock.updateActiveState(level, pos, state, this.active = false);
            }
            profiler.push("disabled");
            this.tickDisabled(level, pos, state, profiler);
            profiler.pop();
        } else {
            profiler.push("active");
            this.state.setStatus(this.tick(level, pos, state, profiler));
            profiler.pop();
            if (!this.active) {
                if (this.state.isActive()) {
                    MachineBlock.updateActiveState(level, pos, state, this.active = true);
                }
            } else {
                if (!this.state.isActive()) {
                    MachineBlock.updateActiveState(level, pos, state, this.active = false);
                }
            }
        }
    }

    /**
     * Called every tick, even if the machine is not active/powered.
     * Use this to tick fuel consumption or transfer resources, for example.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick, when the machine is explicitly disabled (by redstone, for example).
     * Use this to clean-up resources leaked by {@link #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @see #tickBase(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tick(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected void tickDisabled(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
    }

    /**
     * Called every tick on the server, when the machine is active.
     * Use this to update crafting progress, for example.
     * Be sure to clean up state in {@link #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)}.
     *
     * @param level    the world.
     * @param pos      the position of this machine.
     * @param state    the block state of this machine.
     * @param profiler the world profiler.
     * @return the status of this machine.
     * @see #tickDisabled(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     * @see #tickConstant(ServerLevel, BlockPos, BlockState, ProfilerFiller)
     */
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /**
     * Returns whether the machine is currently active or not.
     * Not to be used while ticking.
     * A machine is active when its state is "working" AND redstone activity control allows activity.
     *
     * @return {@code true} if the machine is active, {@code false} otherwise.
     * @see #active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * {@return a controlled/throttled energy storage to expose to adjacent blocks}
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     */
    @ApiStatus.Internal
    private @Nullable EnergyStorage getExposedEnergyStorage(@Nullable BlockFace face) {
        if (face == null) return this.energyStorage.getExposedStorage(ResourceFlow.BOTH);
        return this.configuration.get(face).getExposedEnergyStorage(this.energyStorage);
    }

    /**
     * {@return a controlled/throttled item storage to expose to adjacent blocks}
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@Nullable BlockFace face) {
        if (face == null) return this.itemStorage.createExposedStorage(ResourceFlow.BOTH);
        return this.configuration.get(face).getExposedItemStorage(this.itemStorage::createExposedStorage);
    }

    /**
     * {@return a controlled/throttled fluid storage to expose to adjacent blocks}
     *
     * @param face the block face to get the exposed storages I/O configuration from.
     */
    @ApiStatus.Internal
    private @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@Nullable BlockFace face) {
        if (face == null) return this.fluidStorage.createExposedStorage(ResourceFlow.BOTH);
        return this.configuration.get(face).getExposedFluidStorage(this.fluidStorage::createExposedStorage);
    }

    /**
     * Serializes the machine's state to nbt.
     *
     * @param tag the nbt to serialize to.
     * @param lookup the registry lookup provider.
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.put(Constant.Nbt.ENERGY_STORAGE, this.energyStorage.createTag());
        tag.put(Constant.Nbt.ITEM_STORAGE, this.itemStorage.createTag());
        tag.put(Constant.Nbt.FLUID_STORAGE, this.fluidStorage.createTag());
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        tag.put(Constant.Nbt.SECURITY, this.security.createTag());
        tag.put(Constant.Nbt.REDSTONE_MODE, this.redstone.createTag());
        tag.put(Constant.Nbt.STATE, this.state.createTag());
        tag.putBoolean(Constant.Nbt.DISABLE_DROPS, this.disableDrops);
    }

    /**
     * Deserializes the machine's state from nbt.
     *
     * @param tag the nbt to deserialize from.
     * @param lookup the registry lookup provider.
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        if (tag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND))
            this.configuration.readTag(tag.getList(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND));
        if (tag.contains(Constant.Nbt.SECURITY, Tag.TAG_COMPOUND))
            this.security.readTag(tag.getCompound(Constant.Nbt.SECURITY));
        if (tag.contains(Constant.Nbt.REDSTONE_MODE, Tag.TAG_BYTE))
            this.redstone = RedstoneMode.readTag(tag.get(Constant.Nbt.REDSTONE_MODE));
        if (tag.contains(Constant.Nbt.STATE, Tag.TAG_COMPOUND))
            this.state.readTag(tag.getCompound(Constant.Nbt.STATE));
        if (tag.contains(Constant.Nbt.ENERGY_STORAGE, Tag.TAG_LONG))
            this.energyStorage.readTag(Objects.requireNonNull(((LongTag) tag.get(Constant.Nbt.ENERGY_STORAGE))));
        if (tag.contains(Constant.Nbt.ITEM_STORAGE, Tag.TAG_LIST))
            this.itemStorage.readTag(Objects.requireNonNull(tag.getList(Constant.Nbt.ITEM_STORAGE, Tag.TAG_COMPOUND)));
        if (tag.contains(Constant.Nbt.FLUID_STORAGE, Tag.TAG_LIST))
            this.fluidStorage.readTag(Objects.requireNonNull(tag.getList(Constant.Nbt.FLUID_STORAGE, Tag.TAG_COMPOUND)));
        this.disableDrops = tag.getBoolean(Constant.Nbt.DISABLE_DROPS);

        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(worldPosition, Blocks.AIR.defaultBlockState(), this.getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    /**
     * Pushes energy from this machine to adjacent capacitor blocks.
     *
     * @param level the level.
     * @param state the machine's block state.
     */
    protected void trySpreadEnergy(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.energyCache == null) {
            this.energyCache = AdjacentBlockApiCache.create(EnergyStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            EnergyStorage storage = this.getExposedEnergyStorage(BlockFace.from(facing, direction));
            if (storage != null && storage.supportsExtraction()) {
                EnergyStorageUtil.move(storage, this.energyCache.find(direction), Long.MAX_VALUE, null);
            }
        }
    }

    /**
     * Pushes fluids from this machine to adjacent fluid storages.
     *
     * @param level the level.
     * @param state the machine's block state.
     */
    protected void trySpreadFluids(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.fluidCache == null) {
            this.fluidCache = AdjacentBlockApiCache.create(FluidStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            ExposedStorage<Fluid, FluidVariant> storage = this.getExposedFluidStorage(BlockFace.from(facing, direction));
            if (storage != null && storage.supportsExtraction()) {
                StorageHelper.moveAll(storage, this.fluidCache.find(direction), Long.MAX_VALUE, null); //TODO: fluid I/O cap
            }
        }
    }

    /**
     * Pushes items from this machine to adjacent item storages.
     *
     * @param level the level
     * @param state the machine's block state
     */
    protected void trySpreadItems(@NotNull ServerLevel level, @NotNull BlockState state) {
        if (this.itemCache == null) {
            this.itemCache = AdjacentBlockApiCache.create(ItemStorage.SIDED, level, this.worldPosition);
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            Storage<ItemVariant> storage = this.getExposedItemStorage(BlockFace.from(facing, direction));
            if (storage != null && storage.supportsExtraction()) {
                StorageHelper.moveAll(storage, this.itemCache.find(direction), Long.MAX_VALUE, null);
            }
        }
    }

    /**
     * Tries to extract energy from an item in the specified slot into this machine.
     *
     * @param slot the index of the input slot.
     */
    protected void chargeFromSlot(int slot) {
        if (this.energyStorage().isFull()) return;

        EnergyStorage energyStorage = this.itemStorage.slot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsExtraction()) {
            EnergyStorageUtil.move(energyStorage, this.energyStorage, this.getEnergyItemExtractionRate(), null);
        }
    }

    /**
     * Tries to drain power from this machine's energy storage and insert it into the item in the given slot.
     *
     * @param slot the index of the input slot.
     */
    protected void drainPowerToSlot(int slot) {
        if (this.energyStorage().isEmpty()) return;
        EnergyStorage energyStorage = this.itemStorage.slot(slot).find(EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.supportsInsertion()) {
            EnergyStorageUtil.move(this.energyStorage, energyStorage, this.getEnergyItemInsertionRate(), null);
        }
    }

    /**
     * Tries to extract the specified fluid from the item in the input slot
     * and move it into the tank of the fluid storage.
     *
     * @param inputSlot the index of the input slot from which the fluid will be extracted
     * @param tankSlot the index of the tank where the fluid will be moved to
     * @param fluid the fluid to be extracted and stored
     */
    protected void takeFluidFromSlot(int inputSlot, int tankSlot, @NotNull Fluid fluid) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isFull()) return;
        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsExtraction()) {
            StorageHelper.move(FluidVariant.of(fluid), storage, tank, Integer.MAX_VALUE, null);
        }
    }

    /**
     * Tries to extract fluids from the item in the input slot that match the slot's filter
     * and moves them into the tank of the fluid storage.
     *
     * @param inputSlot the index of the input slot from which the fluid will be extracted
     * @param tankSlot the index of the tank where the fluid will be moved to
     */
    protected void takeFluidFromSlot(int inputSlot, int tankSlot) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isFull()) return;
        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsExtraction()) {
            StorageHelper.move(storage, tank, Integer.MAX_VALUE, null);
        }
    }

    /**
     * Tries to extract fluids from the fluid storage and moves them into the tank of the item in the input slot.
     *
     * @param inputSlot the index of the input slot where the fluid will be inserted
     * @param tankSlot the index of the tank from which the fluid will be extracted
     */
    protected void drainFluidToSlot(int inputSlot, int tankSlot) {
        FluidResourceSlot tank = this.fluidStorage().slot(tankSlot);
        if (tank.isEmpty()) return;

        ItemResourceSlot slot = this.itemStorage.slot(inputSlot);
        Storage<FluidVariant> storage = slot.find(FluidStorage.ITEM);
        if (storage != null && storage.supportsInsertion()) {
            StorageHelper.move(tank, storage, Integer.MAX_VALUE, null);
        }
    }

    @Override
    public RegistryFriendlyByteBuf getScreenOpeningData(ServerPlayer player) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        if (!this.getSecurity().hasAccess(player)) {
            MachineLib.LOGGER.error("Player {} has illegally accessed machine at {}", player.getStringUUID(), this.worldPosition);
        }

        buf.writeBlockPos(this.getBlockPos());
        this.configuration.writePacket(buf);
        this.security.writePacket(buf);
        this.redstone.writePacket(buf);
        this.state.writePacket(buf);
        this.energyStorage.writePacket(buf);
        this.itemStorage.writePacket(buf);
        this.fluidStorage.writePacket(buf);

        return buf;
    }

    @Override
    public @NotNull BlockEntityType<? extends MachineBlockEntity> getType() {
        return this.type.getBlockEntityType();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.name;
    }

    @Override
    public @NotNull MachineRenderData getRenderData() {
        return this.configuration;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag tag = new CompoundTag();
        tag.put(Constant.Nbt.CONFIGURATION, this.configuration.createTag());
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // safe cast because the ClientCommonPacketListener is a superclass of ClientGamePacketListener
        // noinspection unchecked, rawtypes
        return (Packet) new ClientboundCustomPayloadPacket(new BaseMachineUpdatePayload(this.worldPosition, this.configuration));
    }

    /**
     * Broadcasts an update to all players tracking this machine (within view distance).
     * @param payload the packet to send
     */
    protected void broadcastUpdate(CustomPacketPayload payload) {
        if (this.level != null && !this.level.isClientSide) {
            for (ServerPlayer player : ((ServerLevel) MachineBlockEntity.this.level).getChunkSource().chunkMap.getPlayers(new ChunkPos(MachineBlockEntity.this.worldPosition), false)) {
                ServerPlayNetworking.getSender(player).sendPacket(payload);
            }
        }
    }

    /**
     * Marks the block entity for re-rendering.
     */
    public void markForRerender() {
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 0);
        }
    }

    public void awardUsedRecipes(@NotNull ServerPlayer player, @NotNull Set<ResourceLocation> recipes) {
        for (ResourceLocation id : recipes) {
            Optional<RecipeHolder<?>> optional = player.serverLevel().getRecipeManager().byKey(id);
            if (optional.isPresent()) {
                player.awardRecipes(Collections.singleton(optional.get()));
                player.triggerRecipeCrafted(optional.get(), Collections.emptyList());
            }
        }
    }

    /**
     * Subclass that tracks modifications to save the block entity.
     */
    private class InternalMachineState extends MachineState {
        @Override
        public void setStatus(@Nullable MachineStatus status) {
            if (this.status != status) {
                this.status = status;
                MachineBlockEntity.this.setChanged();
            }
        }

        @Override
        public void setPowered(boolean powered) {
            if (this.powered != powered) {
                this.powered = powered;
                MachineBlockEntity.this.setChanged();
            }
        }
    }

    /**
     * Subclass that tracks modifications to save the block entity.
     */
    private class InternalSecuritySettings extends SecuritySettings {
        @Override
        public void tryUpdate(@NotNull Player player) {
            if (this.owner == null) {
                this.owner = player.getUUID();
                MachineBlockEntity.this.setChanged();
            }
        }

        @Override
        public void setAccessLevel(@NotNull AccessLevel accessLevel) {
            if (this.accessLevel != accessLevel) {
                this.accessLevel = accessLevel;
                MachineBlockEntity.this.setChanged();
            }
        }
    }

    /**
     * Subclass that tracks modifications to save and re-render the block entity.
     */
    private class InternalIOFace extends IOFace {
        private final BlockFace face;
        private @Nullable ExposedStorage<Item, ItemVariant> cachedItemStorage = null;
        private @Nullable ExposedStorage<Fluid, FluidVariant> cachedFluidStorage = null;
        private @Nullable EnergyStorage cachedEnergyStorage = null;

        public InternalIOFace(int i) {
            super(ResourceType.NONE, ResourceFlow.BOTH);
            this.face = BlockFace.values()[i];
        }

        @Override
        public void setOption(@NotNull ResourceType type, @NotNull ResourceFlow flow) {
            if (this.type != type || this.flow != flow) {
                this.type = type;
                this.flow = flow;

                MachineBlockEntity.this.setChanged();
                if (MachineBlockEntity.this.level.isClientSide) {
                    MachineBlockEntity.this.markForRerender();
                } else {
                    MachineBlockEntity.this.broadcastUpdate(new SideConfigurationUpdatePayload(MachineBlockEntity.this.worldPosition, this.face, type, flow));
                }

                this.cachedItemStorage = null;
                this.cachedFluidStorage = null;
                this.cachedEnergyStorage = null;
            }
        }

        @Override
        public @Nullable ExposedStorage<Item, ItemVariant> getExposedItemStorage(@NotNull StorageProvider<Item, ItemVariant> provider) {
            if (this.type.willAcceptResource(ResourceType.ITEM)) {
                if (this.cachedItemStorage == null) {
                    this.cachedItemStorage = provider.createExposedStorage(this.flow);
                }
                return this.cachedItemStorage;
            } else {
                assert this.cachedItemStorage == null;
            }
            return null;
        }

        @Override
        public @Nullable ExposedStorage<Fluid, FluidVariant> getExposedFluidStorage(@NotNull StorageProvider<Fluid, FluidVariant> provider) {
            if (this.type.willAcceptResource(ResourceType.FLUID)) {
                if (this.cachedFluidStorage == null) {
                    this.cachedFluidStorage = provider.createExposedStorage(this.flow);
                }
                return this.cachedFluidStorage;
            } else {
                assert this.cachedFluidStorage == null;
            }
            return null;
        }

        @Override
        public @Nullable EnergyStorage getExposedEnergyStorage(@NotNull MachineEnergyStorage storage) {
            if (this.type.willAcceptResource(ResourceType.ENERGY)) {
                if (this.cachedEnergyStorage == null) {
                    this.cachedEnergyStorage = storage.getExposedStorage(this.flow);
                }
                return this.cachedEnergyStorage;
            }
            return null;
        }

        @Override
        public void readTag(@NotNull ByteTag tag) {
            this.cachedItemStorage = null;
            this.cachedFluidStorage = null;
            this.cachedEnergyStorage = null;

            super.readTag(tag);
        }


        @Override
        public void readPacket(@NotNull ByteBuf buf) {
            this.cachedItemStorage = null;
            this.cachedFluidStorage = null;
            this.cachedEnergyStorage = null;

            super.readPacket(buf);
        }
    }
}
