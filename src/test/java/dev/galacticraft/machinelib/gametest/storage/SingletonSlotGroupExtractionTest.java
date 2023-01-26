/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.gametest.storage;

import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.gametest.Util;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.galacticraft.machinelib.gametest.Assertions.*;
import static net.minecraft.world.item.Items.GOLD_INGOT;
import static net.minecraft.world.item.Items.IRON_INGOT;

public final class SingletonSlotGroupExtractionTest implements MachineLibGametest {
    private SlotGroup<Item, ItemStack, ? extends ResourceSlot<Item, ItemStack>> group;
    private ItemResourceSlot internalSlot;

    @Override
    public void beforeEach(@NotNull GameTestHelper context) {
        this.internalSlot = ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always());
        this.group = SlotGroup.<Item, ItemStack, ItemResourceSlot>create(TestModSlotGroupTypes.DIAMONDS)
                        .add(this.internalSlot)
                        .build();
    }

    @Override
    public void afterEach(@NotNull GameTestHelper context) {
        this.group = null;
    }

    @Override
    public void invokeTestMethod(@NotNull GameTestHelper context, @NotNull Method method) {
        if (method.getAnnotation(GameTest.class) == null && method.getReturnType() == Runnable.class) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == TransactionContext.class) {
                return;
            }
        }
        MachineLibGametest.super.invokeTestMethod(context, method);
    }

    @GameTestGenerator
    public @NotNull Collection<TestFunction> generate_tests() {
        List<TestFunction> tests = new ArrayList<>();
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(GameTest.class) == null && !method.getName().contains("lambda") && method.getReturnType() == Runnable.class) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0] == TransactionContext.class) {
                    tests.add(new TestFunction("slot_group", "slot_group." + method.getName(), EMPTY_STRUCTURE, Rotation.NONE, 0, 0, true, 1, 1, gameTestHelper -> {
                        this.beforeEach(gameTestHelper);
                        try {
                            ((Runnable) method.invoke(this, new Object[]{null})).run();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            if (e.getTargetException() instanceof GameTestAssertException) {
                                throw (GameTestAssertException) e.getTargetException();
                            }
                            throw new RuntimeException(e);
                        }
                        this.afterEach(gameTestHelper);
                        gameTestHelper.succeed();
                    }));

                    tests.add(new TestFunction("slot_group", "slot_group." + method.getName() + "_transactive", EMPTY_STRUCTURE, Rotation.NONE, 0, 0, true, 1, 1, gameTestHelper -> {
                        this.beforeEach(gameTestHelper);
                        try (Transaction transaction = Transaction.openOuter()) {
                            Runnable runnable = (Runnable) method.invoke(this, transaction);
                            ItemStack itemStack = this.internalSlot.copyStack();
                            runnable.run();
                            transaction.abort();
                            ItemStack itemStack1 = this.internalSlot.copyStack();
                            if (Util.stacksEqual(itemStack, itemStack1)) {
                                gameTestHelper.succeed();
                            } else {
                                gameTestHelper.fail("transaction test failed" + itemStack + " != " + itemStack1);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            if (e.getTargetException() instanceof GameTestAssertException) {
                                throw (GameTestAssertException) e.getTargetException();
                            }
                            throw new RuntimeException(e);
                        }
                        this.afterEach(gameTestHelper);
                        gameTestHelper.succeed();
                    }));
                }
            }
        }
        return tests;
    }

    @Contract(pure = true)
    private @NotNull Runnable extract$empty_fail(TransactionContext context) {
        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, null, 1, context));
            assertEquals(0, this.group.extract(GOLD_INGOT, 100, context));
            assertEquals(0, this.group.extract(GOLD_INGOT, 1, context));
        };
    }

    private @NotNull Runnable extract$one(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, null, 1);

        return () -> {
            assertEquals(1, this.group.extract(GOLD_INGOT, null, 1, context));
            assertTrue(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$one_nbt(TransactionContext context) {
        CompoundTag tag = Util.generateNbt();
        this.internalSlot.set(GOLD_INGOT, tag, 1);

        return () -> {
            assertEquals(1, this.group.extract(GOLD_INGOT, tag.copy(), 1, context));
            assertTrue(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$multiple(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, null, 64);

        return () -> {
            assertEquals(32, this.group.extract(GOLD_INGOT, null, 32, context));
            assertEquals(16, this.group.extract(GOLD_INGOT, null, 16, context));
            assertEquals(8, this.group.extract(GOLD_INGOT, null, 8, context));
            assertFalse(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$multiple_nbt(TransactionContext context) {
        CompoundTag tag = Util.generateNbt();
        this.internalSlot.set(GOLD_INGOT, tag.copy(), 64);

        return () -> {
            assertEquals(32, this.group.extract(GOLD_INGOT, tag.copy(), 32, context));
            assertEquals(16, this.group.extract(GOLD_INGOT, tag.copy(), 16, context));
            assertEquals(8, this.group.extract(GOLD_INGOT, tag.copy(), 8, context));
            assertFalse(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$multiple_exact(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, null, 8);

        return () -> {
            assertEquals(8, this.group.extract(GOLD_INGOT, null, 8, context));
            assertTrue(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$multiple_exact_nbt(TransactionContext context) {
        CompoundTag tag = Util.generateNbt();
        this.internalSlot.set(GOLD_INGOT, tag.copy(), 8);

        return () -> {
            assertEquals(8, this.group.extract(GOLD_INGOT, tag.copy(), 8, context));
            assertTrue(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$type_fail(TransactionContext context) {
        this.internalSlot.set(IRON_INGOT, null, 8);

        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, 1, context));
            assertFalse(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$type_fail_nbt(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, null, 8);

        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, Util.generateNbt(), 1, context));
            assertTrue(!this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$nbt_type_fail(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, Util.generateNbt(), 8);

        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, null, 1, context));
            assertFalse(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$different_nbt_type_fail(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, Util.generateNbt(), 8);

        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, Util.generateNbt(), 1, context));
            assertFalse(this.group.isEmpty());
        };
    }

    private @NotNull Runnable extract$overflow(TransactionContext context) {
        this.internalSlot.set(GOLD_INGOT, Util.generateNbt(), 8);

        return () -> {
            assertEquals(0, this.group.extract(GOLD_INGOT, Util.generateNbt(), 1, context));
            assertFalse(this.group.isEmpty());
        };
    }
}
