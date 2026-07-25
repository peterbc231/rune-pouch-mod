package com.example.runepouch.curio;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioProvider implements ICapabilityProvider {
    private final LazyOptional<ICurio> curioOptional;

    public CurioProvider(ItemStack stack) {
        this.curioOptional = LazyOptional.of(() -> new ICurio() {
            // 不加 @Override，直接实现接口方法
            public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                // 只允许放入 "back" 槽位
                return slotContext.getIdentifier().equals("back");
            }

            public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
                return true;
            }

            public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                return true;
            }
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CuriosCapability.ITEM) {
            return curioOptional.cast();
        }
        return LazyOptional.empty();
    }
}
