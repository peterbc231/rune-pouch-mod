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
        this.pouchStack = inv.player.getItemInHand(hand);
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Rune Pouch capability missing!"));

        // 符文袋格子：9列 x 2行 = 18格
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 玩家背包 3行
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // 快捷栏 1行
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index < RunePouchItem.SLOTS) {
                if (!moveItemStackTo(stackInSlot, RunePouchItem.SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stackInSlot, 0, RunePouchItem.SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof RunePouchItem || offHand.getItem() instanceof RunePouchItem;
    }
}
