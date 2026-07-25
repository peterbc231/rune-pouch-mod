package com.example.runepouch.item;

import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;

public class RunePouchItem extends Item implements ICurioItem {
    public RunePouchItem() {
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    public RunePouchItem(Properties properties) {
        super(properties.maxStackSize(1).maxDamage(500));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new RunePouchInventory(stack);
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        stack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
            stack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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

    // ========== Curios API 核心方法 ==========
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 明确允许放入任何槽位
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
