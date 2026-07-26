package com.example.runepouch.mixin;

import com.example.runepouch.inventory.RunePouchInventory;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "net.tslat.aoa3.util.ItemUtil")
public class ItemUtilMixin {

    @Inject(method = "findAndConsumeRunes", at = @At("HEAD"), cancellable = true)
    private static void onFindAndConsumeRunes(HashMap<Item, Integer> runeMap, ServerPlayerEntity player,
                                               boolean allowBuffs, @Nonnull ItemStack heldItem,
                                               CallbackInfoReturnable<Boolean> cir) {
        // 1. 获取护符栏中的符文袋
        ItemStack pouchStack = ItemStack.EMPTY;
        LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (!curiosOpt.isPresent()) return;
        ICuriosItemHandler curios = curiosOpt.orElse(null);
        if (curios == null) return;

        LazyOptional<IItemHandler> charmHandlerOpt = curios.getStacksHandler("charm");
        if (!charmHandlerOpt.isPresent()) return;
        IItemHandler charmHandler = charmHandlerOpt.orElse(null);
        if (charmHandler == null) return;

        for (int i = 0; i < charmHandler.getSlots(); i++) {
            ItemStack stack = charmHandler.getStackInSlot(i);
            if (stack.getItem() instanceof RunePouchItem) {
                pouchStack = stack;
                break;
            }
        }
        if (pouchStack.isEmpty()) return;

        // 2. 获取符文袋的 ItemStackHandler
        LazyOptional<IItemHandler> cap = pouchStack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!cap.isPresent()) return;
        IItemHandler handler = cap.orElse(null);
        if (handler == null) return;

        // 3. 检查符文袋中是否有所需符文
        for (Map.Entry<Item, Integer> entry : runeMap.entrySet()) {
            int found = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    found += stack.getCount();
                }
            }
            if (found < entry.getValue()) {
                return; // 符文不足，走原逻辑
            }
        }

        // 4. 从符文袋扣除
        for (Map.Entry<Item, Integer> entry : runeMap.entrySet()) {
            int remaining = entry.getValue();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                    if (remaining == 0) break;
                }
            }
        }

        // 5. 保存符文袋数据
        pouchStack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(h -> {
                    if (h instanceof RunePouchInventory) {
                        ((RunePouchInventory) h).save();
                    }
                });

        // 6. 拦截原方法，返回 true
        cir.setReturnValue(true);
    }
}
