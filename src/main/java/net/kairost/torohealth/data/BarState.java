package net.kairost.torohealth.data;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BarState {
    private static final float HEALTH_DISPLAY_DELAY = 5;
    private static final float HEALTH_CUMULATE_DELAY = 20;

    public float health;
    public float lastHealth;
    public float healthDisplay;
    public float lastHealthDisplay;
    public int healthChangeLast;
    public int healthChangeCumulate;
    public float healthCumulateDelay;
    public float healthDisplayDelay;
    private float animationSpeed;


    private BarState(float health){
        this.health = health;
        this.healthDisplay = health;
        this.lastHealthDisplay = health;
        this.healthChangeLast = 0;
        this.healthChangeCumulate = 0;
        this.lastHealth = health;
        this.healthCumulateDelay = 0;
        this.animationSpeed = 0;
    }

    public static BarState create(LivingEntity entity){
        float currentHealth = Math.min(entity.getHealth(), entity.getMaxHealth());
        return new BarState(currentHealth);
    }

    public void tick() {
        incrementTimers();

         if (this.healthCumulateDelay <= 0) {
            reset();
        }
        updateAnimations();
    }

    private void reset() {
        healthChangeLast = 0;
        healthChangeCumulate = 0;
    }

    private void incrementTimers() {
        if (this.healthCumulateDelay > 0) {
            this.healthCumulateDelay--;
        }
        if (this.healthDisplayDelay > 0) {
            this.healthDisplayDelay--;
        }
    }

    public void handleHealthChange() {
        this.healthChangeLast = Mth.ceil(this.health) - Mth.ceil(this.lastHealth);
        this.healthChangeCumulate += this.healthChangeLast;
        this.healthCumulateDelay = HEALTH_CUMULATE_DELAY;
        this.healthDisplay = Math.max(Math.max(this.lastHealth, this.healthDisplay), this.health);
        if (this.healthDisplay <= this.lastHealth) {
            this.healthDisplayDelay = HEALTH_DISPLAY_DELAY;
        }
    	this.updateAnimationSpeed();
    }

    private void updateAnimationSpeed() {
        this.animationSpeed = (this.healthDisplay - this.health) / 10f;
    }

    private void updateAnimations() {
        lastHealthDisplay = healthDisplay;
        if (healthDisplayDelay <= 0) {
            healthDisplay = Math.max(healthDisplay - animationSpeed, health);
        }
    }

    public void updateHealth(float health) {
        this.lastHealth = this.health;
        this.health = health;
    }
}
