package net.torocraft.torohealth;

import java.util.Random;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.torocraft.torohealth.config.Config;
import net.torocraft.torohealth.config.loader.ConfigLoader;
import net.torocraft.torohealth.display.Hud;
import net.torocraft.torohealth.util.RayTrace;
import net.torocraft.torohealth.particle.HealthChangeParticle;
import net.fabricmc.api.ClientModInitializer;

public class ToroHealth implements ClientModInitializer {

  public static final String MODID = "torohealth";


  public static Config CONFIG = new Config();
  public static Hud HUD = new Hud();
  public static RayTrace RAYTRACE = new RayTrace();
  public static boolean IS_HOLDING_WEAPON = false;
  public static Random RAND = new Random();

  private static ConfigLoader<Config> CONFIG_LOADER = new ConfigLoader<>(new Config(),
      ToroHealth.MODID + ".json", config -> ToroHealth.CONFIG = config);

  public static final DefaultParticleType HEALTH_CHANGE = FabricParticleTypes.simple();

  @Override
  public void onInitializeClient() {
      CONFIG_LOADER.load();
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
