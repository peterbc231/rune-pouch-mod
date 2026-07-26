package com.example.runepouch.curio;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
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

            // 允许渲染
            public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
                return true;
            }

            // 渲染物品
            public void render(String identifier, int index, MatrixStack matrixStack,
                               IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                               float limbSwing, float limbSwingAmount, float partialTicks,
                               float ageInTicks, float netHeadYaw, float headPitch) {
                ICurio.RenderHelper.translateIfSneaking(matrixStack, livingEntity);
                ICurio.RenderHelper.rotateIfSneaking(matrixStack, livingEntity);
                matrixStack.scale(0.35F, 0.35F, 0.35F);
                matrixStack.translate(0.0F, 0.5F, -0.4F);
                matrixStack.rotate(Direction.DOWN.getRotation());
                Minecraft.getInstance().getItemRenderer().renderItem(stack,
                        ItemCameraTransforms.TransformType.NONE,
                        light, OverlayTexture.NO_OVERLAY,
                        matrixStack, renderTypeBuffer);
            }

            // 允许任何槽位（强制 true）
            public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                return true;
            }

            // 允许取下
            public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
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
