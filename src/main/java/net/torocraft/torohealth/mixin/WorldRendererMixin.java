package net.torocraft.torohealth.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.torocraft.torohealth.ModConfig;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.client.render.InWorldBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
  @Final
  @Shadow
  private EntityRenderDispatcher entityRenderDispatcher;


  @Inject(method = "renderEntity", at = @At(value = "RETURN"))
  private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
      MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
      if (ToroHealth.CONFIG.enabled && !ToroHealth.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
          InWorldBarRenderer.render(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta), this.entityRenderDispatcher);
      }
  }
}