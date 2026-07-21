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
        super(new Properties().maxStackSize(1).maxDamage(500));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return ActionResult.resultSuccess(stack);
        }
        stack.damageItem(1, player, (p) -> p.sendBreakAnimation(hand));
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (id, inv, p) -> new RunePouchContainer(id, inv, hand),
                new StringTextComponent("Rune Pouch")
        ), buf -> buf.writeEnumValue(hand));
        return ActionResult.resultConsume(stack);
    }

    // 注意：这里没有 @Override，因为此方法在 Forge 1.16.5 中不要求覆盖
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilityProvider() {
            private final ItemStackHandler handler = new ItemStackHandler(SLOTS) {
                @Override
                protected void onContentsChanged(int slot) {
                    stack.getOrCreateTag().putBoolean("Dirty", true);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    if (stack.getItem().getRegistryName() == null) return false;
                    String path = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
                    return path.contains("rune");
                }
            };
            private final LazyOptional<ItemStackHandler> optional = LazyOptional.of(() -> handler);

            {
                if (nbt != null && nbt.contains("Inventory")) {
                    handler.deserializeNBT(nbt.getCompound("Inventory"));
                }
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.util.Direction side) {
                if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .filter(h -> h instanceof ItemStackHandler)
                .ifPresent(h -> tag.put("Inventory", ((ItemStackHandler) h).serializeNBT()));
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("Inventory")) {
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .filter(h -> h instanceof ItemStackHandler)
                    .ifPresent(h -> ((ItemStackHandler) h).deserializeNBT(nbt.getCompound("Inventory")));
        }
        super.readShareTag(stack, nbt);
    }
}
