package net.kairost.torohealth.client.gui;

import net.minecraft.entity.passive.AbstractNautilusEntity;
import org.joml.Vector3f;
import org.joml.Quaternionf;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig.FrameStyle;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.client.util.EntityUtil;
import net.kairost.torohealth.client.util.EntityUtil.Relation;

public class ToroHealthHud {
    public static final Identifier CONTAINER = Identifier.of("minecraft", "hud/heart/container");
    public static final Identifier FULL = Identifier.of("minecraft", "hud/heart/full");
    private static final Identifier ARMOR_FULL = Identifier.of("minecraft", "hud/armor_full");
    private static final Identifier TOROHEALTH_BARS_TEXTURE = Identifier.of(ToroHealth.MODID, "textures/gui/bars.png");
    private static final Identifier TOROHEALTH_FRAME_TEXTURE = Identifier.of(ToroHealth.MODID, "textures/gui/frame.png");
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
    private boolean at_left;
    private boolean at_top;

    public ToroHealthHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!ToroHealth.getConfig().enabled || !ToroHealth.getConfig().hudOptions.showHUD) {
            return;
        }
        if (entity == null) {
            return;
        }
        if (entity.isRemoved()) {
            return;
        }
        if (ToroHealth.getConfig().hudOptions.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        float tickDelta = tickCounter.getTickProgress(false);
        context.getMatrices().pushMatrix();
        float x = determineX();
        float y = determineY();
        int scale = ToroHealth.getConfig().hudOptions.hudScale;
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        if (ToroHealth.getConfig().hudOptions.showEntity) {
            this.renderFrame(context);
            context.getMatrices().translate((this.at_left ? 1 : -1) * (FRAME_SIZE + 2), (this.at_top ? 1 : -1) * (INFO_Y_BASE + (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.HEAVY)? 2 : 0)));

        }

        // draw entity info
        this.renderInfo(context, tickDelta);
        context.getMatrices().popMatrix();

        if (ToroHealth.getConfig().hudOptions.showEntity) {
            drawEntity(context, (int) (x + scale * (this.entityX - 2 * FRAME_SIZE + (this.at_left ? 0 : -FRAME_SIZE))), (int) (y + scale * (this.entityY - 2 * FRAME_SIZE + (this.at_top ? 0 : -FRAME_SIZE))), (int) (x + scale * (this.entityX + 2 * FRAME_SIZE + (this.at_left ? 0 : -FRAME_SIZE))), (int) (y + scale * (this.entityY + 2 * FRAME_SIZE + (this.at_top ? 0 : -FRAME_SIZE))), this.entityScale * scale, (this.at_left ? -80 : 80), -20, entity, tickDelta);
        }
    }

    private float determineX() {
        float x = ToroHealth.getConfig().hudOptions.hudXPosition;
        float wScreen = this.client.getWindow().getScaledWidth();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
            case BOTTOM_LEFT, TOP_LEFT: {
                this.at_left = true;
                yield x;
            }
            case BOTTOM_RIGHT, TOP_RIGHT: {
                this.at_left = false;
                yield wScreen + x;
            }
        };
    }

    private float determineY() {
        float y = ToroHealth.getConfig().hudOptions.hudYPosition;
        float hScreen = client.getWindow().getScaledHeight();

        return switch (ToroHealth.getConfig().hudOptions.anchorPoint) {
            case TOP_LEFT, TOP_RIGHT: {
                this.at_top = true;
                yield y;
            }
            case BOTTOM_LEFT, BOTTOM_RIGHT: {
                this.at_top = false;
                yield hScreen + y;
            }
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
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * entityScale * 3 / 8 ;
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


    private void renderFrame(DrawContext context) {
        boolean light_style = (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.LIGHT));
        int h = 42;
        int w = light_style ? 42 : 179;
        int x = this.at_left ? 0 : -w;
        int y = this.at_top ? 0 : -h;
        int u = light_style ? 0 : 42;
        int v = (this.at_top ? 0 : 42) + (this.at_left ? 0 : 84);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TOROHEALTH_FRAME_TEXTURE, x, y, u, v, w, h, 256, 256);
    }


    private void renderInfo(DrawContext context, float tickDelta) {
        // render bar
        this.renderHealthBar(context, this.entity, (this.at_left ? 0 : -130), (this.at_top ? BAR_Y : -(BAR_Y + 5)), tickDelta);
        int x_pos_scalar = this.at_left ? 1 : -1;
        int y_pos_scalar = this.at_top ? 1 : -1;

        int xOffset = x_pos_scalar * INFO_X_BASE;

        // name
        String name = this.entity.getDisplayName().getString();
        context.drawTextWithShadow(this.client.textRenderer, name, xOffset + (this.at_left ? 0 : -this.client.textRenderer.getWidth(name)), (this.at_top ? 1 : -8), 0xFFFFFFFF);
        xOffset += x_pos_scalar * (this.client.textRenderer.getWidth(name) + INFO_SPACING);


        // health
        int healthMax = MathHelper.ceil(this.entity.getMaxHealth());
        int healthCurrent = MathHelper.clamp(
            MathHelper.ceil(this.entity.getHealth()),
            0,
            healthMax
        );
        String healthText = healthCurrent + "/" + healthMax;
        int healthTextY = this.at_top ? 1 : -8;
        if (this.at_left) {
            renderHeartIcon(context, xOffset, healthTextY - 1);

            xOffset += 10;

            context.drawTextWithShadow(this.client.textRenderer, healthText, xOffset, healthTextY, 0xFFFFFFFF);
            xOffset +=this.client.textRenderer.getWidth(healthText) + INFO_SPACING;
        } else {
            context.drawTextWithShadow(this.client.textRenderer, healthText, xOffset - this.client.textRenderer.getWidth(healthText), healthTextY, 0xFFFFFFFF);

            xOffset -= (this.client.textRenderer.getWidth(healthText) + 1);

            renderHeartIcon(context, xOffset - 9, (healthTextY - 1));
            xOffset -= (9 + INFO_SPACING);
        }

        // armor
        int armor = this.entity.getArmor();
        if (armor > 0) {
            String armorText = Integer.toString(armor);
            int armorTextY = this.at_top ? 1 : -8;
            if (this.at_left) {
                renderArmorIcon(context, xOffset, armorTextY - 1);
                xOffset += 10;
                context.drawTextWithShadow(this.client.textRenderer, armorText, xOffset, armorTextY, 0xFFFFFFFF);
            } else {
                context.drawTextWithShadow(this.client.textRenderer, armorText, xOffset - this.client.textRenderer.getWidth(armorText), armorTextY, 0xFFFFFFFF);
                xOffset -= (this.client.textRenderer.getWidth(Integer.toString(entity.getArmor())) + 1);
                renderArmorIcon(context, xOffset - 9, armorTextY - 1);
            }
        }

        // render health change
        this.renderHealthChangeText(context, entity, (this.at_left ? 1 : -1) * BAR_SIZE, y_pos_scalar * HEALTH_CHANGE_Y);
    }

    private void renderHeartIcon(DrawContext context, int x, int y) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CONTAINER, x, y, 9, 9);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, FULL, x, y, 9, 9);
    }

    private void renderArmorIcon(DrawContext context, int x, int y) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_FULL, x, y, 9, 9);
    }

    private void renderHealthChangeText(DrawContext context, LivingEntity entity, int x, int y) {
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
        int color = (healthChange > 0 ? ToroHealth.getConfig().particleOptions.healColor : ToroHealth.getConfig().particleOptions.damageColor) | 0xFF000000;
        if (healthChange != 0) {
            String text = Integer.toString(Math.abs(healthChange));
            context.drawTextWithShadow(this.client.textRenderer, text, x - (this.at_left ? 1 : -1) * this.client.textRenderer.getWidth(text), (this.at_top ? y : (y - 7)), color);
        }
    }

    // draw a health Bar composed of 3 layers in InGameHud.
    private void renderHealthBar(DrawContext context, LivingEntity entity, int x, int y, float tickDelta) {
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
        this.renderBar(context, x, y, 130, DARK_GRAY);
        if (width2 > 0) {
            this.renderBar(context, x, y, width2, color2);
        }
        if (width > 0) {
            this.renderBar(context, x, y, width, color);
        }
    }

    // this method draws a single bar in InGameHud
    private void renderBar(DrawContext context, int x, int y, int width, int color) {
        int color_argb = color | 0xFF000000;
        int shift = this.at_left ? 0 : (130 - width);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TOROHEALTH_BARS_TEXTURE, x + shift, y, shift, 6 * 2 * 5 + 5, width, 5, 256, 256, color_argb);
    }

    //modified from vanilla InventoryScreen.drawEntity
    public static void drawEntity(DrawContext context, int x1, int y1, int x2, int y2, float size, float mouseX, float mouseY, LivingEntity entity, float tickDelta) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        EntityRenderState entityRenderState = drawEntity(entity, tickDelta);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyYaw = 180.0F + f * 20.0F;
            if (entity instanceof AbstractNautilusEntity) {
                livingEntityRenderState.bodyYaw += 180.0F;
            }
            livingEntityRenderState.width = livingEntityRenderState.width / livingEntityRenderState.baseScale;
            livingEntityRenderState.height = livingEntityRenderState.height / livingEntityRenderState.baseScale;
            livingEntityRenderState.baseScale = 1.0F;
        }
        Vector3f vector3f = new Vector3f(0.0F, 0.0F, 0.0F);
        context.addEntity(entityRenderState, size, vector3f, quaternionf, quaternionf2, x1, y1, x2, y2);
    }

    //copied from InventoryScreen.drawEntity
    private static EntityRenderState drawEntity(LivingEntity entity, float tickDelta) {
        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderManager.getRenderer(entity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(entity, tickDelta);
        entityRenderState.light = 0xF000F0;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }
}
