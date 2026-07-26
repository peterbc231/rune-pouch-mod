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
            // 允许右键直接装备
            public boolean canEquipFromUse(SlotContext ctx) {
                return true;
            }

            // 允许放入任何槽位
            public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                return true;
            }

            // 允许取下
            public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
                return true;
            }

            // 其他方法（如 render、canRender）不重写，使用默认空实现
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
