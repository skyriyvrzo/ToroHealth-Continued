package net.torocraft.torohealth.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.torocraft.torohealth.ModConfig;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.client.render.InWorldBarRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At(value = "RETURN"))
    private void torohealth$renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
        MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (ToroHealth.getConfig().enabled && !ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
            InWorldBarRenderer.render(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta), this.entityRenderDispatcher);
        }
    }
}