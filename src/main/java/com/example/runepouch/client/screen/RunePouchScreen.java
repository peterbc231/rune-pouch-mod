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
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(MatrixStack stack, float partial, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        getMinecraft().getTextureManager().bind(BG);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(stack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partial) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partial);
        renderTooltip(stack, mouseX, mouseY);
    }
}
