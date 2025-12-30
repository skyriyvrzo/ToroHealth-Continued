package net.kairost.torohealth.mixin;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.SubmittableBatch;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.client.render.InWorldBarRenderer;
import net.kairost.torohealth.client.particle.TextRenderEntry;
import net.kairost.torohealth.client.particle.TextRenderQueue;
import net.kairost.torohealth.client.particle.TextParticleRenderer;
import net.kairost.torohealth.mixin.accessor.WorldRendererInvoker;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    @Final
    @Shadow
    private EntityRenderManager entityRenderManager;

    @Final
    @Shadow
    private SubmittableBatch particleBatch;

    @Final
    @Shadow
    private OrderedRenderCommandQueueImpl entityRenderCommandQueue;


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addToBatch(Lnet/minecraft/client/render/SubmittableBatch;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;F)V"))
    private void torohealth$render_AddInWorldBarsToBatch(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
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
        Vec3d vec3d = camera.getCameraPos();
        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
        Frustum frustum = ((WorldRendererInvoker) worldRenderer).torohealth$invokeSetupFrustum(positionMatrix, projectionMatrix, vec3d);
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();
        for (Entity entity : this.world.getEntities()) {
            float tickDelta = tickCounter.getTickProgress(true);
            int light = ModConfig.INSTANCE.inWorldBarOptions.inWorldBarLightMode.equals(ModConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightmapTextureManager.MAX_LIGHT_COORDINATE : this.entityRenderManager.getLight(entity, tickDelta);
            if (this.entityRenderManager.shouldRender(entity, frustum, d, e, f) || entity.hasPassengerDeep(this.client.player)) {
                InWorldBarRenderer.render(entity, camera, tickCounter.getTickProgress(true), light, entityRenderManager);
            }
        }
        this.particleBatch.add(InWorldBarRenderer.getSubmittable());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderParticles(Lnet/minecraft/client/render/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void torohealth$render_RenderTextParticles(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
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
            TextParticleRenderer.render(entry.text(), camera, entry.x(), entry.y(), entry.z(), entry.u(), entry.v(), entry.color(), entityRenderCommandQueue, entry.light());
        }
    }
}