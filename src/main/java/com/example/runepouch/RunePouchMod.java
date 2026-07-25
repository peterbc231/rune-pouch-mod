package com.example.runepouch;

import com.example.runepouch.client.screen.RunePouchScreen;
import com.example.runepouch.curio.CurioProvider;
import com.example.runepouch.event.ItemEventHandler;
import com.example.runepouch.init.ModContainers;
import com.example.runepouch.init.ModItems;
import com.example.runepouch.item.RunePouchItem;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(RunePouchMod.MOD_ID)
public class RunePouchMod {
    public static final String MOD_ID = "runepouch";
    public static final Logger LOGGER = LogManager.getLogger();

    public RunePouchMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModContainers.CONTAINERS.register(bus);
        bus.addListener(this::clientSetup);
        bus.addListener(this::enqueueIMC);
        MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
        // 监听物品能力附加事件，用于动态绑定饰品能力（模仿 CurioOfUndying）
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::attachCapabilities);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenManager.registerFactory(ModContainers.RUNE_POUCH.get(), RunePouchScreen::new);
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BACK.getMessageBuilder()
                        .size(1)
                        .build());
        LOGGER.info("Registered curios slot: back");
    }

    private void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof RunePouchItem) {
            event.addCapability(new ResourceLocation(MOD_ID, "curio"), new CurioProvider(stack));
        }
    }
}
