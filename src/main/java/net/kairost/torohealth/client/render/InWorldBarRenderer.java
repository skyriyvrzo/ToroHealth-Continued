package net.kairost.torohealth.client.render;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;
import net.kairost.torohealth.data.BarStateAccessor;

public class InWorldBarRenderer {
    private static final int DARK_GRAY = 0xFF808080;
    private static final float SIZE = 0.025f;
    private static final int BAR_WIDTH = 40;
    private static final Identifier IN_WORLD_BAR = Identifier.of(ToroHealth.MODID, "in_world_bar");
    private static final Sprite sprite = MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.PARTICLES).getSprite(IN_WORLD_BAR);
    private static final TextureSubmittable submittable = new TextureSubmittable();

    public static void render(Entity entity, Camera camera, float tickDelta, int light, EntityRenderManager entityRenderManager) {
        if (!shouldRender(entity, entityRenderManager)) {
            return;
        }

        Vec3d vec3d = camera.getPos();
        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - vec3d.x;
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - vec3d.y;
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - vec3d.z;
        float f = entity.getHeight() + 0.7f;
        Vector3f vector3f = new Vector3f((float) -BAR_WIDTH / 2, -5, 0.0F).rotate(camera.getRotation()).mul(SIZE).add((float) x, (float) y + f, (float) z);

        renderHealthBar((LivingEntity)entity, vector3f.x, vector3f.y, vector3f.z, new Quaternionf(camera.getRotation()), light, tickDelta);
    }

    private static void renderHealthBar(LivingEntity entity, float x, float y, float z, Quaternionf quaternionf,int light, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        Relation relation = EntityUtil.getRelation(entity);
        int color = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColor : ToroHealth.getConfig().barColor.friendColor;
        int color2 = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColorSecondary : ToroHealth.getConfig().barColor.friendColorSecondary;
        color = color | (0xFF << 24);
        color2 = color2 | (0xFF << 24);
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(MathHelper.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();

        int width = Math.min(MathHelper.ceil(percent * 41.0f), BAR_WIDTH);
        int width2 = Math.min(MathHelper.ceil(percent2 * 41.0f), BAR_WIDTH);

        Vector3f shift = new Vector3f(0f, 0f, 0.1f).rotate(quaternionf).mul(SIZE);

        if (40 > width && 40 > width2) {
            renderBar(x, y, z, DARK_GRAY, BAR_WIDTH, light, quaternionf);
        }
        if (width2 > width) {
            renderBar(x + shift.x, y + shift.y, z + shift.z, color2, width2, light, quaternionf);
        }
        if (width > 0) {
            renderBar(x + 2 * shift.x, y + 2 * shift.y, z + 2 * shift.z, color, width, light, quaternionf);
        }
    }


    private static void renderBar(float x, float y, float z, int color, int width, int light, Quaternionf rotation) {
        float u1 = sprite.getMinU();
        float u2 = MathHelper.lerp((float) width / BAR_WIDTH, sprite.getMinU(), sprite.getMaxU());
        float v1 = sprite.getMinV();
        float v2 = sprite.getMaxV();

        submittable.render(BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT, x, y, z, (float) width, 5f, rotation.x, rotation.y, rotation.z, rotation.w, SIZE, u1, u2, v1, v2, color, light);
    }


    private static boolean shouldRender(Entity entity, EntityRenderManager entityRenderManager) {
        if (ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.NONE)) {
            return false;
        }
        if (ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode.equals(ModConfig.InWorldBarVisibilityMode.WHEN_HOLDING_WEAPON) && !ToroHealth.isHoldingWeapon()) {
            return false;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        if (entityRenderManager.getSquaredDistanceToCamera(entity) > ToroHealth.getConfig().inWorldBarOptions.inWorldBarDistanceSquared) {
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

    public static TextureSubmittable getSubmittable() {
        return submittable;
    }
}
