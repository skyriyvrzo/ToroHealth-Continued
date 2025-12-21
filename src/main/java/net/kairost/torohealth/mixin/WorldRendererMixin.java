package net.kairost.torohealth.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.render.InWorldBarRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At(value = "RETURN"))
    private void torohealth$renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
        MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (ToroHealth.getConfig().enabled && !ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
            int light = ModConfig.INSTANCE.inWorldBarOptions.inWorldBarLightMode.equals(ModConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightmapTextureManager.MAX_LIGHT_COORDINATE : this.entityRenderDispatcher.getLight(entity, tickDelta);
            InWorldBarRenderer.render(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, light, this.entityRenderDispatcher);
        }
    }
}