package com.example.runepouch.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class RunePouchItem extends Item {
    public static final int SLOTS = 27;

    // 无参构造（默认耐久500，堆叠1）
    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    // 带参构造（支持自定义属性，如创造标签页）
    public RunePouchItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilityProvider() {
            private final ItemStackHandler handler = new ItemStackHandler(SLOTS) {
                @Override
                protected void onContentsChanged(int slot) {
                    stack.getOrCreateTag().putBoolean("Dirty", true);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    if (stack.getItem().getRegistryName() == null) return false;
                    String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                    return path.contains("rune");
                }
            };
            private final LazyOptional<ItemStackHandler> optional = LazyOptional.of(() -> handler);

            {
                if (nbt != null && nbt.contains("Inventory")) {
                    handler.deserializeNBT(nbt.getCompound("Inventory"));
                }
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .filter(h -> h instanceof ItemStackHandler)
                .ifPresent(h -> tag.put("Inventory", ((ItemStackHandler) h).serializeNBT()));
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("Inventory")) {
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .filter(h -> h instanceof ItemStackHandler)
                    .ifPresent(h -> ((ItemStackHandler) h).deserializeNBT(nbt.getCompound("Inventory")));
        }
        super.readShareTag(stack, nbt);
    }
}
