package net.torocraft.torohealth;

import blue.endless.jankson.annotation.Nullable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.client.MinecraftClient;
import net.torocraft.torohealth.client.gui.ToroHealthHud;
import net.torocraft.torohealth.client.particle.HealthChangeParticle;
import net.torocraft.torohealth.client.util.HoldingWeaponUpdater;

public class ToroHealth implements ClientModInitializer {

  public static final String MODID = "torohealth";


  public static ModConfig CONFIG;
  public static ToroHealthHud toroHealthHud;
  public static boolean IS_HOLDING_WEAPON = false;
  private static LivingEntity targetedEntity;


  public static final DefaultParticleType HEALTH_CHANGE = FabricParticleTypes.simple();

  @Override
  public void onInitializeClient() {
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
      CONFIG = ModConfig.INSTANCE;

      toroHealthHud = new ToroHealthHud(MinecraftClient.getInstance());

      Registry.register(
          Registry.PARTICLE_TYPE,
          new Identifier(MODID, "health_change"),
          HEALTH_CHANGE
      );

      ClientTickEvents.END_CLIENT_TICK.register(client -> {
          HoldingWeaponUpdater.update();
          ToroHealth.toroHealthHud.tick();
      });

      ParticleFactoryRegistry.getInstance().register(
          ToroHealth.HEALTH_CHANGE,
          HealthChangeParticle.HealthChangeFactory::new
      );
  }


    public static @Nullable LivingEntity getTargetedEntity() {
        return targetedEntity;
    }

    public static void setTargetedEntity(@Nullable LivingEntity entity) {
        targetedEntity = entity;
    }
}
