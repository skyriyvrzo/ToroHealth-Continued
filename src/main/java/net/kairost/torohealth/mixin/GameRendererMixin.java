package net.kairost.torohealth.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.util.RayTrace;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void torohealth$preRenderWorld(float tickDelta, long limitTime, CallbackInfo info) {
        if (ToroHealth.getConfig().enabled) {
            LivingEntity entity = RayTrace.getEntityInCrosshair(tickDelta, Math.max(ToroHealth.getConfig().hudOptions.hudDistance, ToroHealth.getConfig().inWorldBarOptions.inWorldBarDistance));
            ToroHealth.setTargetedEntity(entity);
            ToroHealth.toroHealthHud.setEntity(entity);
        }
    }
}