package net.kairost.torohealth;

import net.kairost.torohealth.config.ModConfig;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.kairost.torohealth.config.ModConfig;
import net.kairost.torohealth.client.gui.ToroHealthHud;
import net.kairost.torohealth.client.particle.HealthChangeParticle;
import net.kairost.torohealth.client.util.HoldingWeaponUpdater;

public class ToroHealth implements ClientModInitializer {
    public static final String MODID = "torohealth";
    public static final DefaultParticleType HEALTH_CHANGE = FabricParticleTypes.simple();
    private static ModConfig config;
    public static ToroHealthHud toroHealthHud = null;
    private static boolean holdingWeapon = false;
    private static LivingEntity targetedEntity;

    @Override
    public void onInitializeClient() {
        // set config
        ModConfig.init();

        ConfigHolder<ModConfig> holder =
            AutoConfig.getConfigHolder(ModConfig.class);

        holder.registerSaveListener((h, c) -> {
            c.postLoad();
            return ActionResult.SUCCESS;
        });

        holder.registerLoadListener((h, c) -> {
            c.postLoad();
            return ActionResult.SUCCESS;
        });
        config = ModConfig.INSTANCE;

        //toroHealth Particle
        Registry.register(
            Registries.PARTICLE_TYPE,
            new Identifier(MODID, "health_change"),
            HEALTH_CHANGE
        );

        ParticleFactoryRegistry.getInstance().register(
            ToroHealth.HEALTH_CHANGE,
            HealthChangeParticle.HealthChangeFactory::new
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            HoldingWeaponUpdater.update();
            ToroHealth.toroHealthHud.tick();
        });

        // toroHealthHud
        toroHealthHud = new ToroHealthHud(MinecraftClient.getInstance());
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static @Nullable LivingEntity getTargetedEntity() {
        return targetedEntity;
    }

    public static void setTargetedEntity(@Nullable LivingEntity entity) {
        targetedEntity = entity;
    }

    public static void setHoldingWeapon(boolean bl) {
        holdingWeapon = bl;
    }

    public static boolean isHoldingWeapon() {
        return holdingWeapon;
    }
}
