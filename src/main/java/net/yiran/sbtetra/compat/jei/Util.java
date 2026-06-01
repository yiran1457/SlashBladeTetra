package net.yiran.sbtetra.compat.jei;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import net.yiran.sbtetra.item.ISlashBladeTetra;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Util {
    public static Map<String, ItemStack> translation2blade = new Object2ObjectOpenHashMap<>();
    public static Map<String, List<ItemStack>> translation2modularBlade = new Object2ObjectOpenHashMap<>();

    //将对sbt刀的配方查询重定向到对普通拔刀查询
    @SuppressWarnings("unchecked")
    public static <V> ITypedIngredient<V> wrapperITypedIngredient(TypedItemStack ingredient) {
        if (ingredient.getIngredient().getItem() instanceof ISlashBladeTetra) {
            var state = ingredient.getIngredient().getCapability(ItemSlashBlade.BLADESTATE);
            if (state.isPresent()) {
                //获取翻译键
                var key = ingredient.getIngredient().getOrCreateTag().getCompound("bladeState").getString("translationKey");
                if (Minecraft.getInstance().getConnection() != null) {
                    //cache
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
                    if (!translation2blade.get(key).isEmpty())
                        return (ITypedIngredient<V>) TypedItemStack.create(translation2blade.get(key));
                }
            }
        }
        return (ITypedIngredient<V>) ingredient;
    }

    //在配方转移的时候向普通拔刀的需求里面插入sbt拔刀
    public static Stream<ITypedIngredient<?>> injectITypedIngredient(ITypedIngredient<?> ingredient) {
        if (ingredient.getItemStack().isPresent()) {
            var stack = ingredient.getItemStack().get();
            if (stack.getItem() instanceof ItemSlashBlade) {
                var state = stack.getCapability(ItemSlashBlade.BLADESTATE);
                if (state.isPresent()) {
                    var key = stack.getOrCreateTag().getCompound("bladeState").getString("translationKey");
                    if (Minecraft.getInstance().getConnection() != null) {
                        if (!translation2modularBlade.containsKey(key)) {
                            for (SlashBladeDefinition definition : Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY)) {
                                if (Objects.equals(definition.getTranslationKey(), key)) {
                                    translation2modularBlade.put(key, SBTIngredientManager.getItems().stream().map(definition::getBlade).toList());
                                    break;
                                }
                            }
                            if (!translation2modularBlade.containsKey(key)) {
                                translation2modularBlade.put(key, List.of());
                            }
                        }
                        return Stream.concat(translation2modularBlade.get(key).stream().map(TypedItemStack::create), Stream.of(ingredient));
                    }
                }
            }
        }
        return Stream.of(ingredient);
    }
}
