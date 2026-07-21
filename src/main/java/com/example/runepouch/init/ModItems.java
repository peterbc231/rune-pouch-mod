package com.example.runepouch.init;

import com.example.runepouch.RunePouchMod;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RunePouchMod.MOD_ID);
    public static final RegistryObject<Item> RUNE_POUCH = ITEMS.register("rune_pouch", RunePouchItem::new);
}
