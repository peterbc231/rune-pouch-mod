package com.example.runepouch.mixin;

// ... (import 语句保持不变) ...

@Mixin(targets = "net.tslat.aoa3.util.ItemUtil")
public class ItemUtilMixin {

    @Inject(method = "findAndConsumeRunes", at = @At("RETURN"), cancellable = true)
    private static void onFindAndConsumeRunes(HashMap<Item, Integer> runeMap, ServerPlayerEntity player,
                                               boolean allowBuffs, @Nonnull ItemStack heldItem,
                                               CallbackInfoReturnable<Boolean> cir) {
        // 只有原方法返回 true（即成功从背包扣除了符文）时，我们才进行拦截
        if (!cir.getReturnValueZ()) {
            return;
        }

        // 1. 获取护符栏中的符文袋
        // ... (查找符文袋的逻辑保持不变) ...
        if (pouchStack.isEmpty()) return;

        // 2. 获取符文袋的 ItemStackHandler
        // ... (获取 handler 的逻辑保持不变) ...

        // 3. 从符文袋中扣除与 runeMap 数量相同的符文
        // 注意：这里的 runeMap 已经是经过附魔减免后的数量（即1个）
        // ... (扣除逻辑保持不变) ...

        // 4. 强制保存
        // ... (保存逻辑保持不变) ...

        // 5. 消耗耐久
        pouchStack.damageItem(1, player, (p) -> p.sendBreakAnimation(net.minecraft.util.Hand.MAIN_HAND));

        // 6. 返回 true，表示操作成功
        cir.setReturnValue(true);
    }
}
