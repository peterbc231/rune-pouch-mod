package com.example.runepouch;

import com.example.runepouch.client.screen.RunePouchScreen;
import com.example.runepouch.event.ItemEventHandler;
import com.example.runepouch.init.ModContainers;
import com.example.runepouch.init.ModItems;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RunePouchMod.MOD_ID)
public class RunePouchMod {
    public static final String MOD_ID = "runepouch";
    public static final Logger LOGGER = LogManager.getLogger();

    public RunePouchMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModContainers.CONTAINERS.register(bus);
        bus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenManager.registerFactory(ModContainers.RUNE_POUCH.get(), RunePouchScreen::new);
        });
    }
}
