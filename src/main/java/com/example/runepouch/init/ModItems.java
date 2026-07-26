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

    // 尝试获取 AoA3 法杖标签页，若不存在则回退到工具标签页
    private static ItemGroup getAoAStavesTab() {
        // AoA3 标签页 ID 可能是 "aoa3:staves" 或 "aoa3.staves"
        ItemGroup tab = ItemGroup.byName("aoa3:staves");
        if (tab == null) {
            tab = ItemGroup.byName("aoa3.staves");
        }
        return tab != null ? tab : ItemGroup.TOOLS;
    }

    public static final RegistryObject<Item> RUNE_POUCH = ITEMS.register("rune_pouch",
            () -> new RunePouchItem(new Item.Properties().group(getAoAStavesTab())));
}
