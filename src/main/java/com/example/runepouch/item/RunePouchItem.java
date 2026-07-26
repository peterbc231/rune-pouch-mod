package com.example.runepouch.item;

import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

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
        // 返回组合 Capability：既提供物品存储能力，也提供饰品能力
        return new ICapabilityProvider() {
            private final RunePouchInventory inventory = new RunePouchInventory(stack);
            private final LazyOptional<ICurio> curioOptional = LazyOptional.of(() -> new ICurio() {
                @Override
                public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                    // 允许放入任何槽位
                    return true;
                }

                @Override
                public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
                    return true;
                }

                @Override
                public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                    return true;
                }
            });

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                // 如果是物品存储能力，返回 inventory
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return inventory.getCapability(cap, side);
                }
                // 如果是饰品能力，返回 curioOptional
                if (cap == CuriosCapability.ITEM) {
                    return curioOptional.cast();
                }
                return LazyOptional.empty();
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
