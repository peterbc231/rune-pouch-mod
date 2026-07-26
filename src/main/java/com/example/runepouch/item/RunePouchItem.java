package com.example.runepouch.item;

import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
                // 所有方法都不加 @Override，避免编译冲突
                public void curioTick(String identifier, int index, LivingEntity livingEntity) {
                    // 可以在这里添加每个 tick 执行的功能（暂不需要）
                }

                public void playRightClickEquipSound(LivingEntity livingEntity) {
                    livingEntity.world.playSound(null, livingEntity.getPosX(), livingEntity.getPosY(),
                            livingEntity.getPosZ(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                            SoundCategory.NEUTRAL, 1.0f, 1.0f);
                }

                public void onEquip(String identifier, int index, LivingEntity livingEntity) {
                    // 装备时触发
                }

                public void onUnequip(String identifier, int index, LivingEntity livingEntity) {
                    // 卸下时触发
                }

                public DropRule getDropRule(LivingEntity livingEntity) {
                    return DropRule.DEFAULT;
                }

                public boolean canRightClickEquip() {
                    return true;
                }

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
