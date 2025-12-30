package net.kairost.torohealth.mixin.accessor;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererInvoker {
    @Invoker("setupFrustum")
    Frustum torohealth$invokeSetupFrustum(Matrix4f posMatrix, Matrix4f projMatrix, Vec3d pos);
}