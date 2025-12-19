package net.torocraft.torohealth.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.torocraft.torohealth.ToroHealth;

@Mixin(InGameHud.class)
public class InGameHudMixin {
  @Inject(method = "render", at = @At("RETURN"))
  private void torohealth$render(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
      if (ToroHealth.getConfig().enabled && ToroHealth.getConfig().hudOptions.showHUD) {
          ToroHealth.toroHealthHud.render(matrixStack, tickDelta);
      }
  }
}
