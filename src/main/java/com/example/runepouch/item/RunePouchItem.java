package com.example.runepouch.item;

import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class RunePouchItem extends Item {
    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    public RunePouchItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new RunePouchInventory(stack);
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        // 确保数据已同步到 NBT
        stack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof com.example.runepouch.inventory.RunePouchInventory) {
                        ((com.example.runepouch.inventory.RunePouchInventory) h).save();
                    }
                });
        return stack.getOrCreateTag();
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            stack.setTag(nbt);
            // 触发重新加载
            stack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(h -> {
                        if (h instanceof com.example.runepouch.inventory.RunePouchInventory) {
                            ((com.example.runepouch.inventory.RunePouchInventory) h).save(); // 保存加载的数据
                        }
                    });
        }
        super.readShareTag(stack, nbt);
    }
}
