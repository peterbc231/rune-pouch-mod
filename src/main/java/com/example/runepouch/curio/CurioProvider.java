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
            @Override
            public boolean canEquipFromUse(SlotContext ctx) {
                return true;
            }

            @Override
            public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
                return true;
            }

            @Override
            public void render(String identifier, int index, MatrixStack matrixStack,
                               IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                               float limbSwing, float limbSwingAmount, float partialTicks,
                               float ageInTicks, float netHeadYaw, float headPitch) {
                // 使用不死图腾的渲染逻辑（完全相同的调用）
                ICurio.RenderHelper.translateIfSneaking(matrixStack, livingEntity);
                ICurio.RenderHelper.rotateIfSneaking(matrixStack, livingEntity);
                matrixStack.scale(0.35F, 0.35F, 0.35F);
                matrixStack.translate(0.0F, 0.5F, -0.4F);
                matrixStack.rotate(Direction.DOWN.getRotation());
                // 1.16.5 的 ItemRenderer.renderItem 正确签名
                Minecraft.getInstance().getItemRenderer().renderItem(stack,
                        ItemCameraTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY,
                        matrixStack, renderTypeBuffer);
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
