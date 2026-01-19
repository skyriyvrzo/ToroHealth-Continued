package net.kairost.torohealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.client.render.InWorldBarRenderer;
import net.kairost.torohealth.config.ToroHealthConfig;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At(value = "RETURN"))
    private void torohealth$renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo info) {
        if (ToroHealthConfig.CONFIG.enabled.get() && !ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get().equals(ToroHealthConfig.InWorldBarVisibilityMode.NONE)) {
            int light = ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarLightMode.get().equals(ToroHealthConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightTexture.FULL_BRIGHT : this.entityRenderDispatcher.getPackedLightCoords(entity, tickDelta);
            InWorldBarRenderer.render(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, light, this.entityRenderDispatcher);
        }
    }
}
