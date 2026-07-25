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

    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

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
                    // 标记物品数据已更改，触发保存
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

            // 如果传入的 nbt 有数据，立即恢复
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

    // ========== 关键：正确处理掉落/捡起 ==========
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        // 获取当前物品的 NBT
        CompoundNBT tag = stack.getOrCreateTag();
        // 从 Capability 获取 handler，序列化 Inventory 数据
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .filter(h -> h instanceof ItemStackHandler)
                .ifPresent(h -> {
                    CompoundNBT invTag = ((ItemStackHandler) h).serializeNBT();
                    tag.put("Inventory", invTag);
                });
        // 返回完整的 NBT（包含 Inventory 和可能其他数据）
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            // 先恢复 Capability 数据
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .filter(h -> h instanceof ItemStackHandler)
                    .ifPresent(h -> {
                        if (nbt.contains("Inventory")) {
                            ((ItemStackHandler) h).deserializeNBT(nbt.getCompound("Inventory"));
                        }
                    });
            // 将剩余的 NBT 设置到物品上（比如耐久、Dirty 标记等）
            // 注意：不要直接 setTag，而是合并
            CompoundNBT currentTag = stack.getOrCreateTag();
            for (String key : nbt.getAllKeys()) {
                if (!key.equals("Inventory")) {
                    currentTag.put(key, nbt.get(key));
                }
            }
        }
        // 调用父类方法，让超级逻辑处理（它也会调用 setTag，但我们已经合并了）
        super.readShareTag(stack, nbt);
    }
}
