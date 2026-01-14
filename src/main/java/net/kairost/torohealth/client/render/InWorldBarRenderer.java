package net.kairost.torohealth.client.render;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;
import net.kairost.torohealth.config.ToroHealthConfig;

public class InWorldBarRenderer {
    private static final int DARK_GRAY = 0x808080;
    private static final ResourceLocation TOROHEALTH_BARS_TEXTURES = new ResourceLocation("torohealth:textures/gui/bars.png");

    // referencing vanilla entity draw name tag function
    public static void render(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light,  EntityRenderDispatcher entityRenderDispatcher) {
        if (!shouldRender(entity, entityRenderDispatcher)) {
            return;
        }

        matrices.pushPose();
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
        EntityRenderer<? super Entity> entityRenderer = entityRenderDispatcher.getRenderer(entity);
        Vec3 vec3d = entityRenderer.getRenderOffset(entity, tickDelta);
        matrices.translate(x - cameraX + vec3d.x(), y - cameraY + vec3d.y(), z - cameraZ + vec3d.z());
        float f = entity.getNameTagOffsetY() + 0.2f;
        matrices.translate(0.0, f, 0.0);
        matrices.mulPose(entityRenderDispatcher.cameraOrientation());
        matrices.scale(-0.025f, -0.025f, 0.025f);
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderType.text(TOROHEALTH_BARS_TEXTURES));
        renderHealthBar(matrices, (LivingEntity)entity, -20.0F, 0.0F, light, buffer, tickDelta);
        matrices.popPose();
    }
    private static void renderHealthBar(PoseStack matrices, LivingEntity entity, float x, float y, int light, VertexConsumer buffer, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        Matrix4f matrix = matrices.last().pose();
        Relation relation = EntityUtil.getRelation(entity);
        int color = relation.equals(Relation.FOE) ? ToroHealthConfig.CONFIG.barColor.foeColor.get() : ToroHealthConfig.CONFIG.barColor.friendColor.get();
        int color2 = relation.equals(Relation.FOE) ? ToroHealthConfig.CONFIG.barColor.foeColorSecondary.get() : ToroHealthConfig.CONFIG.barColor.friendColorSecondary.get();
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(Mth.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();

        int width = Mth.ceil(percent * 41.0f);
        int width2 = Mth.ceil(percent2 * 41.0f);
        if (40 > width && 40 > width2) {
            renderBar(matrix, x, y, 0.0f, DARK_GRAY, 40, light, buffer);
        }
        if (width2 > width) {
            renderBar(matrix, x, y, -0.1f, color2, width2, light, buffer);
        }
        if (width > 0) {
            renderBar(matrix, x, y, -0.2f, color, width, light, buffer);
        }
    }


    private static void renderBar(Matrix4f matrix, float x0, float y0, float z, int color, int width, int light, VertexConsumer buffer) {
        float x1 = x0 +(float)width;
        float y1 = 5f + y0;
        float u0 = 0f / 256f;
        float u1 = 0f + width / 256f;
        float v0 = 135f / 256f;
        float v1 = 140f / 256f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        buffer.vertex(matrix, x0, y1, z).color(r, g, b, 1.0f).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, 1.0f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, x1, y0, z).color(r, g, b, 1.0f).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, x0, y0, z).color(r, g, b, 1.0f).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();

    }


    private static boolean shouldRender(Entity entity, EntityRenderDispatcher entityRenderDispatcher) {
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get().equals(ToroHealthConfig.InWorldBarVisibilityMode.NONE)) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get().equals(ToroHealthConfig.InWorldBarVisibilityMode.WHEN_HOLDING_WEAPON) && !ToroHealth.isHoldingWeapon()) {
            return false;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        if (entityRenderDispatcher.distanceToSqr(entity) > ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarDistanceSquared) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.onlyWhenHurt.get() && livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
            return false;
        }
        if (ToroHealthConfig.CONFIG.inWorldBarOptions.onlyWhenLookingAt.get() && ToroHealth.getTargetedEntity() != entity) {
            return false;
        }
        return EntityUtil.showHealthBar(entity, Minecraft.getInstance());
    }
}
