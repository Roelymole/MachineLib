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

import dev.galacticraft.machinelib.api.block.entity.BaseBlockEntity;
import dev.galacticraft.machinelib.impl.util.Utils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SynchronizedMenuType<BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> extends ExtendedScreenHandlerType<Menu, RegistryFriendlyByteBuf> {
    private final Factory<BE, Menu> factory;

    protected SynchronizedMenuType(Factory<BE, Menu> factory) {
        super((syncId, inventory, data) -> null, Utils.BUF_IDENTITY_CODEC);
        this.factory = factory;
    }

    @Contract("_ -> new")
    public static <BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> @NotNull MenuType<Menu> create(Factory<BE, Menu> factory) {
        return new SynchronizedMenuType<>(factory);
    }

    @Contract("_ -> new")
    public static <BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> @NotNull MenuType<Menu> createSimple(InventoryFactory<BE, Menu> factory) {
        return create(factory, 8, 84);
    }

    @Contract("_, _ -> new")
    public static <BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> @NotNull MenuType<Menu> create(InventoryFactory<BE, Menu> factory, int invY) {
        return create(factory, 8, invY);
    }

    @Contract("_, _, _ -> new")
    public static <BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> @NotNull MenuType<Menu> create(InventoryFactory<BE, Menu> factory, int invX, int invY) {
        return new SynchronizedMenuType<>((type, syncId, inventory, buf) -> factory.create(type, syncId, inventory, buf, invX, invY));
    }

    @Contract(value = "_ -> new", pure = true)
    public static <BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> @NotNull MenuType<Menu> create(ExtendedScreenHandlerType.ExtendedFactory<Menu, RegistryFriendlyByteBuf> factory) {
        return new ExtendedScreenHandlerType<>(factory, Utils.BUF_IDENTITY_CODEC);
    }

    @Override
    public Menu create(int syncId, Inventory inventory, RegistryFriendlyByteBuf data) {
        Menu menu = this.factory.create(this, syncId, inventory, data);
        menu.registerData(menu.getData());
        menu.getData().handleInitial(data);
        return menu;
    }

    /**
     * A factory for creating machine menus (without extra type information).
     *
     * @param <BE> The type of machine block entity.
     * @param <Menu> The type of machine menu.
     */
    @FunctionalInterface
    public interface Factory<BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> {
        /**
         * Creates a new menu.
         *
         * @param syncId the synchronization ID of the menu
         * @param inventory the player's inventory
         * @param buf the byte buffer containing data for the menu
         * @return the created menu
         */
        Menu create(MenuType<Menu> type, int syncId, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf buf);
    }

    @FunctionalInterface
    public interface InventoryFactory<BE extends BaseBlockEntity, Menu extends SynchronizedMenu<BE>> {
        Menu create(MenuType<Menu> type, int syncId, @NotNull Inventory inventory, @NotNull RegistryFriendlyByteBuf buf, int invX, int invY);
    }
}
