package com.example.runepouch.event;

import com.example.runepouch.container.RunePouchContainer;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemEventHandler {

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItem() instanceof RunePouchItem) {
            event.setCanceled(true);
            if (!world.isRemote) {
                // 不再消耗耐久（移除 damageItem 调用）
                NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                        (id, inv, p) -> new RunePouchContainer(id, inv, hand),
                        new StringTextComponent("Rune Pouch")
                ), buf -> buf.writeEnumValue(hand));
            }
        }
    }
}
