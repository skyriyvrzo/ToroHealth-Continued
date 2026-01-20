package net.kairost.torohealth.mixin;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.client.render.InWorldBarRenderer;
import net.kairost.torohealth.config.ToroHealthConfig;
import net.kairost.torohealth.client.particle.TextRenderEntry;
import net.kairost.torohealth.client.particle.TextRenderQueue;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Final
    @Shadow
    private ParticlesRenderState particlesRenderState;

    @Final
    @Shadow
    private SubmitNodeStorage submitNodeStorage;


    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))
    private void torohealth$render_AddInWorldBarsToBatch(
        GraphicsResourceAllocator allocator,
        DeltaTracker tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f matrix4f,
        Matrix4f projectionMatrix,
        GpuBufferSlice fogBuffer,
        Vector4f fogColor,
        boolean renderSky,
        CallbackInfo callbackInfo,
        @Local(ordinal = 0) Frustum frustum
    ) {
        Vec3 vec3d = camera.position();
        double d = vec3d.x();
        double e = vec3d.y();
        double f = vec3d.z();
        for (Entity entity : this.level.entitiesForRendering()) {
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
            int light = ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarLightMode.get().equals(ToroHealthConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightTexture.FULL_BRIGHT : this.entityRenderDispatcher.getPackedLightCoords(entity, tickDelta);
            if (this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(this.minecraft.player)) {
                InWorldBarRenderer.render(entity, camera, tickDelta, light, entityRenderDispatcher);
            }
        }
        this.particlesRenderState.add(InWorldBarRenderer.getSubmittable());
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addParticlesPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4f;)V"))
    private void torohealth$render_RenderTextParticles(
        GraphicsResourceAllocator allocator,
        DeltaTracker tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f matrix4f,
        Matrix4f projectionMatrix,
        GpuBufferSlice fogBuffer,
        Vector4f fogColor,
        boolean renderSky,
        CallbackInfo callbackInfo
    ) {
        for (TextRenderEntry entry : TextRenderQueue.consume()) {
            ToroHealth.textParticleRenderer.render(entry.text(), camera, entry.x(), entry.y(), entry.z(), entry.u(), entry.v(), entry.color(), submitNodeStorage, entry.light());
        }
    }
}
