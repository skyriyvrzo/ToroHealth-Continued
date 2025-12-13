package net.torocraft.torohealth.bars;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.OverlayTexture;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.ModConfig;
import net.torocraft.torohealth.util.EntityUtil;
import net.torocraft.torohealth.util.EntityUtil.Relation;
import net.torocraft.torohealth.api.BarStateAccessor;

public class InWorldBarRenderer {
    private static final int DARK_GRAY = 0x808080;
    private static final Identifier TOROHEALTH_BARS_TEXTURES = new Identifier("torohealth:textures/gui/bars.png");

    public static void render(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,  EntityRenderDispatcher entityRenderDispatcher) {
        if (!shouldRender(entity, entityRenderDispatcher)) {
            return;
        }

        matrices.push();
        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
        matrices.translate(x - cameraX, y - cameraY, z - cameraZ);
        float f = entity.getHeight() + 0.7f;
        matrices.translate(0.0, f, 0.0);
        matrices.multiply(entityRenderDispatcher.getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TOROHEALTH_BARS_TEXTURES));
        renderHealthBar(matrices, (LivingEntity)entity, -20.0F, 0.0F, light, buffer, tickDelta);
        matrices.pop();
    }
    private static void renderHealthBar(MatrixStack matrices, LivingEntity entity, float x, float y, int light, VertexConsumer buffer, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Relation relation = EntityUtil.determineRelation(entity);
        int color = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barColor.friendColor : ToroHealth.CONFIG.barColor.foeColor;
        int color2 = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barColor.friendColorSecondary : ToroHealth.CONFIG.barColor.foeColorSecondary;
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(MathHelper.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();

        int width = MathHelper.ceil(percent * 41.0f);
        int width2 = MathHelper.ceil(percent2 * 41.0f);
        renderBar(matrix, x, y, 0.0f, DARK_GRAY, 40, light, buffer);
        if (width2 > 0) {
            renderBar(matrix, x, y, -0.1f, color2, width2, light, buffer);
        }
        if (width > 0) {
            renderBar(matrix, x, y, -0.2f, color, width, light, buffer);
        }
    }


    private static void renderBar(Matrix4f matrix, float x, float y, float z, int color, int width, int light, VertexConsumer buffer) {
        float x0 = x;
        float x1 = x+(float)width;
        float y0 = y;
        float y1 = 5f + y;
        float u0 = 0f / 256f;
        float u1 = 0f + width / 256f;
        float v0 = 135f / 256f;
        float v1 = 140f / 256f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        buffer.vertex(matrix, x0, y1, z).color(r, g, b, 1.0f).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, 1.0f).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        buffer.vertex(matrix, x1, y0, z).color(r, g, b, 1.0f).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        buffer.vertex(matrix, x0, y0, z).color(r, g, b, 1.0f).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();

    }


    private static boolean shouldRender(Entity entity, EntityRenderDispatcher entityRenderDispatcher) {
        if (ToroHealth.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
            return false;
        }
        if (ToroHealth.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.WHEN_HOLDING_WEAPON) && !ToroHealth.IS_HOLDING_WEAPON) {
            return false;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        if (entityRenderDispatcher.getSquaredDistanceToCamera(entity) > ToroHealth.CONFIG.inWorldBarOptions.inWorldBarDistanceSquared) {
            return false;
        }
        if (ToroHealth.CONFIG.inWorldBarOptions.onlyWhenHurt && livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
            return false;
        }
        if (ToroHealth.CONFIG.inWorldBarOptions.onlyWhenLookingAt && ToroHealth.getTargetedEntity() != entity) {
            return false;
        }
        return EntityUtil.showHealthBar(entity, MinecraftClient.getInstance());
    }

}
