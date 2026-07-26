package com.example.runepouch.inventory;

import com.example.runepouch.item.RunePouchItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class RunePouchInventory implements ICapabilityProvider {
    public static final int SLOTS = 27;
    private final ItemStackHandler handler;
    private final LazyOptional<ItemStackHandler> optional;
    private final ItemStack pouchStack;

    public RunePouchInventory(ItemStack stack) {
        this.pouchStack = stack;
        this.handler = new ItemStackHandler(SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                save();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (stack.isEmpty()) return false;
                if (stack.getItem().getRegistryName() == null) return false;

                // 禁止放入符文袋自身（防止套娃）
                if (stack.getItem() instanceof RunePouchItem) {
                    return false;
                }

                String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                return path.contains("rune");
            }
        };
        this.optional = LazyOptional.of(() -> handler);
        load();
    }

    private void load() {
        CompoundNBT tag = pouchStack.getOrCreateTag();
        if (tag.contains("Inventory")) {
            handler.deserializeNBT(tag.getCompound("Inventory"));
        }
    }

    public void save() {
        CompoundNBT tag = pouchStack.getOrCreateTag();
        tag.put("Inventory", handler.serializeNBT());
    }

    public ItemStackHandler getHandler() {
        return handler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}
