package net.kairost.torohealth;

import com.mojang.logging.LogUtils;
import net.kairost.torohealth.client.particle.HealthChangeParticle;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import org.slf4j.Logger;
import net.kairost.torohealth.config.ToroHealthConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.kairost.torohealth.client.gui.ToroHealthHud;
import net.kairost.torohealth.client.util.HoldingWeaponUpdater;

@Mod(ToroHealth.MODID)
public class ToroHealth {
    public static final String MODID = "torohealth";
    public static ToroHealthHud toroHealthHud = null;
    private static boolean holdingWeapon = false;
    private static LivingEntity targetedEntity;
    public static final Logger LOGGER = LogUtils.getLogger();



    public ToroHealth(IEventBus modBus) {

        modBus.addListener(ToroHealthConfig::onConfigLoad);
        modBus.addListener(ToroHealthConfig::onConfigReload);

        ModLoadingContext.get().registerConfig(
            ModConfig.Type.CLIENT,
            ToroHealthConfig.SPEC
        );

        modBus.addListener(this::onClientSetup);


        ToroHealthParticles.register(modBus);
        modBus.addListener(this::onRegisterParticleFactories);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.register(ToroHealthHudEvents.class);
    }

    private void onRegisterParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
            ToroHealthParticles.HEALTH_CHANGE.get(),
            HealthChangeParticle.HealthChangeFactory::new
        );
    }

    public final class ToroHealthHudEvents {
        @SubscribeEvent
        public static void test(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay().id() == VanillaGuiOverlay.CROSSHAIR.id()) {
                if (!ToroHealthConfig.CONFIG.enabled.get()) return;
                if (!ToroHealthConfig.CONFIG.hudOptions.showHUD.get()) return;
                if (ToroHealth.toroHealthHud == null) return;

                ToroHealth.toroHealthHud.render(
                    event.getGuiGraphics(),
                    event.getPartialTick()
                );
            }
        }
    }





    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        HoldingWeaponUpdater.update();

        if (toroHealthHud != null) {
            toroHealthHud.tick();
        }
    }


    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            toroHealthHud = new ToroHealthHud(Minecraft.getInstance());
        });
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
