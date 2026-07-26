package com.example.runepouch.mixin;

import com.example.runepouch.inventory.RunePouchInventory;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
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

    private static Enchantment ARCHMAGE_ENCHANT = null;
    private static Enchantment GREED_ENCHANT = null;

    private static Enchantment getArchmage() {
        if (ARCHMAGE_ENCHANT == null) {
            ARCHMAGE_ENCHANT = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("aoa3", "archmage"));
        }
        return ARCHMAGE_ENCHANT;
    }

    private static Enchantment getGreed() {
        if (GREED_ENCHANT == null) {
            GREED_ENCHANT = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("aoa3", "greed"));
        }
        return GREED_ENCHANT;
    }

    // 通过物品注册名检测全套噩梦盔甲（不依赖AoA3内部API）
    private static boolean hasNightmareArmor(ServerPlayerEntity player) {
        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack legs = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
        ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        boolean hasHelmet = !helmet.isEmpty() && helmet.getItem().getRegistryName() != null && helmet.getItem().getRegistryName().getPath().startsWith("nightmare_helmet");
        boolean hasChest = !chest.isEmpty() && chest.getItem().getRegistryName() != null && chest.getItem().getRegistryName().getPath().startsWith("nightmare_chestplate");
        boolean hasLegs = !legs.isEmpty() && legs.getItem().getRegistryName() != null && legs.getItem().getRegistryName().getPath().startsWith("nightmare_leggings");
        boolean hasBoots = !boots.isEmpty() && boots.getItem().getRegistryName() != null && boots.getItem().getRegistryName().getPath().startsWith("nightmare_boots");
        return hasHelmet && hasChest && hasLegs && hasBoots;
    }

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

        Optional<ICurioStacksHandler> charmHandlerOpt = curios.getStacksHandler("charm");
        if (!charmHandlerOpt.isPresent()) return;
        ICurioStacksHandler charmHandler = charmHandlerOpt.get();

        IItemHandler charmInventory = charmHandler.getStacks();
        for (int i = 0; i < charmInventory.getSlots(); i++) {
            ItemStack stack = charmInventory.getStackInSlot(i);
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
        if (!(handler instanceof ItemStackHandler)) return;
        ItemStackHandler stackHandler = (ItemStackHandler) handler;

        // 3. 计算实际消耗量
        int archmage = allowBuffs ? EnchantmentHelper.getEnchantmentLevel(getArchmage(), heldItem) : 0;
        boolean greed = allowBuffs && EnchantmentHelper.getEnchantmentLevel(getGreed(), heldItem) > 0;
        boolean nightmare = allowBuffs && hasNightmareArmor(player);

        HashMap<Item, Integer> actualNeeded = new HashMap<>();
        for (Map.Entry<Item, Integer> entry : runeMap.entrySet()) {
            int amount = entry.getValue();
            if (greed) amount += 2;
            if (archmage > 0) amount -= archmage;
            if (nightmare) amount -= 1;
            if (amount <= 0) amount = 1;
            actualNeeded.put(entry.getKey(), amount);
        }

        // 4. 检查符文袋中是否有所需符文（按实际消耗量）
        for (Map.Entry<Item, Integer> entry : actualNeeded.entrySet()) {
            int found = 0;
            for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
                ItemStack stack = stackHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    found += stack.getCount();
                }
            }
            if (found < entry.getValue()) {
                return;
            }
        }

        // 5. 从符文袋扣除实际消耗量
        for (Map.Entry<Item, Integer> entry : actualNeeded.entrySet()) {
            int remaining = entry.getValue();
            for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
                ItemStack stack = stackHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == entry.getKey()) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                    if (remaining == 0) break;
                }
            }
        }

        // 6. 强制保存
        CompoundNBT tag = pouchStack.getOrCreateTag();
        tag.put("Inventory", stackHandler.serializeNBT());

        // 7. 消耗符文袋耐久（每次施法扣1点）
        pouchStack.damageItem(1, player, (p) -> p.sendBreakAnimation(net.minecraft.util.Hand.MAIN_HAND));

        // 8. 拦截原方法，返回 true（表示消耗成功）
        cir.setReturnValue(true);
    }
}
