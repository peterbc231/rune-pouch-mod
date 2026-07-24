package com.example.runepouch.client.screen;

import com.example.runepouch.container.RunePouchContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RunePouchScreen extends ContainerScreen<RunePouchContainer> {
    private static final ResourceLocation BG = new ResourceLocation("runepouch", "textures/gui/rune_gui.png");

    public RunePouchScreen(RunePouchContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.xSize = 176;
        // 窗口高度：只显示前2行格子 + 背包区域（保持间隔）
        // 原版大箱子高度是222，但我们裁剪到 125（具体数值经测量，确保背包完整）
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BG);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        // 从纹理的 (0,0) 开始绘制，但只绘制高度为 ySize 的区域（即只显示顶部部分）
        this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
}
