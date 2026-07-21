package com.example.runepouch.item;

import com.example.runepouch.container.RunePouchContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class RunePouchItem extends Item {
    public static final int SLOTS = 18;

    public RunePouchItem() {
        super(new Properties().stacksTo(1).durability(500));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return ActionResult.success(stack);
        }
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (id, inv, p) -> new RunePouchContainer(id, inv, hand),
                new StringTextComponent("Rune Pouch")
        ), buf -> buf.writeByte(hand.ordinal()));
        return ActionResult.consume(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new CapabilityProvider(stack, nbt);
    }

    private static class CapabilityProvider implements ICapabilityProvider {
        private final ItemStackHandler handler;
        private final LazyOptional<ItemStackHandler> optional;

        public CapabilityProvider(ItemStack stack, @Nullable CompoundNBT nbt) {
            this.handler = new ItemStackHandler(SLOTS) {
                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    stack.getOrCreateTag().putBoolean("Dirty", true);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    if (stack.getItem().getRegistryName() == null) return false;
                    String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                    // 稳定匹配所有包含 "rune" 的 ID（完美兼容虚无世界3 的 fire_rune, water_rune 等）
                    return path.contains("rune");
                }
            };
            if (nbt != null && nbt.contains("Inventory")) {
                handler.deserializeNBT(nbt.getCompound("Inventory"));
            }
            this.optional = LazyOptional.of(() -> handler);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return optional.cast();
            }
            return LazyOptional.empty();
        }
    }
}
