package net.kairost.torohealth.mixin;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
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

import java.util.Objects;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
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
    LevelRenderState levelRenderState;

    @Final
    @Shadow
    private SubmitNodeStorage submitNodeStorage;



    @Inject(method = "extractLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;extract(Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;F)V"))
    private void torohealth$render_AddInWorldBarsToBatch(
        DeltaTracker deltaTracker,
        Camera camera,
        float deltaPaitialTick,
        CallbackInfo callbackInfo,
        @Local(name = "cullFrustum") Frustum frustum
    ) {
        Vec3 vec3d = camera.position();
        double d = vec3d.x();
        double e = vec3d.y();
        double f = vec3d.z();
        for (Entity entity : this.level.entitiesForRendering()) {
            int light = ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarLightMode.get().equals(ToroHealthConfig.InWorldBarLightMode.FULL_BRIGHT) ? LightCoordsUtil.FULL_BRIGHT : this.entityRenderDispatcher.getPackedLightCoords(entity, deltaPaitialTick);
            if (this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(Objects.requireNonNull(this.minecraft.player))) {
                InWorldBarRenderer.render(entity, camera, deltaPaitialTick, light, entityRenderDispatcher);
            }
        }
        this.levelRenderState.particlesRenderState.add(InWorldBarRenderer.getSubmittable());
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"))
    private void torohealth$render_RenderTextParticles(
        GraphicsResourceAllocator resourceAllocator,
        DeltaTracker deltaTracker,
        boolean renderOutline,
        CameraRenderState cameraState,
        Matrix4fc modelViewMatrix,
        GpuBufferSlice terrainFog,
        Vector4f fogColor,
        boolean shouldRenderSky,
        ChunkSectionsToRender chunkSectionsToRender,
        CallbackInfo callbackInfo
    ) {
        for (TextRenderEntry entry : TextRenderQueue.consume()) {
            ToroHealth.textParticleRenderer.render(entry.text(), levelRenderState.cameraRenderState.orientation, entry.x(), entry.y(), entry.z(), entry.u(), entry.v(), entry.color(), submitNodeStorage, entry.light());
        }
    }
}
