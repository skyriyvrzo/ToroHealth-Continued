package net.torocraft.torohealth.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.ModConfig.FrameStype;
import net.torocraft.torohealth.data.BarStateAccessor;
import net.torocraft.torohealth.data.BarState;
import net.torocraft.torohealth.client.util.EntityUtil;

public class ToroHealthHud extends DrawableHelper {
    private static final Identifier ICON_TEXTURES = new Identifier("textures/gui/icons.png");
    private static final Identifier TOROHEALTH_BARS_TEXTURES = new Identifier(ToroHealth.MODID + ":textures/gui/bars.png");
    private static final Identifier TOROHEALTH_FRAME_TEXTURE =
      new Identifier(ToroHealth.MODID + ":textures/gui/frame.png");
    private LivingEntity entity;
    private final MinecraftClient client;
    private int age;
    private static final int DARK_GRAY = 0x808080;
    private final static int FRAME_SIZE = 42;
    private static final float RENDER_HEIGHT = 32f;
    private static final float RENDER_WIDTH = 24f;
    private float entityX;
    private float entityY;
    private float entityScale;

    public ToroHealthHud(MinecraftClient client) {
        this.client = client;
    }

    public void draw(MatrixStack matrix, float tickDelta) {
        if (entity == null) {
            return;
        }
        if (entity.isRemoved()) {
            return;
        }
        if (ToroHealth.CONFIG.hudOptions.onlyWhenHurt && entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        matrix.push();
        float x = determineX();
        float y = determineY();
        matrix.translate(x, y, 0);
        int scale = ToroHealth.CONFIG.hudOptions.hudScale;
        matrix.scale(scale, scale, scale);
        if (ToroHealth.CONFIG.hudOptions.showEntity) {
            this.drawSkin(matrix);
            this.drawEntity(matrix,  this.entityX,  this.entityY, this.entityScale, -80, -20, entity, tickDelta);
            matrix.translate(FRAME_SIZE + 2, 0, 0);
        }

        // draw entity info
        this.renderInfo(matrix, entity, tickDelta);
        matrix.pop();
    }

  private float determineX() {
    float x = ToroHealth.CONFIG.hudOptions.hudXPosition;
    float wScreen = this.client.getWindow().getScaledWidth();

      return switch (ToroHealth.CONFIG.hudOptions.anchorPoint) {
          case BOTTOM_CENTER, TOP_CENTER -> (wScreen / 2) + x;
          case BOTTOM_RIGHT, TOP_RIGHT -> (wScreen) + x;
          default -> x;
      };
  }

  private float determineY() {
    float y = ToroHealth.CONFIG.hudOptions.hudYPosition;
    float hScreen = client.getWindow().getScaledHeight();

    switch (ToroHealth.CONFIG.hudOptions.anchorPoint) {
      case BOTTOM_CENTER:
      case BOTTOM_LEFT:
      case BOTTOM_RIGHT:
        return y + hScreen;
      default:
        return y;
    }
  }


    public void tick()  {
        age++;
    }

    public void setEntity(LivingEntity entity) {
        if (entity != null) {
            this.age = 0;
        }

        if (entity == null && age > ToroHealth.CONFIG.hudOptions.hudHideDelay) {
            setEntityWork(null);
        }

        if (entity != null && entity != this.entity) {
            setEntityWork(entity);
        }
    }

    private  void  setEntityWork(LivingEntity  entity)  {
        this.entity  =  entity;
        if  (entity  !=  null)  {
            this.entityX = (float) FRAME_SIZE / 2;
            this.entityY = (float) FRAME_SIZE / 2 + RENDER_HEIGHT / 2;
            if (entity instanceof GhastEntity) {
                this.entityY -= 10;
            }

            this.entityScale = Math.min(RENDER_HEIGHT / entity.getHeight(), RENDER_WIDTH / entity.getWidth());
            if (entity instanceof MobEntity mob && mob.isBaby()) {
                this.entityScale *= 0.7;
            }
            this.entityScale = Math.min(this.entityScale, 32f);
        }
    }


  private void drawSkin(MatrixStack matrix) {
    RenderSystem.setShaderTexture(0, TOROHEALTH_FRAME_TEXTURE);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    int w = 179, h = 42;
    this.drawTexture(matrix, 0, 0, 0, (ToroHealth.CONFIG.hudOptions.frameStyle.equals(FrameStype.LIGHT) ? 42 : 0), w, h);
  }



    // this method draws a single bar in InGameHud
    private void renderBar(MatrixStack matrices, int x, int y, int color, int width) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TOROHEALTH_BARS_TEXTURES);
        this.drawTexture(matrices, x, y, 0, 6 * 2 * 5 + 5, width, 5);
    }


    private void renderInfo(MatrixStack matrix, LivingEntity entity, float tickDelta) {
        // render bar
        this.renderHealthBar(matrix, entity, 0, 14, tickDelta);
        // render text
        int xOffset = 2;

        // name
        String name = entity.getDisplayName().getString();
        this.client.textRenderer.drawWithShadow(matrix, name, (float)xOffset, 4f, 0xFFFFFF);
        xOffset += this.client.textRenderer.getWidth(name) + 5;

        this.renderHeartIcon(matrix, xOffset, (int) 3);
        xOffset += 10;

        // health
        int healthMax = MathHelper.ceil(entity.getMaxHealth());
        int healthCurrent = MathHelper.clamp(
            MathHelper.ceil(entity.getHealth()),
            0,
            healthMax
        );
        String healthText = healthCurrent + "/" + healthMax;
        this.client.textRenderer.drawWithShadow(matrix, healthText, (float)xOffset, (float)4f, 0xFFFFFF);
        xOffset += this.client.textRenderer.getWidth(healthText) + 5;
        // render health change


        // armor
        int armor = entity.getArmor();
        if (armor > 0) {
            renderArmorIcon(matrix, xOffset, (int) 3);
            xOffset += 10;
            this.client.textRenderer.drawWithShadow(matrix, Integer.toString(entity.getArmor()), xOffset, 4, 0xe0e0e0);
        }

        this.renderHealthChange(matrix, entity, 130, 21);
        // render health change
    }

    private void renderHeartIcon(MatrixStack matrix, int x, int y) {
        RenderSystem.setShaderTexture(0, ICON_TEXTURES);
        this.drawTexture(matrix, x, y, 16, 0, 9, 9);
        this.drawTexture(matrix, x, y, 16 + 36, 0, 9, 9);
    }

    private void renderArmorIcon(MatrixStack matrix, int x, int y) {
        RenderSystem.setShaderTexture(0, ICON_TEXTURES);
        this.drawTexture(matrix, x, y, 34, 9, 9, 9);
    }

    private void renderHealthChange(MatrixStack matrices, LivingEntity entity, int x, int y) {
        int healthChange;
        BarState state = ((BarStateAccessor) entity).torohealth$getBarState();
        if (state == null) {
            return;
        }
        switch (ToroHealth.CONFIG.hudOptions.healthChangeType) {
            case LAST:
                healthChange = -state.lastDmg;
                break;
            case CUMULATIVE:
                healthChange = -state.lastDmgCumulative;
                break;
            default:
                healthChange = 0;
        }
        int color = healthChange > 0 ? ToroHealth.CONFIG.particleOptions.healColor : ToroHealth.CONFIG.particleOptions.damageColor;
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

        int color = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barColor.friendColor : ToroHealth.CONFIG.barColor.foeColor;
        int color2 = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barColor.friendColorSecondary : ToroHealth.CONFIG.barColor.foeColorSecondary;
        float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
        float percent2 = Math.min(MathHelper.lerp(tickDelta, state.lastHealthDisplay, state.healthDisplay), entity.getMaxHealth()) / entity.getMaxHealth();
        int width = MathHelper.ceil(percent * 131.0f);
        int width2 = MathHelper.ceil(percent2 * 131.0f);
        this.renderBar(matrices, x, y, DARK_GRAY, 130);
        if (width2 > 0) {
            this.renderBar(matrices, x, y, color2, width2);
        }
        if (width > 0) {
            this.renderBar(matrices, x, y, color, width);
        }
    }


    public static void drawEntity(MatrixStack matrices, float x, float y, float size, float mouseX, float mouseY, LivingEntity entity, float tickDelta) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        matrixStack.translate(x ,y ,1050.0D);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.push();
        matrixStack2.translate(0.0D, 0.0D, 1000.0D);
        matrixStack2.scale((float) size, (float) size, (float) size);
        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(g * 20.0F);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack2.multiply(quaternion);
        float i = entity.bodyYaw;
        float j = entity.prevBodyYaw;
        float k = entity.headYaw;
        float l = entity.prevHeadYaw;
        entity.bodyYaw = 180.0f + f * 20.0f;
        entity.prevBodyYaw = 180.0f + f * 20.0f;
        entity.headYaw = 180.0f + f * 20.0f + k - i;
        entity.prevHeadYaw = 180.0f + f * 20.0f + l - j;
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher =
            MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate =
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrixStack2, immediate, 0xF000F0));
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = i;
        entity.prevBodyYaw = j;
        entity.headYaw = k;
        entity.prevHeadYaw = l;
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }
}