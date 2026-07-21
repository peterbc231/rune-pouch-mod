package com.example.runepouch;

import com.example.runepouch.init.ModContainers;
import com.example.runepouch.init.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.ScreenManager;
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
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModHandler {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ScreenManager.register(ModContainers.RUNE_POUCH.get(), com.example.runepouch.client.screen.RunePouchScreen::new);
            });
        }
    }
}
