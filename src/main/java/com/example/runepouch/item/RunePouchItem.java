package com.example.runepouch.item;

import com.example.runepouch.curio.CurioProvider;
import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RunePouchItem extends Item {
    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    public RunePouchItem(Properties properties) {
        super(properties.maxStackSize(1).maxDamage(500));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        // 返回一个组合的 Capability 提供者
        return new ICapabilityProvider() {
            private final RunePouchInventory inventory = new RunePouchInventory(stack);
            private final CurioProvider curio = new CurioProvider(stack);

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
                // 优先检查是否是物品处理器能力
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return inventory.getCapability(cap, side);
                }
                // 否则检查是否是饰品能力
                return curio.getCapability(cap, side);
            }
        };
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof RunePouchInventory) {
                        ((RunePouchInventory) h).save();
                    }
                });
        return stack.getOrCreateTag();
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            stack.setTag(nbt);
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(h -> {
                        if (h instanceof RunePouchInventory) {
                            ((RunePouchInventory) h).save();
                        }
                    });
        }
        super.readShareTag(stack, nbt);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }
}
