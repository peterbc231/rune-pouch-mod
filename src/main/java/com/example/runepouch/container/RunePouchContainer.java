package com.example.runepouch.container;

import com.example.runepouch.init.ModContainers;
import com.example.runepouch.inventory.RunePouchInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
    private final RunePouchInventory inventory;
    private final ItemStack pouchStack;

    public RunePouchContainer(int id, PlayerInventory inv, Hand hand) {
        super(ModContainers.RUNE_POUCH.get(), id);
        this.pouchStack = inv.player.getHeldItem(hand);
        // 直接从 Capability 获取 IItemHandler
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing capability"));
        // 通过 handler 获取其所属的 Inventory（需要在 RunePouchInventory 中添加 getter）
        // 但由于我们无法从 IItemHandler 直接获取 Inventory，我们改为在保存时重新获取
        this.inventory = null; // 不再直接持有 Inventory，改用静态方法保存

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

        // 快捷栏：1行
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    // 保存数据的辅助方法
    private void saveInventory() {
        // 每次保存时重新获取 Inventory 并保存
        pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof net.minecraftforge.items.ItemStackHandler) {
                        // 由于我们无法直接获取 RunePouchInventory，我们直接通过 NBT 保存
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
            // 保存数据
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
