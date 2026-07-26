package com.example.runepouch.mixin;

import com.example.runepouch.inventory.RunePouchInventory;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(targets = "net.tslat.aoa3.util.ItemUtil")
public class ItemUtilMixin {

    @Inject(method = "findAndConsumeRunes", at = @At("HEAD"), cancellable = true)
    private static void onFindAndConsumeRunes(HashMap<Item, Integer> runeMap, ServerPlayerEntity player,
                                               boolean allowBuffs, @Nonnull ItemStack heldItem,
                                               CallbackInfoReturnable<Boolean> cir) {
        System.out.println("[RunePouch] findAndConsumeRunes called!");

        // 1. 获取护符栏中的符文袋
        ItemStack pouchStack = ItemStack.EMPTY;
        LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (!curiosOpt.isPresent()) {
            System.out.println("[RunePouch] No Curios handler found");
            return;
        }
        ICuriosItemHandler curios = curiosOpt.orElse(null);
        if (curios == null) return;

        Optional<ICurioStacksHandler> charmHandlerOpt = curios.getStacksHandler("charm");
        if (!charmHandlerOpt.isPresent()) {
            System.out.println("[RunePouch] No charm slot found");
            return;
        }
        ICurioStacksHandler charmHandler = charmHandlerOpt.get();

        IItemHandler charmInventory = charmHandler.getStacks();
        for (int i = 0; i < charmInventory.getSlots(); i++) {
            ItemStack stack = charmInventory.getStackInSlot(i);
            if (stack.getItem() instanceof RunePouchItem) {
                pouchStack = stack;
                System.out.println("[RunePouch] Found pouch in charm slot!");
                break;
            }
        }
        if (pouchStack.isEmpty()) {
            System.out.println("[RunePouch] No pouch found in charm slot");
            return;
        }

        // 2. 获取符文袋的 ItemStackHandler
        LazyOptional<IItemHandler> cap = pouchStack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!cap.isPresent()) return;
        IItemHandler handler = cap.orElse(null);
        if (handler == null) return;

        // 如果是 ItemStackHandler，直接操作
        if (!(handler instanceof ItemStackHandler)) {
            System.out.println("[RunePouch] Handler is not ItemStackHandler, falling back to AoA3");
            return;
        }
        ItemStackHandler stackHandler = (ItemStackHandler) handler;

        // 3. 检查符文袋中是否有所需符文（打印当前数量）
        for (Map.Entry<Item, Integer> entry : runeMap.entrySet()) {
            int found = 0;
            for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
                ItemStack stack = stackHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    found += stack.getCount();
                }
            }
            System.out.println("[RunePouch] Need " + entry.getValue() + " of " + entry.getKey().getRegistryName() + ", found " + found);
            if (found < entry.getValue()) {
                System.out.println("[RunePouch] Not enough runes in pouch, falling back to AoA3");
                return;
            }
        }

        // 4. 从符文袋扣除
        for (Map.Entry<Item, Integer> entry : runeMap.entrySet()) {
            int remaining = entry.getValue();
            for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
                ItemStack stack = stackHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    System.out.println("[RunePouch] Removed " + take + " of " + entry.getKey().getRegistryName() + " from slot " + slot);
                    remaining -= take;
                    if (remaining == 0) break;
                }
            }
        }

        // 5. 强制保存到 NBT（直接用 NBT 操作，确保持久化）
        CompoundNBT tag = pouchStack.getOrCreateTag();
        tag.put("Inventory", stackHandler.serializeNBT());
        System.out.println("[RunePouch] Saved to NBT, Inventory size: " + stackHandler.getSlots());

        // 6. 消耗符文袋耐久（每次施法扣1点）
        pouchStack.damageItem(1, player, (p) -> p.sendBreakAnimation(net.minecraft.util.Hand.MAIN_HAND));

        System.out.println("[RunePouch] Successfully consumed runes from pouch!");
        // 7. 拦截原方法，返回 true
        cir.setReturnValue(true);
    }
}
