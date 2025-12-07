package net.torocraft.torohealth.display;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.bars.BarState;
import net.torocraft.torohealth.bars.BarStates;
import net.torocraft.torohealth.util.EntityUtil;

public class BarDisplay {

  private static final Identifier ICON_TEXTURES = new Identifier("textures/gui/icons.png");
  private static final Identifier TOROHEALTH_BARS_TEXTURES = new Identifier(ToroHealth.MODID + ":textures/gui/bars.png");
    private static final int DARK_GRAY = 0x808080;
  private final MinecraftClient mc;
  private final DrawableHelper gui;

  public BarDisplay(MinecraftClient mc, DrawableHelper gui) {
    this.mc = mc;
    this.gui = gui;
  }

  private String getEntityName(LivingEntity entity) {
    return entity.getDisplayName().getString();
  }

  public void draw(MatrixStack matrix, LivingEntity entity) {
    int xOffset = 42 + 2;

    this.renderHealthBar(matrix, entity, xOffset, 14);
    xOffset += 2;
    String name = getEntityName(entity);
    int healthMax = MathHelper.ceil(entity.getMaxHealth());
    int healthCur = Math.min(MathHelper.ceil(entity.getHealth()), healthMax);
    String healthText = healthCur + "/" + healthMax;
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    mc.textRenderer.drawWithShadow(matrix, name, (float)xOffset, 2, 0xFFFFFF);
    xOffset += mc.textRenderer.getWidth(name) + 5;

    renderHeartIcon(matrix, xOffset, (int) 1);
    xOffset += 10;

    mc.textRenderer.drawWithShadow(matrix, healthText, xOffset, 2, 0xe0e0e0);
    xOffset += mc.textRenderer.getWidth(healthText) + 5;

    int armor = entity.getArmor();

    if (armor > 0) {
      renderArmorIcon(matrix, xOffset, (int) 1);
      xOffset += 10;
      mc.textRenderer.drawWithShadow(matrix, entity.getArmor() + "", xOffset, 2, 0xe0e0e0);
    }

    this.drawHealthChange(matrix, entity);
  }

  private void renderArmorIcon(MatrixStack matrix, int x, int y) {
    RenderSystem.setShaderTexture(0, ICON_TEXTURES);
    gui.drawTexture(matrix, x, y, 34, 9, 9, 9);
  }

  private void renderHeartIcon(MatrixStack matrix, int x, int y) {
    RenderSystem.setShaderTexture(0, ICON_TEXTURES);
    gui.drawTexture(matrix, x, y, 16, 0, 9, 9);
    gui.drawTexture(matrix, x, y, 16 + 36, 0, 9, 9);
  }

  // draw a health Bar composed of 3 layers in InGameHud.
  private void renderHealthBar(MatrixStack matrices, LivingEntity entity, int x, int y) {
      BarState state = BarStates.getState(entity);
      if (state == null) {
          return;
      }
      EntityUtil.Relation relation = EntityUtil.determineRelation(entity);

      int color = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barOptions.friendColor
          : ToroHealth.CONFIG.barOptions.foeColor;
      int color2 = relation.equals(EntityUtil.Relation.FRIEND) ? ToroHealth.CONFIG.barOptions.friendColorSecondary
          : ToroHealth.CONFIG.barOptions.foeColorSecondary;
      float percent = Math.min(state.health, entity.getMaxHealth()) / entity.getMaxHealth();
      float percent2 = Math.min(state.previousHealthDisplay, entity.getMaxHealth()) / entity.getMaxHealth();
      int width = (int)(percent * 131.0f);
      int width2 = (int)(percent2 * 131.0f);
      this.renderBar(matrices, x, y, DARK_GRAY, 130);
      if (width2 > 0) {
          this.renderBar(matrices, x, y, color2, width2);
      }
      if (width > 0) {
          this.renderBar(matrices, x, y, color, width);
      }
  }


  // this method draws a single bar in InGameHud
  private void renderBar(MatrixStack matrices, int x, int y, int color, int width) {
      float r = (color >> 16 & 255) / 255.0F;
      float g = (color >> 8 & 255) / 255.0F;
      float b = (color & 255) / 255.0F;
      RenderSystem.setShaderColor(r, g, b, 1);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, TOROHEALTH_BARS_TEXTURES);
      gui.drawTexture(matrices, x, y, 0, 6 * 2 * 5 + 5, width, 5);
  }

  private void drawHealthChange(MatrixStack matrices, LivingEntity entity) {
      final int X_RIGHTMOST = 42 + 2 + 130;
      final int Y_UPMOST = 19 + 2;
      int healthChange;
      BarState state = BarStates.getState(entity);
      if (state == null) {
          return;
      }
      switch (ToroHealth.CONFIG.barOptions.healthChangeType) {
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
          String s = Integer.toString(Math.abs(healthChange));
          int sw = mc.textRenderer.getWidth(s);
          mc.textRenderer.drawWithShadow(matrices, s, X_RIGHTMOST - sw, Y_UPMOST, color);
      }
  }
}