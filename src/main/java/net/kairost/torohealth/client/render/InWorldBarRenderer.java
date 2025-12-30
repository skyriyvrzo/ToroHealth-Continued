package net.kairost.torohealth.client.render;

import org.joml.Matrix4f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;
import net.kairost.torohealth.data.BarStateAccessor;

public class InWorldBarRenderer {
    private static final int DARK_GRAY = 0x808080;
    private static final Identifier TOROHEALTH_BARS_TEXTURE = Identifier.of(ToroHealth.MODID, "textures/gui/bars.png");

    // referencing vanilla entity draw name tag function
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
        matrices.scale(0.025f, -0.025f, 0.025f);

        renderHealthBar(matrices, (LivingEntity)entity, -20.0F, 0.0F, light, vertexConsumers, tickDelta);
        matrices.pop();
    }
    private static void renderHealthBar(MatrixStack matrices, LivingEntity entity, float x, float y, int light, VertexConsumerProvider vertexConsumers, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Relation relation = EntityUtil.getRelation(entity);
        int color = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColor : ToroHealth.getConfig().barColor.friendColor;
        int color2 = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColorSecondary : ToroHealth.getConfig().barColor.friendColorSecondary;
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(MathHelper.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();

        int width = MathHelper.ceil(percent * 41.0f);
        int width2 = MathHelper.ceil(percent2 * 41.0f);
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


    private static void renderBar(Matrix4f matrix, float x1, float y1, float z, int color, int width, int light, VertexConsumerProvider vertexConsumers) {
        float x2 = x1 +(float)width;
        float y2 = 5f + y1;
        float u1 = 0f / 256f;
        float u2 = 0f + width / 256f;
        float v1 = 135f / 256f;
        float v2 = 140f / 256f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TOROHEALTH_BARS_TEXTURE));
        buffer.vertex(matrix, x1, y2, z).color(r, g, b, 1.0f).texture(u1, v2).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, x2, y2, z).color(r, g, b, 1.0f).texture(u2, v2).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, x2, y1, z).color(r, g, b, 1.0f).texture(u2, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, 1.0f).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);

    }


    private static boolean shouldRender(Entity entity, EntityRenderDispatcher entityRenderDispatcher) {
        if (ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
            return false;
        }
        if (ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.WHEN_HOLDING_WEAPON) && !ToroHealth.isHoldingWeapon()) {
            return false;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        if (entityRenderDispatcher.getSquaredDistanceToCamera(entity) > ToroHealth.getConfig().inWorldBarOptions.inWorldBarDistanceSquared) {
            return false;
        }
        if (ToroHealth.getConfig().inWorldBarOptions.onlyWhenHurt && livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
            return false;
        }
        if (ToroHealth.getConfig().inWorldBarOptions.onlyWhenLookingAt && ToroHealth.getTargetedEntity() != entity) {
            return false;
        }
        return EntityUtil.showHealthBar(entity, MinecraftClient.getInstance());
    }
}
