package net.kairost.torohealth.data;

import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;

public class BarState {
    private static final float HEALTH_DISPLAY_DELAY = 5;
    private static final float HEALTH_CUMULATE_DELAY = 20;

    public final Integer entityID;
    public float health;
    public float lastHealth;
    public float healthDisplay;
    public float lastHealthDisplay;
    public int healthChangeLast;
    public int healthChangeCumulate;
    public float healthCumulateDelay;
    public float healthDisplayDelay;
    private float animationSpeed;


    private BarState(Integer id, float health){
        this.entityID = id;
        this.health = health;
        this.healthDisplay = health;
        this.lastHealthDisplay = health;
        this.healthChangeLast = 0;
        this.healthChangeCumulate = 0;
        this.lastHealth = health;
        this.healthCumulateDelay = 0;
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

             if (this.healthCumulateDelay == 0.0F) {
                reset();
            }
            updateAnimations();
        }
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
        this.healthChangeLast = MathHelper.ceil(this.health) - MathHelper.ceil(this.lastHealth);
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
