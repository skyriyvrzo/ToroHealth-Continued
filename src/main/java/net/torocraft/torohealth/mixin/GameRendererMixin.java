package net.torocraft.torohealth.mixin;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;

import net.minecraft.entity.LivingEntity;
import net.torocraft.torohealth.ToroHealth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.torocraft.torohealth.client.util.RayTrace;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void updateTargetedEntity(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        if (ToroHealth.CONFIG.enabled) {
            LivingEntity entity = RayTrace.getEntityInCrosshair(tickDelta, Math.max(ToroHealth.CONFIG.hudOptions.hudDistance, ToroHealth.CONFIG.inWorldBarOptions.inWorldBarDistance));
            ToroHealth.setTargetedEntity(entity);
            ToroHealth.toroHealthHud.setEntity(entity);
        }
    }
}