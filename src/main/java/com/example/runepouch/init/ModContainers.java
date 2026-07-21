package com.example.runepouch.init;

import com.example.runepouch.RunePouchMod;
import com.example.runepouch.container.RunePouchContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, RunePouchMod.MOD_ID);
    public static final RegistryObject<ContainerType<RunePouchContainer>> RUNE_POUCH = CONTAINERS.register("rune_pouch", () -> IForgeContainerType.create((id, inv, data) -> {
        net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.values()[data.readByte()];
        return new RunePouchContainer(id, inv, hand);
    }));
}
