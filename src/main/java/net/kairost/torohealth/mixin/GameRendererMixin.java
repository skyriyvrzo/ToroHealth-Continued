package net.kairost.torohealth.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.util.RayTrace;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V", shift = At.Shift.AFTER))
    private void torohealth$preRenderWorld(RenderTickCounter tickCounter, CallbackInfo info) {
        if (ToroHealth.getConfig().enabled) {
            float tickDelta = tickCounter.getTickDelta(true);
            LivingEntity entity = RayTrace.getEntityInCrosshair(tickDelta, Math.max(ToroHealth.getConfig().hudOptions.hudDistance, ToroHealth.getConfig().inWorldBarOptions.inWorldBarDistance));
            ToroHealth.setTargetedEntity(entity);
            ToroHealth.toroHealthHud.setEntity(entity);
        }
    }
}