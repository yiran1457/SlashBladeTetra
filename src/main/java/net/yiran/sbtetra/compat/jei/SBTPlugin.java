package net.yiran.sbtetra.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.SlashBladeTetra;

@JeiPlugin
public class SBTPlugin implements IModPlugin {
    private final SBTRecipeManagerPlugin recipeManagerPlugin = new SBTRecipeManagerPlugin();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("sbt:plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        Util.translation2blade.clear();
        Util.translation2modularBlade.clear();
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SlashBladeTetra.MODLUAR.get(), SBTPlugin::syncSlashBlade);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        // 取代 InternalRecipeManagerPluginMixin：焦点重定向改由官方扩展点完成。
        registration.addRecipeManagerPlugin(this.recipeManagerPlugin);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        Util.translation2blade.clear();
        Util.translation2modularBlade.clear();
        Util.ingredientManager = jeiRuntime.getIngredientManager();
        this.recipeManagerPlugin.bind(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        this.recipeManagerPlugin.unbind();
        Util.ingredientManager = null;
        Util.translation2blade.clear();
        Util.translation2modularBlade.clear();
    }

    public static String syncSlashBlade(ItemStack stack, UidContext context) {
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent((cap) -> {
            if (stack.getOrCreateTag().contains("bladeState")) {
                cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
            }

        });
        return stack.getCapability(ItemSlashBlade.BLADESTATE).map(ISlashBladeState::getTranslationKey).orElse("");
    }
}
