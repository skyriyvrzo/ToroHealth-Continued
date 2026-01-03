package net.kairost.torohealth.client.gui;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
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
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
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
        if (this.client.options.hudHidden) {
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

        float tickDelta = tickCounter.getTickDelta(false);
        context.getMatrices().push();
        float x = determineX();
        float y = determineY();
        context.getMatrices().translate(x, y, 0);
        int scale = ToroHealth.getConfig().hudOptions.hudScale;
        context.getMatrices().scale(scale, scale, scale);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (ToroHealth.getConfig().hudOptions.showEntity) {
            this.renderFrame(context);
            context.getMatrices().push();
            context.getMatrices().translate((this.at_left ? 1 : -1) * (FRAME_SIZE + 2), (this.at_top ? 1 : -1) * (INFO_Y_BASE + (ToroHealth.getConfig().hudOptions.frameStyle.equals(FrameStyle.HEAVY)? 2 : 0)), 0);
        }

        // draw entity info
        this.renderInfo(context, tickDelta);

        // render entity
        if (ToroHealth.getConfig().hudOptions.showEntity) {
            context.getMatrices().pop();
            drawEntity(context,  this.entityX + (this.at_left ? 0 : -FRAME_SIZE),  this.entityY + (this.at_top ? 0 : -FRAME_SIZE), this.entityScale, (this.at_left ? 1 : -1) * -80, -20, entity, tickDelta);
        }
        context.getMatrices().pop();
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
            this.entityY = (float) FRAME_SIZE / 2 + entity.getHeight() * entityScale / 4 ;
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
        RenderSystem.enableBlend();
        context.drawTexture(RenderLayer::getGuiTextured, TOROHEALTH_FRAME_TEXTURE, x, y, u, v, w, h, 256, 256);
        RenderSystem.disableBlend();
    }


    private void renderInfo(DrawContext context, float tickDelta) {
        // render bar
        this.renderHealthBar(context, this.entity, (this.at_left ? 0 : -BAR_SIZE), (this.at_top ? BAR_Y : -(BAR_Y + 5)), tickDelta);
        int x_pos_scalar = this.at_left ? 1 : -1;
        int y_pos_scalar = this.at_top ? 1 : -1;

        int xOffset = x_pos_scalar * INFO_X_BASE;

        // name
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
        String name = this.entity.getDisplayName().getString();
        context.drawTextWithShadow(this.client.textRenderer, name, xOffset + (this.at_left ? 0 : -this.client.textRenderer.getWidth(name)), (this.at_top ? 1 : -8), 0xFFFFFF);
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

            context.drawTextWithShadow(this.client.textRenderer, healthText, xOffset, healthTextY, 0xffffff);
            xOffset +=this.client.textRenderer.getWidth(healthText) + INFO_SPACING;
        } else {
            context.drawTextWithShadow(this.client.textRenderer, healthText, xOffset - this.client.textRenderer.getWidth(healthText), healthTextY, 0xffffff);

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
                context.drawTextWithShadow(this.client.textRenderer, armorText, xOffset, armorTextY, 0xffffff);
            } else {
                context.drawTextWithShadow(this.client.textRenderer, armorText, xOffset - this.client.textRenderer.getWidth(armorText), armorTextY, 0xffffff);
                xOffset -= (this.client.textRenderer.getWidth(Integer.toString(entity.getArmor())) + 1);
                renderArmorIcon(context, xOffset - 9, armorTextY - 1);
            }
        }

        // render health change
        this.renderHealthChangeText(context, entity, (this.at_left ? 1 : -1) * BAR_SIZE, y_pos_scalar * HEALTH_CHANGE_Y);
    }

    private void renderHeartIcon(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(RenderLayer::getGuiTextured, CONTAINER, x, y, 9, 9);
        context.drawGuiTexture(RenderLayer::getGuiTextured, FULL, x, y, 9, 9);
        RenderSystem.disableBlend();
    }

    private void renderArmorIcon(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(RenderLayer::getGuiTextured, ARMOR_FULL, x, y, 9, 9);
        RenderSystem.disableBlend();
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
        int color = healthChange > 0 ? ToroHealth.getConfig().particleOptions.healColor : ToroHealth.getConfig().particleOptions.damageColor;
        if (healthChange != 0) {
            String text = Integer.toString(Math.abs(healthChange));
            context.drawTextWithShadow(this.client.textRenderer, text, x - (this.at_left ? 1 : -1) * this.client.textRenderer.getWidth(text), (this.at_top ? y : (y - this.client.textRenderer.getWrappedLinesHeight(text, 10000))), color);
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
        int width = MathHelper.ceil(percent * (BAR_SIZE + 1));
        int width2 = MathHelper.ceil(percent2 * (BAR_SIZE + 1));
        if (BAR_SIZE > width && BAR_SIZE > width2) {
            this.renderBar(context, x, y, BAR_SIZE, DARK_GRAY);
        }
        if (width2 > width) {
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
        RenderSystem.enableBlend();
        context.drawTexture(RenderLayer::getGuiTextured, TOROHEALTH_BARS_TEXTURE, x + shift, y, shift, 6 * 2 * 5 + 5, width, 5, 256, 256, color_argb);
        RenderSystem.disableBlend();
    }

    //modified from vanilla InventoryScreen.drawEntity
    public static void drawEntity(DrawContext context, float x, float y, float size, float mouseX, float mouseY, LivingEntity entity, float tickDelta) {
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
        if (entity instanceof EnderDragonEntity dragon) {
            EndCrystalEntity endCrystal = dragon.connectedCrystal;
            dragon.connectedCrystal = null;
            drawEntity(context, x, y, size, new Vector3f(0.0F, 0.0F, 0.0F), quaternionf, quaternionf2, entity, tickDelta);
            dragon.connectedCrystal = endCrystal;
        } else {
            drawEntity(context, x, y, size, new Vector3f(0.0F, 0.0F, 0.0F), quaternionf, quaternionf2, entity, tickDelta);
        }
        entity.bodyYaw = i;
        entity.prevBodyYaw = j;
        entity.headYaw = k;
        entity.prevHeadYaw = l;
    }

    //copied from InventoryScreen.drawEntity
    public static void drawEntity(DrawContext context, float x, float y, float size, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity, float tickDelta) {
        context.getMatrices().push();
        context.getMatrices().translate((double)x, (double)y, 50.0);
        context.getMatrices().scale(size, size, -size);
        context.getMatrices().translate(vector3f.x, vector3f.y, vector3f.z);
        context.getMatrices().multiply(quaternionf);
        context.draw();
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            entityRenderDispatcher.setRotation(quaternionf2.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        }

        entityRenderDispatcher.setRenderShadows(false);
        context.draw(vertexConsumers -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, tickDelta, context.getMatrices(), vertexConsumers, 0xF000F0));
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
}
