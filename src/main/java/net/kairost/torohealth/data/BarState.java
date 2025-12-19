package net.kairost.torohealth.data;

import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;

public class BarState {
    private static final float HEALTH_INDICATOR_DELAY = 10;

    public final Integer entityID;
    public float health;
    public float lastHealth;
    public float healthDisplay;
    public float lastHealthDisplay;
    public int lastHealthChange;
    public int lastHealthChangeCumulative;
    public float healthChangeDelay;
    public float healthDisplayDelay;
    private float animationSpeed;


    private BarState(Integer id, float health){
        this.entityID = id;
        this.health = health;
        this.healthDisplay = health;
        this.lastHealthDisplay = health;
        this.lastHealthChange = 0;
        this.lastHealthChangeCumulative = 0;
        this.lastHealth = health;
        this.healthChangeDelay = 0;
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

             if (this.healthChangeDelay == 0.0F) {
                reset();
            }
            updateAnimations();
        }
    }

    private void reset() {
        lastHealthChange = 0;
        lastHealthChangeCumulative = 0;
    }

    private void incrementTimers() {
        if (this.healthChangeDelay > 0) {
            this.healthChangeDelay--;
        }
        if (this.healthDisplayDelay > 0) {
            this.healthDisplayDelay--;
        }
    }

    public void handleHealthChange() {
        this.lastHealthChange = MathHelper.ceil(this.health) - MathHelper.ceil(this.lastHealth);
        this.lastHealthChangeCumulative += this.lastHealthChange;
        this.healthChangeDelay = HEALTH_INDICATOR_DELAY * 2;
        this.healthDisplay = Math.max(Math.max(this.lastHealth, this.healthDisplay), this.health);
        if (this.healthDisplay <= this.lastHealth) {
            this.healthDisplayDelay = HEALTH_INDICATOR_DELAY;
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
