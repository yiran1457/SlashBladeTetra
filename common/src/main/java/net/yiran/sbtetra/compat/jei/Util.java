package net.yiran.sbtetra.compat.jei;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.item.ISlashBladeTetra;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Util {
    public static Map<String, ItemStack> translation2blade = new Object2ObjectOpenHashMap<>();
    public static Map<String, List<ItemStack>> translation2modularBlade = new Object2ObjectOpenHashMap<>();

    /**
     * JEI 的 {@code IIngredientManager}，在 {@code onRuntimeAvailable} 里注入。
     * <p>
     * 用它代替以前直接 new {@code TypedItemStack}：{@code TypedItemStack} 是 JEI 内部类，
     * 6.9 的 JEI 在 {@code mezz.jei.library.ingredients.itemStacks}，新版挪到了
     * {@code mezz.jei.common.ingredients.itemStacks}。而
     * {@link IIngredientManager#createTypedIngredient} 与
     * {@link VanillaTypes#ITEM_STACK} 都是公开 API，跨版本稳定。
     */
    public static IIngredientManager ingredientManager;

    /**
     * 把一叠 SBT 刀翻译成同等翻译键的普通拔刀。
     *
     * @return 翻译结果；不是 SBT 刀、查不到对应普通拔刀、或 JEI 运行时不可用时返回 {@code null}。
     */
    public static ITypedIngredient<ItemStack> translateStack(ItemStack stack) {
        if (!isTranslatable(stack)) {
            return null;
        }
        var state = stack.getCapability(ItemSlashBlade.BLADESTATE);
        if (!state.isPresent() || Minecraft.getInstance().getConnection() == null) {
            return null;
        }
        //获取翻译键
        var key = stack.getOrCreateTag().getCompound("bladeState").getString("translationKey");
        if (!translation2blade.containsKey(key)) {
            for (SlashBladeDefinition definition : Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY)) {
                if (Objects.equals(definition.getTranslationKey(), key)) {
                    translation2blade.put(key, definition.getBlade());
                    break;
                }
            }
            if (!translation2blade.containsKey(key)) {
                translation2blade.put(key, ItemStack.EMPTY);
            }
        }
        ItemStack blade = translation2blade.get(key);
        return blade.isEmpty() ? null : createTyped(blade);
    }

    /**
     * 判断这叠物品是否是「SBT 刀」，即需要做焦点重定向的那种。
     */
    public static boolean isTranslatable(ItemStack stack) {
        return stack.getItem() instanceof ISlashBladeTetra;
    }

    /**
     * 经 JEI 公开 API 创建 typed ingredient；运行时不可用时返回 {@code null}。
     */
    public static ITypedIngredient<ItemStack> createTyped(ItemStack stack) {
        IIngredientManager manager = ingredientManager;
        if (manager == null) {
            return null;
        }
        return manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack).orElse(null);
    }
}
