package net.kairost.torohealth.client.gui;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig.FrameStyle;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;

public class ToroHealthHud extends DrawableHelper {
    private static final Identifier ICON_TEXTURES = new Identifier("textures/gui/icons.png");
    private static final Identifier TOROHEALTH_BARS_TEXTURES = new Identifier(ToroHealth.MODID + ":textures/gui/bars.png");
    private static final Identifier TOROHEALTH_FRAME_TEXTURE = new Identifier(ToroHealth.MODID + ":textures/gui/frame.png");
    private static final int DARK_GRAY = 0x808080;
    private static final int LIGHT_GRAY = 0xe0e0e0;
    private static final int FRAME_SIZE = 42;
    private static final float ENTITY_RENDER_HEIGHT = 32f;
    private static final float ENTITY_RENDER_WIDTH = 24f;
    private static final float ENTITY_RENDER_SCALE = 32f;
    private static final int INFO_Y_BASE = 2;
    private static final int INFO_X_BASE = 2;
    private static final int INFO_SPACING = 4;
    private static final int BAR_Y = 12;
    private static final int BAR_SIZE = 130;
    private static final int HEALTH_CHANGE_Y = 18;
    private final MinecraftClient client;
    private LivingEntity entity;
    private int age;
    private float entityX;
    private float entityY;
    private float entityScale;

    public ToroHealthHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrix, float tickDelta) {
        if (entity == null) {
            return;
        }
        if (entity.isRemoved()) {
            return;
        }
        if (ToroHealth.getConfig().hudOptions.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        matrix.push();
        float x = determineX();
        float y = determineY();
        matrix.translate(x, y, 0);
        int scale = ToroHealth.getConfig().hudOptions.hudScale;
        matrix.scale(scale, scale, scale);
        if (ToroHealth.getConfig().hudOptions.showEntity) {
            this.renderFrame(matrix);
            drawEntity(matrix,  this.entityX,  this.entityY, this.entityScale, -80, -20, entity, tickDelta);
            matrix.translate(FRAME_SIZE + 2, INFO_Y_BASE + (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.HEAVY)? 2 : 0), 0);
        }

        // draw entity info
        this.renderInfo(matrix, entity, tickDelta);
        matrix.pop();
    }

    private float determineX() {
        float x = ToroHealth.getConfig().hudOptions.hudXPosition;
        float wScreen = this.client.getWindow().getScaledWidth();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
                case BOTTOM_CENTER, TOP_CENTER -> (wScreen / 2) + x;
                case BOTTOM_RIGHT, TOP_RIGHT -> (wScreen) + x;
                default -> x;
        };
    }

    private float determineY() {
        float y = ToroHealth.getConfig().hudOptions.hudYPosition;
        float hScreen = client.getWindow().getScaledHeight();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
            case BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT -> y + hScreen;
            default -> y;
        };
    }

    public void tick()  {
        if (this.entity != null) {
            setEntityRenderPos();
            age++;
        }
    }

    public void setEntity(LivingEntity entity) {
        if (entity != null) {
            this.age = 0;
            if (entity != this.entity) {
                setEntityWork(entity);
            }
        }

        if (entity == null && age > ToroHealth.getConfig().hudOptions.hudHideDelay) {
            setEntityWork(null);
        }
    }

    private void setEntityWork(LivingEntity entity)  {
        this.entity = entity;
        if  (entity !=  null) {
            if (entity instanceof EnderDragonEntity) {
                this.entityScale = 2 * ENTITY_RENDER_HEIGHT / entity.getHeight();
            }
            else {
                float scale = this.entity.getScaleFactor();
                float height = this.entity.getHeight() / scale;
                float width = this.entity.getWidth() / scale;
                this.entityScale = Math.min(ENTITY_RENDER_HEIGHT / height, ENTITY_RENDER_WIDTH / width);

                // restrict entity scale
                if (this.entityScale > ENTITY_RENDER_SCALE) {
                    this.entityScale = ENTITY_RENDER_SCALE;
                }
                else if (this.entityScale > ENTITY_RENDER_SCALE / 2) {
                    // unchange
                }
                else if (this.entityScale > ENTITY_RENDER_SCALE / 2.5 ) {
                    this.entityScale = ENTITY_RENDER_SCALE / 2;
                }
                else {
                    this.entityScale = 5 * this.entityScale / 4;
                }
            }

            setEntityRenderPos();
        }
        else {
            this.entityScale = 0;
            this.entityX = 0;
            this.entityY = 0;
        }
    }


    private void setEntityRenderPos() {
        assert this.entity != null;
        if (this.entityX == 0) {
            this.entityX = (float) FRAME_SIZE / 2;
        }
        if (this.entityY == 0) {
            // default
            this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
        }
        if (this.entity instanceof GhastEntity) {
            this.entityY = 3;
        }
        else if (this.entity instanceof EnderDragonEntity) {
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * this.entityScale / 4;
        }
        else if (this.entity instanceof ShulkerEntity shulker) {
            switch (shulker.getAttachedFace()){
                case DOWN:
                    this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
                    break;
                case UP:
                    this.entityY = (float) FRAME_SIZE / 2 - ENTITY_RENDER_HEIGHT / 2 + entity.getHeight() * entityScale;
                    break;
                case NORTH, SOUTH, EAST, WEST:
                    this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * this.entityScale / 2;
                    break;
            }
        }
        else if (this.entity instanceof VillagerEntity villager && villager.isSleeping())
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * this.entityScale / 2;
        else if (this.entity instanceof PlayerEntity player && player.isSleeping())
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * this.entityScale / 2;
        else if (this.entity instanceof BatEntity bat && !bat.isRoosting())
            this.entityY = (float) FRAME_SIZE / 2 - ENTITY_RENDER_HEIGHT / 2 + entity.getHeight() * entityScale;
        else if (this.entity instanceof SpiderEntity spider && spider.isClimbing())
            this.entityY = (float) FRAME_SIZE / 2 + this.entity.getHeight() * this.entityScale / 2;
        else if (this.entity.hasVehicle() || EntityUtil.isFloating(this.entity) || this.entity.hasStatusEffect(StatusEffects.LEVITATION))
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * this.entityScale / 2;
        else if (this.entity.isOnGround())
            this.entityY = (float) FRAME_SIZE / 2 + ENTITY_RENDER_HEIGHT / 2;
    }


    private void renderFrame(MatrixStack matrix) {
        RenderSystem.setShaderTexture(0, TOROHEALTH_FRAME_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int w = 179, h = 42;
        drawTexture(matrix, 0, 0, 0, (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.LIGHT) ? 42 : 0), w, h);
    }


    private void renderInfo(MatrixStack matrix, LivingEntity entity, float tickDelta) {
        // render bar
        this.renderHealthBar(matrix, entity, 0, BAR_Y, tickDelta);

        int xOffset = INFO_X_BASE;
        // name
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
        String name = entity.getDisplayName().getString();
        this.client.textRenderer.drawWithShadow(matrix, name, (float)xOffset, 1f, 0xFFFFFF);
        xOffset += this.client.textRenderer.getWidth(name) + INFO_SPACING;

        this.renderHeartIcon(matrix, xOffset,0);
        xOffset += 10;

        // health
        int healthMax = MathHelper.ceil(entity.getMaxHealth());
        int healthCurrent = MathHelper.clamp(
            MathHelper.ceil(entity.getHealth()),
            0,
            healthMax
        );
        String healthText = healthCurrent + "/" + healthMax;
        this.client.textRenderer.drawWithShadow(matrix, healthText, (float)xOffset, 1f, 0xffffff);
        xOffset += this.client.textRenderer.getWidth(healthText) + INFO_SPACING;

        // armor
        int armor = entity.getArmor();
        if (armor > 0) {
            renderArmorIcon(matrix, xOffset, 0);
            xOffset += 10;
            this.client.textRenderer.drawWithShadow(matrix, Integer.toString(entity.getArmor()), xOffset, 1f, 0xffffff);
        }

        // render health change
        this.renderHealthChangeText(matrix, entity, BAR_SIZE, HEALTH_CHANGE_Y);
    }

    private void renderHeartIcon(MatrixStack matrix, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ICON_TEXTURES);
        drawTexture(matrix, x, y, 16, 0, 9, 9);
        drawTexture(matrix, x, y, 16 + 36, 0, 9, 9);
    }

    private void renderArmorIcon(MatrixStack matrix, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ICON_TEXTURES);
        drawTexture(matrix, x, y, 34, 9, 9, 9);
    }

    private void renderHealthChangeText(MatrixStack matrices, LivingEntity entity, int x, int y) {
        int healthChange;
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        if (state == null) {
            return;
        }
        healthChange = switch (ToroHealth.getConfig().hudOptions.healthChangeType) {
            case LAST -> state.healthChangeLast;
            case CUMULATIVE -> state.healthChangeCumulate;
            default -> 0;
        };
        int color = healthChange > 0 ? ToroHealth.getConfig().particleOptions.healColor : ToroHealth.getConfig().particleOptions.damageColor;
        if (healthChange != 0) {
            String text = Integer.toString(Math.abs(healthChange));
            this.client.textRenderer.drawWithShadow(matrices, text, x - this.client.textRenderer.getWidth(text), y, color);
        }
    }

    // draw a health Bar composed of 3 layers in InGameHud.
    private void renderHealthBar(MatrixStack matrices, LivingEntity entity, int x, int y, float tickDelta) {
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        if (state == null) {
            return;
        }
        EntityUtil.Relation relation = EntityUtil.getRelation(entity);

        int color = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColor : ToroHealth.getConfig().barColor.friendColor;
        int color2 = relation.equals(Relation.FOE) ? ToroHealth.getConfig().barColor.foeColorSecondary : ToroHealth.getConfig().barColor.friendColorSecondary;
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(MathHelper.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();
        int width = MathHelper.ceil(percent * 131.0f);
        int width2 = MathHelper.ceil(percent2 * 131.0f);
        this.renderBar(matrices, x, y, 130, DARK_GRAY);
        if (width2 > 0) {
            this.renderBar(matrices, x, y, width2, color2);
        }
        if (width > 0) {
            this.renderBar(matrices, x, y, width, color);
        }
    }

    // this method draws a single bar in InGameHud
    private void renderBar(MatrixStack matrices, int x, int y, int width, int color) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TOROHEALTH_BARS_TEXTURES);
        drawTexture(matrices, x, y, 0, 6 * 2 * 5 + 5, width, 5);
    }

    //modified from vanilla InventoryScreen.drawEntity
    public static void drawEntity(MatrixStack matrices, float x, float y, float size, float mouseX, float mouseY, LivingEntity entity, float tickDelta) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        float i = entity.bodyYaw;
        float j = entity.prevBodyYaw;
        float k = entity.headYaw;
        float l = entity.prevHeadYaw;
        entity.bodyYaw = 180.0f + f * 20.0f;
        entity.prevBodyYaw = 180.0f + f * 20.0f;
        entity.headYaw = 180.0f + f * 20.0f + k - i;
        entity.prevHeadYaw = 180.0f + f * 20.0f + l - j;
        drawEntity(matrices, x, y, size, quaternionf, quaternionf2, entity, tickDelta);
        entity.bodyYaw = i;
        entity.prevBodyYaw = j;
        entity.headYaw = k;
        entity.prevHeadYaw = l;
    }

    public static void drawEntity(MatrixStack matrices, float x, float y, float size, Quaternionf quaternionf, Quaternionf quaternionf2, LivingEntity entity, float tickDelta) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        matrixStack.translate(x ,y ,1000.0D);
        matrixStack.scale(1.0F ,1.0F ,-1.0F);
        RenderSystem.applyModelViewMatrix();

        MatrixStack matrices2 = new MatrixStack();
        matrices2.translate(0, 0, 950.0D);
        matrices2.multiplyPositionMatrix(new Matrix4f().scaling((float) size, (float) size, (float) size));
        matrices2.multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrices2, immediate, 0xF000F0));
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        DiffuseLighting.enableGuiDepthLighting();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }
}