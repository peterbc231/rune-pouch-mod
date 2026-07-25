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
        super(new Properties()
                .maxStackSize(1)      // 只能堆叠1个
                .durability(500)      // 500耐久
                .setNoRepair()        // 不能修复（可选）
        );
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

    // 强制显示耐久条
    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    // ========== Curios API 集成 ==========
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 只允许放入 back 槽
        return slotContext.getIdentifier().equals("back");
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
