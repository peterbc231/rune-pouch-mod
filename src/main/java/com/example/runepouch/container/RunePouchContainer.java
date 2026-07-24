package com.example.runepouch.container;

import com.example.runepouch.init.ModContainers;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RunePouchContainer extends Container {
    private final IItemHandler handler;
    private final ItemStack pouchStack;

    public RunePouchContainer(int id, PlayerInventory inv, Hand hand) {
        super(ModContainers.RUNE_POUCH.get(), id);
        this.pouchStack = inv.player.getHeldItem(hand);
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Rune Pouch capability missing!"));

        // === 符文袋格子：2行 × 9列（索引 0~17）===
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                // 原版格子起始坐标：8, 18，每格间隔18
                addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // === 玩家背包：3行 × 9列（索引 18~44）===
        // 原版背包起始Y是84，但因为我们只显示2行袋子，把背包往上提8像素，紧贴袋子底部
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = 18 + row * 9 + col;
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 58 + row * 18));
            }
        }

        // === 快捷栏：1行 × 9列（索引 45~53）===
        for (int col = 0; col < 9; col++) {
            int index = 45 + col;
            addSlot(new Slot(inv, col, 8 + col * 18, 116));
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            ItemStack copy = stackInSlot.copy();
            if (index < RunePouchItem.SLOTS) {
                if (!this.mergeItemStack(stackInSlot, RunePouchItem.SLOTS, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(stackInSlot, 0, RunePouchItem.SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();
        return main.getItem() instanceof RunePouchItem || off.getItem() instanceof RunePouchItem;
    }
}
