package com.example.runepouch.event;

import com.example.runepouch.item.RunePouchItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class RunePouchEnchantmentHandler {

    private static Enchantment archmage = null;

    private static Enchantment getArchmage() {
        if (archmage == null) {
            archmage = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("aoa3", "archmage"));
        }
        return archmage;
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // 必须是符文袋 + 附魔书
        if (!(left.getItem() instanceof RunePouchItem)) return;
        if (right.isEmpty() || !(right.getItem() instanceof EnchantedBookItem)) return;

        Enchantment arch = getArchmage();
        if (arch == null) return;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(right);
        if (!enchants.containsKey(arch)) return;

        int level = enchants.get(arch);
        if (level < 1 || level > 3) return;

        // 创建结果：符文袋 + 法术附魔
        ItemStack result = left.copy();
        result.addEnchantment(arch, level);

        // 设置铁砧输出
        event.setOutput(result);
        event.setCost(level * 5);          // 消耗经验等级
        event.setMaterialCost(1);           // 消耗1个附魔书
    }
}
