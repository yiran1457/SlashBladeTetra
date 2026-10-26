package net.yiran.sbtetra.compat.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 把对 SBT 刀的配方查询重定向到对普通拔刀的查询。
 * <p>
 * 取代原先的 {@code InternalRecipeManagerPluginMixin}：那个 mixin 包裹了 JEI 内部的
 * {@code InternalRecipeManagerPlugin} 对 {@code IFocus#getTypedValue()} 的调用，
 * 而这里是 JEI 官方的 {@link IRecipeManagerPlugin} 扩展点。
 * <p>
 * 规避递归：回查一律使用“翻译后”的普通拔刀焦点。翻译对普通拔刀返回 null，
 * 因此本插件对自己的回查不生效，{@code PluginManager} 不会无限自调用。
 */
public class SBTRecipeManagerPlugin implements IRecipeManagerPlugin {
    private IRecipeManager recipeManager;
    private IFocusFactory focusFactory;

    /**
     * @return 翻译后的原料；不属于本插件处理范围时返回 null。
     * <p>
     * 按「原料本身是不是 SBT 刀」判断，而不是 {@code instanceof TypedItemStack}：
     * 后者是 JEI 内部类，跨版本会换包。
     */
    private static ITypedIngredient<?> translate(ITypedIngredient<?> ingredient) {
        ItemStack stack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        return stack.isEmpty() ? null : Util.translateStack(stack);
    }

    public void bind(IJeiRuntime runtime) {
        this.recipeManager = runtime.getRecipeManager();
        this.focusFactory = runtime.getJeiHelpers().getFocusFactory();
    }

    public void unbind() {
        this.recipeManager = null;
        this.focusFactory = null;
    }

    private boolean isReady() {
        return this.recipeManager != null && this.focusFactory != null;
    }

    private IFocus<?> translateFocus(IFocus<?> focus) {
        ITypedIngredient<?> translated = translate(focus.getTypedValue());
        return translated == null ? null : this.focusFactory.createFocus(focus.getRole(), translated);
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        if (!isReady()) {
            return List.of();
        }
        IFocus<?> translated = translateFocus(focus);
        if (translated == null) {
            return List.of();
        }
        return this.recipeManager.createRecipeCategoryLookup()
                .limitFocus(List.of(translated))
                .get()
                .<RecipeType<?>>map(IRecipeCategory::getRecipeType)
                .toList();
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> category, IFocus<V> focus) {
        if (!isReady()) {
            return List.of();
        }
        IFocus<?> translated = translateFocus(focus);
        if (translated == null) {
            return List.of();
        }
        return this.recipeManager.createRecipeLookup(category.getRecipeType())
                .limitFocus(List.of(translated))
                .get()
                .toList();
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> category) {
        return List.of();
    }
}
