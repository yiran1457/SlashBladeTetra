package net.yiran.sbtetra.core.mixins;

import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModule;

@Mixin(ItemModule.class)
public class ItemModuleMixin {
    @Inject(method = "getName(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "HEAD"), cancellable = true)
    private static void getName(String moduleKey, String variantKey, CallbackInfoReturnable<String> cir) {
        if (moduleKey.equals("slashblade/soul/sa")) {
            cir.setReturnValue(I18n.get("tetra.module.sa", I18n.get("slash_art." + variantKey.substring(3).replace(":", "."))));
        }
    }
}
