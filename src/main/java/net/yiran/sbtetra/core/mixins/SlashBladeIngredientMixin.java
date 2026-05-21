package net.yiran.sbtetra.core.mixins;

import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(SlashBladeIngredient.class)
public class SlashBladeIngredientMixin extends Ingredient {
    @Shadow(remap = false)
    @Final
    private RequestDefinition request;

    @Mutable
    @Shadow(remap = false)
    @Final
    private Set<Item> items;

    protected SlashBladeIngredientMixin(Stream<? extends Value> p_43907_) {
        super(p_43907_);
    }

    @Inject(method = "<init>", remap = false, at = @At("RETURN"))
    private void init(Set<Item> items, RequestDefinition request, CallbackInfo ci) {
        Set<Item> itemSet = new HashSet<>(items);
        boolean wrapper = false;
        for (Item item : itemSet) {
            if (Config.CantWrapperItems.get().contains(ForgeRegistries.ITEMS.getKey(item).toString())) {
                continue;
            }
            itemSet.addAll(SBTIngredientManager.getItems());
            wrapper = true;
            break;
        }
        if (!wrapper) {
            return;
        }
        this.values = itemSet.stream()
                .map((item) -> {
                    if (item instanceof ISlashBladeTetra) {
                        ItemStack stack = ((ISlashBladeTetra) item).getDefaultStack();
                        request.initItemStack(stack);
                        return new Ingredient.ItemValue(stack);
                    }
                    ItemStack stack = new ItemStack(item);
                    request.initItemStack(stack);
                    return new Ingredient.ItemValue(stack);
                })
                .toArray(Value[]::new);
        this.items = Collections.unmodifiableSet(itemSet);

    }
}
