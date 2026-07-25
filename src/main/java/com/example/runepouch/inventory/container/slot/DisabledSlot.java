package com.example.runepouch.inventory.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class DisabledSlot extends Slot {
    public DisabledSlot(net.minecraft.inventory.IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
