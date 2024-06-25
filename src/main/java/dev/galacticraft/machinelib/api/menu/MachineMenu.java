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

package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.client.impl.menu.sync.MachineDataClient;
import dev.galacticraft.machinelib.impl.compat.vanilla.RecipeOutputStorageSlot;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import dev.galacticraft.machinelib.impl.menu.sync.MachineDataImpl;
import io.netty.buffer.ByteBufAllocator;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Base container menu for machines.
 *
 * @param <Machine> The type of machine block entity this menu is linked to.
 */
public class MachineMenu<Machine extends MachineBlockEntity> extends AbstractContainerMenu {
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> BUF_IDENTITY_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf src, RegistryFriendlyByteBuf dst) {
            dst.writeBytes(src);
        }

        @Override
        public RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf src) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(src.capacity()), src.registryAccess());
            buf.writeBytes(src);
            return src;
        }
    };

    /**
     * The machine type associated with this menu.
     */
    public final @NotNull MachineType<?, ?> type;

    /**
     * The machine that is the source of this menu.
     */
    @ApiStatus.Internal
    public final @NotNull Machine machine;
    /**
     * Whether the menu exists on the logical server.
     */
    public final boolean server;

    /**
     * The level this menu exists in
     */
    public final @NotNull ContainerLevelAccess levelAccess;
    /**
     * The player interacting with this menu.
     * Always null on the logical client, never null on the logical server.
     */
    public final @NotNull Player player;

    /**
     * The inventory of the player opening this menu
     */
    public final @NotNull Inventory playerInventory;
    /**
     * The UUID of the player interacting with this menu.
     */
    public final @NotNull UUID playerUUID;

    public final @NotNull MachineIOConfig configuration;
    public final @NotNull SecuritySettings security;
    public @NotNull RedstoneMode redstoneMode;

    /**
     * The state of the machine associated with this menu
     */
    public final @NotNull MachineState state;
    /**
     * The energy storage of the machine associated with this menu
     */
    public final @NotNull MachineEnergyStorage energyStorage;
    /**
     * The item storage of the machine associated with this menu
     */
    public final @NotNull MachineItemStorage itemStorage;
    /**
     * The fluid storage of the machine associated with this menu
     */
    public final @NotNull MachineFluidStorage fluidStorage;

    /**
     * The machine this menu is for.
     */
    public final StorageSlot[] machineSlots;
    /**
     * The tanks contained in this menu.
     */
    public final List<Tank> tanks = new ArrayList<>();

    private final MachineData data;

    /**
     * Constructs a new menu for a machine.
     * Called on the logical server
     *
     * @param syncId  The sync id for this menu.
     * @param player  The player who is interacting with this menu.
     * @param machine The machine this menu is for.
     */
    public MachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine) {
        super(machine.getMachineType().getMenuType(), syncId);
        assert !Objects.requireNonNull(machine.getLevel()).isClientSide;
        this.type = machine.getMachineType();
        this.machine = machine;
        this.data = new MachineDataImpl(player);
        this.server = true;

        this.player = player;
        this.playerInventory = player.getInventory();
        this.playerUUID = player.getUUID();

        this.configuration = machine.getIOConfig();
        this.security = machine.getSecurity();
        this.redstoneMode = machine.getRedstoneMode();

        this.state = machine.getState();
        this.energyStorage = machine.energyStorage();
        this.itemStorage = machine.itemStorage();
        this.fluidStorage = machine.fluidStorage();

        this.levelAccess = ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos());

        this.machineSlots = new StorageSlot[this.itemStorage.size()];

        int index = 0;
        for (ItemResourceSlot slot : this.itemStorage) {
            ItemSlotDisplay display = slot.getDisplay();
            if (display != null) {
                StorageSlot slot1;
                if (slot.inputType() == InputType.RECIPE_OUTPUT) {
                    slot1 = new RecipeOutputStorageSlot(this.itemStorage, slot, display, index, player, machine);
                } else {
                    slot1 = new StorageSlot(this.itemStorage, slot, display, index, this.playerUUID);
                }
                this.addSlot(slot1);
                this.machineSlots[index++] = slot1;
            }
        }

        index = 0;
        for (FluidResourceSlot slot : this.fluidStorage) {
            if (!slot.isHidden()) {
                TankDisplay display = slot.getDisplay();
                assert display != null;
                this.addTank(Tank.create(slot, display, slot.inputType(), index++));
            }
        }

        this.addPlayerInventorySlots(player.getInventory(), 0, 0); // it's the server
        this.registerData(this.data);
        this.data.synchronizeFull();
    }

    /**
     * Constructs a new menu for a machine.
     * Called on the logical client
     *
     * @param syncId    The sync id for this menu.
     * @param buf       The synchronization buffer from the server.
     * @param inventory The inventory of the player interacting with this menu.
     * @param type      The type of menu this is.
     */
    protected MachineMenu(int syncId, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf buf, int invX, int invY, @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(type.getMenuType(), syncId);

        this.type = type;
        this.server = false;
        this.data = new MachineDataClient();
        this.player = inventory.player;
        this.playerInventory = inventory;
        this.playerUUID = inventory.player.getUUID();

        BlockPos blockPos = buf.readBlockPos();
        this.machine = (Machine) inventory.player.level().getBlockEntity(blockPos); //todo: actually stop using the BE on the client side
        this.levelAccess = ContainerLevelAccess.create(inventory.player.level(), blockPos);
        this.configuration = new MachineIOConfig();
        this.configuration.readPacket(buf);
        this.security = new SecuritySettings();
        this.security.readPacket(buf);
        this.redstoneMode = RedstoneMode.readPacket(buf);

        this.state = new MachineState();
        this.state.readPacket(buf);
        this.energyStorage = type.createEnergyStorage();
        this.energyStorage.readPacket(buf);
        this.itemStorage = type.createItemStorage();
        this.itemStorage.readPacket(buf);
        this.fluidStorage = type.createFluidStorage();
        this.fluidStorage.readPacket(buf);

        this.machineSlots = new StorageSlot[this.itemStorage.size()];

        int index = 0;
        for (ItemResourceSlot slot : this.itemStorage) {
            ItemSlotDisplay display = slot.getDisplay();
            if (display != null) {
                StorageSlot slot1;
                if (slot.inputType() == InputType.RECIPE_OUTPUT) {
                    slot1 = new RecipeOutputStorageSlot(this.itemStorage, slot, display, index, inventory.player, machine);
                } else {
                    slot1 = new StorageSlot(this.itemStorage, slot, display, index, this.playerUUID);
                }
                this.addSlot(slot1);
                this.machineSlots[index++] = slot1;
            }
        }

        index = 0;
        for (FluidResourceSlot slot : this.fluidStorage) {
            if (!slot.isHidden()) {
                TankDisplay display = slot.getDisplay();
                assert display != null;
                this.addTank(Tank.create(slot, display, slot.inputType(), index++));
            }
        }

        this.addPlayerInventorySlots(inventory, invX, invY);
        this.registerData(this.data);
    }

    /**
     * Creates a new menu type for a machine.
     *
     * @param factory       The factory used to create the machine menu.
     * @param typeSupplier  The supplier for the machine type.
     * @param <Machine>     The type of the machine block entity.
     * @param <Menu>        The type of the machine menu.
     * @return The created menu type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(@NotNull MachineMenuFactory<Machine, Menu> factory, Supplier<MachineType<Machine, Menu>> typeSupplier) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> factory.create(syncId, inventory, buf, typeSupplier.get()), BUF_IDENTITY_CODEC);
    }

    /**
     * Creates a new menu type for a machine with a basic factory.
     *
     * @param factory The factory used to create the machine menu.
     * @param <Machine> The type of the machine block entity.
     * @param <Menu> The type of the machine menu.
     * @return The created menu type.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(@NotNull BasicMachineMenuFactory<Machine, Menu> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, BUF_IDENTITY_CODEC);
    }

    /**
     * Creates a new menu type for a machine with a simple factory.
     *
     * @param invX The x-coordinate of the top-left player inventory slot.
     * @param invY The y-coordinate of the top-left player inventory slot.
     * @param typeSupplier The supplier for the machine type.
     * @param <Machine> The type of the machine block entity.
     * @return The created menu type.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(int invX, int invY, Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new MachineMenu<>(syncId, inventory, buf, invX, invY, typeSupplier.get()), BUF_IDENTITY_CODEC);
    }

    /**
     * Creates a new menu type for a machine with a simple factory.
     *
     * @param invY The y-coordinate of the top-left player inventory slot.
     * @param typeSupplier The supplier for the machine type.
     * @param <Machine> The type of the machine block entity.
     * @return The created menu type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(int invY, Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(8, invY, typeSupplier);
    }

    /**
     * Creates a new menu type for a machine with a simple factory.
     *
     * @param typeSupplier The supplier for the machine type.
     * @param <Machine> The type of the machine block entity.
     * @return The created menu type.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(84, typeSupplier);
    }

    /**
     * Registers the sync handlers for the menu.
     */
    @MustBeInvokedByOverriders
    public void registerData(MachineData data) {
        data.register(this.itemStorage, new long[this.itemStorage.size()]); //todo: probably synced by vanilla - is this necessary?
        data.register(this.fluidStorage, new long[this.fluidStorage.size()]);
        data.register(this.energyStorage, new long[1]);
        data.register(this.configuration, new MachineIOConfig());
        data.register(this.security, new SecuritySettings());
        data.registerEnum(RedstoneMode.values(), () -> this.redstoneMode, mode -> this.redstoneMode = mode);
        data.register(this.state, new MachineState());
    }

    /**
     * Returns whether the given face can be configured for input or output.
     *
     * @param face the block face to test.
     * @return whether the given face can be configured for input or output.
     */
    public boolean isFaceLocked(@NotNull BlockFace face) {
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotId) { //return LEFTOVER (in slot)
        Slot slot = this.slots.get(slotId);
        int size = this.machineSlots.length;

        if (slotId < size) {
            assert slot instanceof StorageSlot;
            ResourceSlot<Item> slot1 = ((StorageSlot) slot).getSlot();
            this.quickMoveIntoPlayerInventory(slot1);
            return ItemStackUtil.create(slot1);
        } else {
            assert !(slot instanceof StorageSlot);
            ItemStack stack1 = slot.getItem();
            if (stack1.isEmpty()) return ItemStack.EMPTY;

            long insert = stack1.getCount();
            for (StorageSlot slot1 : this.machineSlots) {
                if (slot1.getSlot().inputType().playerInsertion() && slot1.getSlot().contains(stack1.getItem(), stack1.getComponentsPatch())) {
                    insert -= slot1.getSlot().insert(stack1.getItem(), stack1.getComponentsPatch(), insert);
                    if (insert == 0) break;
                }
            }

            if (insert == 0) {
                slot.set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            } else {
                for (StorageSlot slot1 : this.machineSlots) {
                    if (slot1.mayPlace(stack1)) {
                        insert -= slot1.getSlot().insert(stack1.getItem(), stack1.getComponentsPatch(), insert);
                        if (insert == 0) break;
                    }
                }

                if (insert == 0) {
                    slot.set(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                } else {
                    assert insert < Integer.MAX_VALUE;
                    stack1.setCount((int)insert);
                    slot.set(stack1);
                    return ItemStack.EMPTY; //fixme: inf loop if we return actual value, although it doesn't seem to be used beyond looping the call so this should be fine
                }
            }
        }
    }

    /**
     * Quick-moves a stack from the machine's inventory into the player's inventory
     * @param slot the slot that was shift-clicked
     */
    private void quickMoveIntoPlayerInventory(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return;
        ItemStack itemStack = ItemStackUtil.copy(slot);
        int extracted = itemStack.getCount();
        int size = this.slots.size() - 1;
        for (int i = size; i >= this.machineSlots.length; i--) {
            Slot slot1 = this.slots.get(i);
            assert !(slot1 instanceof StorageSlot);
            if (ItemStack.isSameItemSameComponents(itemStack, slot1.getItem())) {
                itemStack = slot1.safeInsert(itemStack);
                if (itemStack.isEmpty()) break;
            }
        }

        if (itemStack.isEmpty()) {
            long extract = slot.extract(extracted);
            assert extract == extracted;
            return;
        }

        for (int i = size; i >= this.machineSlots.length; i--) {
            Slot slot1 = this.slots.get(i);
            itemStack = slot1.safeInsert(itemStack);
            if (itemStack.isEmpty()) break;
        }

        long extract = slot.extract(extracted - itemStack.getCount());
        assert extract == extracted - itemStack.getCount();
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverse) {
        throw new UnsupportedOperationException("you shouldn't call this.");
    }

    /**
     * Creates player inventory slots for this screen in the default inventory formation.
     *
     * @param x The x position of the top left slot.
     * @param y The y position of the top left slot.
     */
    private void addPlayerInventorySlots(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.security.hasAccess(player) && stillValid(this.levelAccess, player, this.type.getBlock());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.data.synchronize();
    }

    @ApiStatus.Internal
    public void cycleFaceConfig(BlockFace face, boolean reverse, boolean reset) {
        MachineIOFace option = this.configuration.get(face);

        short bits = calculateIoBitmask();
        if (bits != 0b1_000_000_000_000 && !reset && !isFaceLocked(face)) {
            ResourceType type = option.getType();
            ResourceFlow flow = option.getFlow();
            int index = switch (type) {
                case NONE -> 12;
                case ENERGY, ITEM, FLUID, ANY -> (type.ordinal() - 1) * 3 + flow.ordinal();
            };
            int i = index + (reverse ? -1 : 1);
            while (i != index) {
                if (i == -1) {
                    i = 12;
                } else if (i == 13) {
                    i = 0;
                }

                if ((bits >>> i & 0b1) != 0) {
                    break;
                }
                if (reverse) {
                    i--;
                } else {
                    i++;
                }
            }

            if (i == 12) {
                option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
            } else {
                byte flowIndex = (byte) (i % 3);
                byte typeIndex = (byte) ((i - flowIndex) / 3 + 1);
                type = ResourceType.getFromOrdinal(typeIndex);
                flow = ResourceFlow.getFromOrdinal(flowIndex);

                option.setOption(type, flow);
            }
        } else {
            option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
        }

        this.machine.setChanged();
    }

    private short calculateIoBitmask() {
        // Format: NONE, any [any][out][in], fluid [any][out][in], item [any][out][in], energy [any][out][in]
        short bits = 0b0_000_000_000_000;

        for (FluidResourceSlot slot : this.fluidStorage) {
            InputType inputType = slot.inputType();
            if (inputType.externalInsertion()) bits |= 0b001;
            if (inputType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        for (ItemResourceSlot slot : this.itemStorage) {
            InputType inputType = slot.inputType();
            if (inputType.externalInsertion()) bits |= 0b001;
            if (inputType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        if (this.energyStorage.canExposedInsert()) bits |= 0b001;
        if (this.energyStorage.canExposedExtract()) bits |= 0b010;
        if ((bits & 0b011) == 0b011) bits |= 0b100;

        if (Integer.bitCount(bits & 0b000_001_001_001) > 1) {
            bits |= 0b001_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_010_010_010) > 1) {
            bits |= 0b010_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_100_100_100) > 1) {
            bits |= 0b100_000_000_000;
        }

        bits |= 0b1_000_000_000_000; //set NONE bit
        return bits;
    }

    /**
     * Receives and deserializes sync packets from the server (called on the client).
     *
     * @param buf The packet buffer.
     */
    @ApiStatus.Internal
    public void receiveState(@NotNull RegistryFriendlyByteBuf buf) {
        this.data.handle(buf);
    }

    /**
     * Adds a tank to the screen.
     *
     * @param tank The tank to add.
     */
    public void addTank(@NotNull Tank tank) {
        tank.setId(this.tanks.size());
        this.tanks.add(tank);
    }

    /**
     * A factory for creating machine menus.
     *
     * @param <Machine> The type of machine block entity.
     * @param <Menu> The type of machine menu.
     */
    @FunctionalInterface
    public interface MachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        /**
         * Creates a new menu.
         *
         * @param syncId the synchronization ID
         * @param inventory the player's inventory
         * @param buf the byte buffer containing data for the menu
         * @param type the type of the machine associated with the menu
         * @return the created menu
         */
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf buf, @NotNull MachineType<Machine, Menu> type);
    }


    /**
     * A factory for creating machine menus (without extra type information).
     *
     * @param <Machine> The type of machine block entity.
     * @param <Menu> The type of machine menu.
     */
    @FunctionalInterface
    public interface BasicMachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        /**
         * Creates a new menu.
         *
         * @param syncId the synchronization ID of the menu
         * @param inventory the player's inventory
         * @param buf the byte buffer containing data for the menu
         * @return the created menu
         */
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf buf);
    }
}
