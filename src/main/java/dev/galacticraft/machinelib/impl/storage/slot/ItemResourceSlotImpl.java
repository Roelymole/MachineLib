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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemResourceSlotImpl extends ResourceSlotImpl<Item> implements ItemResourceSlot {
    private static final String RECIPES_KEY = "Recipes";
    private final @Nullable ItemSlotDisplay display;
    private long cachedExpiry = -1;

    private @Nullable SingleSlotStorage<ItemVariant> cachedStorage = null;
    private @Nullable ItemApiLookup<?, ContainerItemContext> cachedLookup = null;
    private @Nullable Object cachedApi = null;
    private @Nullable Set<ResourceLocation> recipes;

    public ItemResourceSlotImpl(@NotNull InputType inputType, @Nullable ItemSlotDisplay display, @NotNull ResourceFilter<Item> externalFilter, int capacity) {
        super(inputType, externalFilter, capacity);
        assert capacity > 0 && capacity <= 64;
        this.display = display;
    }

    @Override
    public long getRealCapacity() {
        assert this.isSane();
        return Math.min(this.capacity, this.resource == null ? 64 : this.resource.getDefaultMaxStackSize());
    }

    @Override
    public long getCapacityFor(@NotNull Item item, @NotNull DataComponentPatch components) {
        Optional<? extends Integer> optional = components.get(DataComponents.MAX_STACK_SIZE);
        if (optional != null && optional.isPresent()) {
           return optional.get();
        }
        return Math.min(this.capacity, item.getDefaultMaxStackSize());
    }

    @Override
    public @Nullable Item consumeOne() {
        DataComponentPatch tag = this.components;
        Item resource = this.extractOne();
        if (resource == null) return null;
        if (resource.hasCraftingRemainingItem()) {
            this.insertRemainder(resource, tag, 1);
        }
        return resource;
    }

    @Override
    public boolean consumeOne(@NotNull Item resource, @Nullable DataComponentPatch components) {
        DataComponentPatch actual = this.components;
        if (this.extractOne(resource, components)) {
            this.insertRemainder(resource, actual, 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long consume(long amount) {
        Item item = this.resource;
        if (item == null) return 0;
        DataComponentPatch components = this.components;
        long consumed = this.extract(amount);
        if (consumed > 0) {
            this.insertRemainder(item, components, 1);
            return consumed;
        }
        return consumed;
    }

    @Override
    public long consume(@NotNull Item resource, @Nullable DataComponentPatch components, long amount) {
        DataComponentPatch actual = this.components;
        long consumed = this.extract(resource, components, amount);
        if (consumed > 0) {
            this.insertRemainder(resource, actual, (int) consumed);
        }
        return consumed;
    }

    private void insertRemainder(@NotNull Item resource, @NotNull DataComponentPatch tag, int extracted) {
        if (resource.hasCraftingRemainingItem()) {
            if (this.isEmpty()) {
                ItemStack remainder = resource.getRecipeRemainder(ItemStackUtil.of(resource, tag, extracted));
                if (!remainder.isEmpty()) {
                    this.insert(remainder.getItem(), remainder.getComponentsPatch(), remainder.getCount());
                }
            }
        }
    }

    @Override
    public @Nullable ItemSlotDisplay getDisplay() {
        return this.display;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        if (this.isEmpty()) return tag;
        tag.putString(RESOURCE_KEY, BuiltInRegistries.ITEM.getKey(this.resource).toString());
        tag.putInt(AMOUNT_KEY, (int) this.amount);
        if (!this.components.isEmpty()) tag.put(COMPONENTS_KEY, DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, this.components).getOrThrow());
        if (this.recipes != null) {
            ListTag recipeTag = new ListTag();
            for (ResourceLocation entry : this.recipes) {
                recipeTag.add(StringTag.valueOf(entry.toString()));
            }
            tag.put(RECIPES_KEY, recipeTag);
        }
        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
        } else {
            this.set(BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString(RESOURCE_KEY))), tag.contains(COMPONENTS_KEY) ? DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, tag.get(COMPONENTS_KEY)).getOrThrow() : DataComponentPatch.EMPTY, tag.getInt(AMOUNT_KEY));
            if (this.inputType() == InputType.RECIPE_OUTPUT && tag.contains(RECIPES_KEY, Tag.TAG_COMPOUND)) {
                ListTag list = tag.getList(RECIPES_KEY, Tag.TAG_STRING);
                if (!list.isEmpty()) {
                    if (this.recipes == null) {
                        this.recipes = new HashSet<>();
                    } else {
                        this.recipes.clear();
                    }
                    for (int i = 0; i < list.size(); i++) {
                        this.recipes.add(new ResourceLocation(list.getString(i)));
                    }
                }
            }
        }
    }

    @Override
    public void writePacket(@NotNull RegistryFriendlyByteBuf buf) {
        if (this.amount > 0) {
            buf.writeInt((int) this.amount);
            buf.writeUtf(BuiltInRegistries.ITEM.getKey(this.resource).toString());
            DataComponentPatch.STREAM_CODEC.encode(buf, this.components);
        } else {
            buf.writeInt(0);
        }
    }

    @Override
    public void readPacket(@NotNull RegistryFriendlyByteBuf buf) {
        int amount = buf.readInt();
        if (amount == 0) {
            this.setEmpty();
        } else {
            Item resource = BuiltInRegistries.ITEM.get(new ResourceLocation(buf.readUtf()));
            DataComponentPatch tag = DataComponentPatch.STREAM_CODEC.decode(buf);
            this.set(resource, tag, amount);
        }
    }

    @Override
    public <A> @Nullable A find(ItemApiLookup<A, ContainerItemContext> lookup) {
        if (this.cachedExpiry != this.getModifications() || this.cachedLookup != lookup) {
            this.cachedExpiry = this.getModifications();
            this.cachedApi = ItemResourceSlot.super.find(lookup);
            this.cachedLookup = lookup;
        }
        return (A) this.cachedApi;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        if (this.cachedStorage == null) {
            this.cachedStorage = new SingleSlotStorage<>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.insert(resource.getItem(), resource.getComponents(), maxAmount, transaction);
                }

                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.extract(resource.getItem(), resource.getComponents(), maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return ItemResourceSlotImpl.this.isEmpty();
                }

                @Override
                public ItemVariant getResource() {
                    return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.resource), ItemResourceSlotImpl.this.components);
                }

                @Override
                public long getAmount() {
                    return ItemResourceSlotImpl.this.getAmount();
                }

                @Override
                public long getCapacity() {
                    return ItemResourceSlotImpl.this.getRealCapacity();
                }

                @Override
                public long getVersion() {
                    return ItemResourceSlotImpl.this.getModifications();
                }
            };
        }
        return this.cachedStorage;
    }

    @Override
    public ItemVariant getItemVariant() {
        return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.resource), ItemResourceSlotImpl.this.components);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.extract(resource.getItem(), resource.getComponents(), maxAmount, transaction);
    }

    @Override
    public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

        if (newVariant.getItem() == this.resource && this.components.equals(newVariant.getComponents())) {
            return Math.min(this.amount, maxAmount);
        }

        if (this.amount == maxAmount && this.getCapacityFor(newVariant.getItem(), newVariant.getComponents()) >= maxAmount) {
            this.updateSnapshots(transaction);
            this.set(newVariant.getItem(), newVariant.getComponents(), maxAmount);
            return maxAmount;
        }

        return 0;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.insert(resource.getItem(), resource.getComponents(), maxAmount, transaction);
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSane() {
        return super.isSane() && this.resource != Items.AIR;
    }

    @Override
    public @Nullable Set<ResourceLocation> takeRecipes() {
        Set<ResourceLocation> recipes1 = this.recipes;
        this.recipes = null;
        return recipes1;
    }

    @Override
    public void recipeCrafted(@NotNull ResourceLocation id) {
        if (this.recipes == null) {
            if (this.inputType() == InputType.RECIPE_OUTPUT) {
                this.recipes = new HashSet<>();
            } else {
                return;
            }
        }
        this.recipes.add(id);
    }
}
