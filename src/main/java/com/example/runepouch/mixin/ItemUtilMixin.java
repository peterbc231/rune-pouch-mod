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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

@Mixin(targets = "net.tslat.aoa3.util.ItemUtil")
public class ItemUtilMixin {

    private static Enchantment ARCHMAGE_ENCHANT = null;
    private static Enchantment GREED_ENCHANT = null;

    private static final Set<ResourceLocation> COMPATIBLE_HELMETS = new HashSet<>();

    static {
        String[] helmetIds = {
            "aoa3:achelos_helmet",
            "aoa3:oceanus_helmet",
            "aoa3:sealord_helmet",
            "aoa3:face_mask",
            "aoa3:night_vision_goggles",
            "aoa3:helm_of_the_dextrous",
            "aoa3:helm_of_the_dryad",
            "aoa3:helm_of_the_trawler",
            "aoa3:helm_of_the_treasurer",
            "aoa3:helm_of_the_warrior"
        };
        for (String id : helmetIds) {
            COMPATIBLE_HELMETS.add(new ResourceLocation(id));
        }
    }

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

    private static boolean hasNightmareArmor(ServerPlayerEntity player) {
        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack legs = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
        ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

        boolean isNightmareChest = !chest.isEmpty() && chest.getItem().getRegistryName() != null &&
                chest.getItem().getRegistryName().getPath().startsWith("nightmare_chestplate");
        boolean isNightmareLegs = !legs.isEmpty() && legs.getItem().getRegistryName() != null &&
                (legs.getItem().getRegistryName().getPath().startsWith("nightmare_legs") ||
                 legs.getItem().getRegistryName().getPath().startsWith("nightmare_leggings"));
        boolean isNightmareBoots = !boots.isEmpty() && boots.getItem().getRegistryName() != null &&
                boots.getItem().getRegistryName().getPath().startsWith("nightmare_boots");

        if (!isNightmareChest || !isNightmareLegs || !isNightmareBoots) {
            return false;
        }

        if (helmet.isEmpty() || helmet.getItem().getRegistryName() == null) {
            return false;
        }
        ResourceLocation helmetId = helmet.getItem().getRegistryName();
        boolean isNightmareHelmet = helmetId.getPath().startsWith("nightmare_helmet");
        boolean isCompatible = COMPATIBLE_HELMETS.contains(helmetId);

        return isNightmareHelmet || isCompatible;
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

        // ====== 符文袋法术附魔概率减免（新增） ======
        int pouchArchmageLevel = EnchantmentHelper.getEnchantmentLevel(getArchmage(), pouchStack);
        if (pouchArchmageLevel > 0) {
            int chance = pouchArchmageLevel * 5; // 5%, 10%, 15%
            if (ThreadLocalRandom.current().nextInt(100) < chance) {
                // 触发不消耗，直接返回 true
                cir.setReturnValue(true);
                return;
            }
        }

        // 2. 获取符文袋的 ItemStackHandler
        LazyOptional<IItemHandler> cap = pouchStack.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!cap.isPresent()) return;
        IItemHandler handler = cap.orElse(null);
        if (!(handler instanceof ItemStackHandler)) return;
        ItemStackHandler stackHandler = (ItemStackHandler) handler;

        // 3. 计算实际消耗量（法杖法术、贪婪、噩梦）
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

        // 4. 检查符文袋中是否有所需符文
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

        // 5. 从符文袋扣除
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

        // 7. 消耗耐久
        pouchStack.damageItem(1, player, (p) -> p.sendBreakAnimation(net.minecraft.util.Hand.MAIN_HAND));

        // 8. 拦截
        cir.setReturnValue(true);
    }
}
