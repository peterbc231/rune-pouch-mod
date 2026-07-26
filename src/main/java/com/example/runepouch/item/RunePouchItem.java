package com.example.runepouch.item;

import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.entity.LivingEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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
        return new ICapabilityProvider() {
            private final RunePouchInventory inventory = new RunePouchInventory(stack);
            private final LazyOptional<ICurio> curioOptional = LazyOptional.of(() -> new ICurio() {
                // 限制只能放入 charm 槽位
                public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                    return slotContext.getIdentifier().equals("charm");
                }

                // 其他方法（不加 @Override）
                public void curioTick(String identifier, int index, LivingEntity livingEntity) {}
                public void onEquip(String identifier, int index, LivingEntity livingEntity) {}
                public void onUnequip(String identifier, int index, LivingEntity livingEntity) {}
                public boolean canRightClickEquip() { return true; }
                public DropRule getDropRule(LivingEntity livingEntity) { return DropRule.DEFAULT; }
                public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
                    return HashMultimap.create();
                }
            });

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return inventory.getCapability(cap, side);
                }
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
