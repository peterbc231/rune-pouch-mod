package com.example.runepouch.client.screen;

import com.example.runepouch.container.RunePouchContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RunePouchScreen extends ContainerScreen<RunePouchContainer> {
    private static final ResourceLocation BG = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public RunePouchScreen(RunePouchContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.xSize = 176;
        this.ySize = 150;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BG);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    // 1.16.5 正确的覆盖方法名
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        // 绘制标题：左上角
        this.font.drawString(matrixStack, this.title.getString(), 8.0F, 6.0F, 4210752);
        // 绘制"物品栏"：X=8, Y=72
        this.font.drawString(matrixStack, this.playerInventory.getDisplayName().getString(), 8.0F, 72.0F, 4210752);
    }
}
