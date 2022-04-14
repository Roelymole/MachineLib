/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.impl.client.screen;

import dev.galacticraft.api.client.screen.Tank;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.impl.client.util.DrawableUtil;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.util.GenericStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Somewhat like a {@link net.minecraft.screen.slot.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 */
public class TankImpl implements Tank {
    public final ExposedStorage<Fluid, FluidVariant> storage;
    private final int index;
    public int id = -1;
    private final int x;
    private final int y;
    private final int height;

    public TankImpl(ExposedStorage<Fluid, FluidVariant> storage, int index, int x, int y, int height) {
        this.storage = storage;
        this.index = index;
        this.x = x;
        this.y = y;
        this.height = height;
    }

    @Override
    public FluidVariant getResource() {
        return this.storage.getResource(this.index);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void drawTooltip(@NotNull MatrixStack matrices, MinecraftClient client, int x, int y, int mouseX, int mouseY) {
        matrices.translate(0, 0, 1);
        if (DrawableUtil.isWithin(mouseX, mouseY, x + this.x, y + this.y, this.getWidth(), this.getHeight())) {
            List<Text> lines = new ArrayList<>(2);
            if (this.getResource().isBlank()) {
                client.currentScreen.renderTooltip(matrices, new TranslatableText("ui.galacticraft.machine.tank.fluid.empty").setStyle(Constant.Text.GRAY_STYLE), mouseX, mouseY);
                return;
            }
            MutableText amount;
            long amnt = this.getAmount();
            if (Screen.hasShiftDown() || amnt / 81.0 < 10000) {
                amount = new LiteralText(DrawableUtil.roundForDisplay(amnt / 81.0, 0) + "mB");
            } else {
                amount = new LiteralText(DrawableUtil.roundForDisplay(amnt / 81000.0, 2) + "B");
            }

            TranslatableText translatableText;
            translatableText = new TranslatableText("ui.galacticraft.machine.tank.fluid");

            lines.add(translatableText.setStyle(Constant.Text.GRAY_STYLE).append(FluidVariantAttributes.getName(this.getResource())).setStyle(Constant.Text.BLUE_STYLE));
            lines.add(new TranslatableText("ui.galacticraft.machine.tank.amount").setStyle(Constant.Text.GRAY_STYLE).append(amount.setStyle(Style.EMPTY.withColor(Formatting.WHITE))));
            client.currentScreen.renderTooltip(matrices, lines, mouseX, mouseY);
        }
        matrices.translate(0, 0, -1);
    }

    @Override
    public boolean acceptStack(@NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            if (storage.supportsExtraction() && this.storage.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    FluidVariant storedResource;
                    if (this.getResource().isBlank()) {
                        storedResource = StorageUtil.findStoredResource(storage, this.storage.getFilter(this.index), transaction);
                    } else {
                        storedResource = this.getResource();
                    }
                    if (storedResource != null) {
                        if (GenericStorageUtil.move(storedResource, storage, this.storage.getSlot(this.index), Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            } else if (storage.supportsInsertion() && this.storage.supportsExtraction()) {
                FluidVariant storedResource = this.getResource();
                if (!storedResource.isBlank()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (GenericStorageUtil.move(storedResource, this.storage.getSlot(this.index), storage, Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ExposedStorage<Fluid, FluidVariant> getStorage() {
        return this.storage;
    }

    @Override
    public long getAmount() {
        return this.storage.getAmount(this.index);
    }

    @Override
    public long getCapacity() {
        return this.storage.getCapacity(this.index);
    }
}
