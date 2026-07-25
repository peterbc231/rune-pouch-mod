package com.example.runepouch.item;

import net.minecraft.item.Item;
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

public class RunePouchItem extends Item {
    public static final int SLOTS = 27;

    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    public RunePouchItem(Properties properties) {
        super(properties);
    }

    // ========== 直接NBT存储（用于可靠持久化） ==========
    public ItemStackHandler getHandler(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                saveHandler(stack, this);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (stack.isEmpty()) return false;
                if (stack.getItem().getRegistryName() == null) return false;
                String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                return path.contains("rune");
            }
        };
        if (tag.contains("Inventory")) {
            handler.deserializeNBT(tag.getCompound("Inventory"));
        }
        return handler;
    }

    public void saveHandler(ItemStack stack, ItemStackHandler handler) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.put("Inventory", handler.serializeNBT());
    }

    // ========== Capability支持（供其他模组联动） ==========
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilityProvider() {
            private final ItemStackHandler capHandler = new ItemStackHandler(SLOTS) {
                @Override
                protected void onContentsChanged(int slot) {
                    // 当其他模组通过Capability修改时，自动同步到直接NBT
                    saveHandler(stack, this);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    if (stack.getItem().getRegistryName() == null) return false;
                    String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                    return path.contains("rune");
                }
            };
            private final LazyOptional<ItemStackHandler> optional = LazyOptional.of(() -> capHandler);

            // 初始化：如果传入的nbt有数据，则恢复
            {
                if (nbt != null && nbt.contains("Inventory")) {
                    capHandler.deserializeNBT(nbt.getCompound("Inventory"));
                }
                // 如果物品还没有Inventory，创建一个空的
                if (!stack.getOrCreateTag().contains("Inventory")) {
                    saveHandler(stack, capHandler);
                }
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    // ========== 序列化（丢弃时保存） ==========
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        // 先确保Capability数据已同步到NBT
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof ItemStackHandler) {
                        saveHandler(stack, (ItemStackHandler) h);
                    }
                });
        return stack.getOrCreateTag();
    }

    // ========== 反序列化（捡起时恢复） ==========
    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            // 将NBT数据复制到物品
            CompoundNBT tag = stack.getOrCreateTag();
            for (String key : nbt.keySet()) {
                tag.put(key, nbt.get(key));
            }
            // 通知Capability重新加载
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(h -> {
                        if (h instanceof ItemStackHandler && tag.contains("Inventory")) {
                            ((ItemStackHandler) h).deserializeNBT(tag.getCompound("Inventory"));
                        }
                    });
        }
        super.readShareTag(stack, nbt);
    }
}
