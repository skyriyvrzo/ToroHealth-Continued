package net.torocraft.torohealth;

import java.util.Random;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.torocraft.torohealth.display.Hud;
import net.torocraft.torohealth.util.RayTrace;
import net.torocraft.torohealth.particle.HealthChangeParticle;

public class ToroHealth implements ClientModInitializer {

  public static final String MODID = "torohealth";


  public static ModConfig CONFIG;
  public static Hud HUD = new Hud();
  public static RayTrace RAYTRACE = new RayTrace();
  public static boolean IS_HOLDING_WEAPON = false;
  public static Random RAND = new Random();


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
      Registry.register(
          Registry.PARTICLE_TYPE,
          new Identifier(MODID, "health_change"),
          HEALTH_CHANGE
      );

      ParticleFactoryRegistry.getInstance().register(
          ToroHealth.HEALTH_CHANGE,
          HealthChangeParticle.HealthChangeFactory::new
      );
  }
}
