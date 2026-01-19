package net.kairost.torohealth.client.render;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
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
    private static final ResourceLocation TOROHEALTH_BARS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ToroHealth.MODID, "textures/gui/bars.png");

    // referencing vanilla entity draw name tag function
    public static void render(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light,  EntityRenderDispatcher entityRenderDispatcher) {
        if (!shouldRender(entity, entityRenderDispatcher)) {
            return;
        }

        matrices.pushPose();
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
        EntityRenderer<Entity, EntityRenderState> entityRenderer = (EntityRenderer<Entity, EntityRenderState>) entityRenderDispatcher.getRenderer(entity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(entity, tickDelta);
        Vec3 vec3d = entityRenderer.getRenderOffset(entityRenderState);
        matrices.translate(x - cameraX + vec3d.x(), y - cameraY + vec3d.y(), z - cameraZ + vec3d.z());
        Vec3 labelPos = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(tickDelta));
        if (labelPos != null) {
            matrices.translate(labelPos.x, labelPos.y + 0.7f, labelPos.z);
        } else {
            float f = entity.getBbHeight() + 0.7f;
            matrices.translate(0.0, f, 0.0);
        }
        matrices.mulPose(entityRenderDispatcher.cameraOrientation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        renderHealthBar(matrices, (LivingEntity)entity, -20.0F, 0.0F, light, vertexConsumers, tickDelta);
        matrices.popPose();
    }

    private static void renderHealthBar(PoseStack matrices, LivingEntity entity, float x, float y, int light, MultiBufferSource vertexConsumers, float tickDelta) {
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
            renderBar(matrix, x, y, 0.0f, DARK_GRAY, 40, light, vertexConsumers);
        }
        if (width2 > width) {
            renderBar(matrix, x, y, 0.1f, color2, width2, light, vertexConsumers);
        }
        if (width > 0) {
            renderBar(matrix, x, y, 0.2f, color, width, light, vertexConsumers);
        }
    }


    private static void renderBar(Matrix4f matrix, float x1, float y1, float z, int color, int width, int light, MultiBufferSource vertexConsumers) {
        float x2 = x1 +(float)width;
        float y2 = 5f + y1;
        float u1 = 0f / 256f;
        float u2 = 0f + width / 256f;
        float v1 = 135f / 256f;
        float v2 = 140f / 256f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderType.entityCutout(TOROHEALTH_BARS_TEXTURE));
        buffer.addVertex(matrix, x1, y2, z).setColor(r, g, b, 1.0f).setUv(u1, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x2, y2, z).setColor(r, g, b, 1.0f).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x2, y1, z).setColor(r, g, b, 1.0f).setUv(u2, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x1, y1, z).setColor(r, g, b, 1.0f).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);

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
