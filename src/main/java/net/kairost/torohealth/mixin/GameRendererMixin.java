package net.kairost.torohealth.mixin;

import net.kairost.torohealth.config.ToroHealthConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.util.RayTrace;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V", shift = At.Shift.AFTER))
    private void torohealth$onUpdateTargetedEntity(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo info) {
        if (ToroHealthConfig.CONFIG.enabled.get()) {
            LivingEntity entity = RayTrace.getEntityInCrosshair(tickDelta, (float) Math.max(ToroHealthConfig.CONFIG.hudOptions.hudDistance.get(), ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarDistance.get()));
            ToroHealth.setTargetedEntity(entity);
            ToroHealth.toroHealthHud.setEntity(entity);
        }
    }
}