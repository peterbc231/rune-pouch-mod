package com.example.runepouch.init;

import com.example.runepouch.RunePouchMod;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RunePouchMod.MOD_ID);

    // 直接使用工具标签页
    public static final RegistryObject<Item> RUNE_POUCH = ITEMS.register("rune_pouch",
            () -> new RunePouchItem(new Item.Properties().group(ItemGroup.TOOLS)));
}
