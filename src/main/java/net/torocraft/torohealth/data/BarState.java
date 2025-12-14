package net.torocraft.torohealth.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.MinecraftClient;

public class BarState {
    public final Integer entityID;

    public float health;
    public float healthDisplay;
    public float previousHealthDelay;
    public int lastDmg;
    public int lastDmgCumulative;
    public float lastHealth;
    public float lastHealthDisplay;
    public float lastDmgDelay;
    private float animationSpeed;

    private static final float HEALTH_INDICATOR_DELAY = 10;


    private BarState(Integer id, float health){
        this.entityID = id;
        this.health = health;
        this.healthDisplay = health;
        this.lastHealthDisplay = health;
        this.lastDmg = 0;
        this.lastDmgCumulative = 0;
        this.lastHealth = health;
        this.lastDmgDelay = 0;
        this.animationSpeed = 0;
    }

    public static BarState create(Integer id){
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        Entity entity = client.world.getEntityById(id);
        if (entity instanceof LivingEntity living) {
            float currentHealth = Math.min(living.getHealth(), living.getMaxHealth());
            return new BarState(id, currentHealth);
        }
        return  null;
    }


    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        LivingEntity entity = (LivingEntity) client.world.getEntityById(entityID);

        if (entity != null){
            health = Math.min(entity.getHealth(), entity.getMaxHealth());
            incrementTimers();

             if (this.lastDmgDelay == 0.0F) {
                reset();
            }

            updateAnimations();
        }
    }

    private void reset() {
        lastDmg = 0;
        lastDmgCumulative = 0;
    }

    private void incrementTimers() {
        if (this.lastDmgDelay > 0) {
            this.lastDmgDelay--;
        }
        if (this.previousHealthDelay > 0) {
            this.previousHealthDelay--;
        }
    }

    public void handleHealthChange() {
        this.lastDmg = MathHelper.ceil(this.lastHealth) - MathHelper.ceil(this.health);
        this.lastDmgCumulative += this.lastDmg;
        this.lastDmgDelay = HEALTH_INDICATOR_DELAY * 2;
        this.healthDisplay = Math.max(Math.max(this.lastHealth, this.healthDisplay), this.health);
        if (this.healthDisplay <= this.lastHealth) {
            this.previousHealthDelay = HEALTH_INDICATOR_DELAY;
        }
    	this.updateAnimationSpeed();
    }

  private void updateAnimationSpeed() {
      this.animationSpeed = (this.healthDisplay - this.health) / 10f;
  }

  private void updateAnimations() {
      lastHealthDisplay = healthDisplay;
      if (previousHealthDelay <= 0) {
          healthDisplay = Math.max(healthDisplay - animationSpeed, health);
      }
  }


  public void updateHealth(float health) {
        this.lastHealth = this.health;
        this.health = health;
  }
}
