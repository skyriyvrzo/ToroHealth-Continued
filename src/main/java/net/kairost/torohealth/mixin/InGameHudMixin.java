package net.kairost.torohealth.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void torohealth$render(DrawContext context, float tickDelta, CallbackInfo info) {
        if (ToroHealth.getConfig().enabled && ToroHealth.getConfig().hudOptions.showHUD) {
            ToroHealth.toroHealthHud.render(context, tickDelta);
        }
    }
}
