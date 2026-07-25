package com.example.runepouch.container;

import com.example.runepouch.init.ModContainers;
import com.example.runepouch.inventory.RunePouchInventory;
import com.example.runepouch.inventory.container.slot.DisabledSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RunePouchContainer extends Container {
    private final IItemHandler handler;
    private final ItemStack pouchStack;
    private final int currentItemIndex;

    public RunePouchContainer(int id, PlayerInventory inv, Hand hand) {
        super(ModContainers.RUNE_POUCH.get(), id);
        this.pouchStack = inv.player.getHeldItem(hand);
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing capability"));
        this.currentItemIndex = inv.selected;

        // 符文袋格子：3行 × 9列
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 玩家背包：3行
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 快捷栏：1行（禁用当前手持槽）
        for (int col = 0; col < 9; col++) {
            int slotIndex = col;
            int x = 8 + col * 18;
            int y = 142;
            if (col == currentItemIndex) {
                // 如果当前手持的是符文袋，禁用该槽
                addSlot(new DisabledSlot(inv, col, x, y));
            } else {
                addSlot(new Slot(inv, col, x, y));
            }
        }
    }

    // 拦截 SWAP 点击（数字键交换）
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.SWAP) {
            // 防止把符文袋从手中换到其他位置
            ItemStack stack = player.inventory.getItem(dragType);
            ItemStack currentItem = player.inventory.getSelected();
            if (!currentItem.isEmpty() && stack == currentItem) {
                return ItemStack.EMPTY;
            }
        }
        return super.clicked(slotId, dragType, clickType, player);
    }

    private void saveInventory() {
        pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof net.minecraftforge.items.ItemStackHandler) {
                        CompoundNBT tag = pouchStack.getOrCreateTag();
                        tag.put("Inventory", ((net.minecraftforge.items.ItemStackHandler) h).serializeNBT());
                    }
                });
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
            saveInventory();
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
            saveInventory();
        }
    }
}
