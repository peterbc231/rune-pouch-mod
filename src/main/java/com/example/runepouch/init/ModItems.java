package com.example.runepouch.init;

import com.example.runepouch.RunePouchMod;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RunePouchMod.MOD_ID);

    // 创造标签页：使用符文袋作为图标
    public static final ItemGroup RUNE_POUCH_GROUP = new ItemGroup("runepouch_tab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(RUNE_POUCH.get());
        }
    };

    public static final RegistryObject<Item> RUNE_POUCH = ITEMS.register("rune_pouch",
            () -> new RunePouchItem(new Item.Properties().group(ItemGroup.TOOLS)));
}
