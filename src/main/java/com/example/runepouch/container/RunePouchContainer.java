package com.example.runepouch.container;

import com.example.runepouch.init.ModContainers;
import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RunePouchContainer extends Container {
    private final RunePouchInventory inventory;
    private final ItemStack pouchStack;

    public RunePouchContainer(int id, PlayerInventory inv, Hand hand) {
        super(ModContainers.RUNE_POUCH.get(), id);
        this.pouchStack = inv.player.getHeldItem(hand);
        this.inventory = (RunePouchInventory) pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing RunePouchInventory"));

        // 符文袋格子：3行 × 9列
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                addSlot(new SlotItemHandler(inventory.getHandler(), index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 玩家背包：3行
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 快捷栏：1行
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            ItemStack copy = stackInSlot.copy();
            if (index < RunePouchInventory.SLOTS) {
                if (!this.mergeItemStack(stackInSlot, RunePouchInventory.SLOTS, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(stackInSlot, 0, RunePouchInventory.SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            inventory.save(); // 每次转移后保存
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();
        return main.getItem() instanceof com.example.runepouch.item.RunePouchItem ||
               off.getItem() instanceof com.example.runepouch.item.RunePouchItem;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            inventory.save();
        }
    }
}
