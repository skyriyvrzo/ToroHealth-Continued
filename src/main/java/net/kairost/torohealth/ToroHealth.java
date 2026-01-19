package net.kairost.torohealth;

import org.slf4j.Logger;
import org.jetbrains.annotations.Nullable;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.kairost.torohealth.config.ToroHealthConfig;
import net.kairost.torohealth.client.gui.ToroHealthHud;
import net.kairost.torohealth.client.util.HoldingWeaponUpdater;
import net.kairost.torohealth.client.particle.TextRenderEntry;
import net.kairost.torohealth.client.particle.TextRenderQueue;
import net.kairost.torohealth.client.particle.HealthChangeParticle;
import net.kairost.torohealth.client.particle.TextParticleRenderer;

@Mod(ToroHealth.MODID)
public class ToroHealth {
    public static final String MODID = "torohealth";
    private static TextParticleRenderer textParticleRenderer;
    public static ToroHealthHud toroHealthHud = null;
    private static boolean holdingWeapon = false;
    private static LivingEntity targetedEntity;
    public static final Logger LOGGER = LogUtils.getLogger();



    public ToroHealth(IEventBus modBus, ModContainer container) {

        modBus.addListener(ToroHealthConfig::onConfigLoad);
        modBus.addListener(ToroHealthConfig::onConfigReload);

        container.registerConfig(
            ModConfig.Type.CLIENT,
            ToroHealthConfig.SPEC
        );

        modBus.addListener(ClientSetup::onClientSetup);
        modBus.addListener(ClientSetup::onRegisterParticleFactories);

        NeoForge.EVENT_BUS.register(ClientEvents.class);


        ToroHealthParticles.register(modBus);

        NeoForge.EVENT_BUS.register(HudRenderEvents.class);
        NeoForge.EVENT_BUS.register(WorldRenderEvents.class);
    }

    public static final class HudRenderEvents {
        @SubscribeEvent
        public static void onHudRender(RenderGuiEvent.Post event) {
            if (!ToroHealthConfig.CONFIG.enabled.get() || !ToroHealthConfig.CONFIG.hudOptions.showHUD.get()) {
                return;
            }

            ToroHealthHud hud = ToroHealth.toroHealthHud;
            if (hud == null) return;

            hud.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }


    public static final class WorldRenderEvents {
        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

            for (TextRenderEntry entry : TextRenderQueue.consume()) {
                textParticleRenderer.render(
                    entry.text(),
                    event.getCamera(),
                    entry.x(), entry.y(), entry.z(),
                    entry.u(), entry.v(),
                    entry.color(),
                    buffers,
                    entry.light()
                );
            }
        }
    }

    public final static class ClientSetup {

        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ToroHealth.toroHealthHud = new ToroHealthHud(Minecraft.getInstance());
                ToroHealth.textParticleRenderer = new TextParticleRenderer(Minecraft.getInstance());
            });
        }

        public static void onRegisterParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                ToroHealthParticles.HEALTH_CHANGE.get(),
                HealthChangeParticle.HealthChangeFactory::new
            );
        }
    }

    public static final class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.level == null) return;

            HoldingWeaponUpdater.update();

            if (ToroHealth.toroHealthHud != null) {
                ToroHealth.toroHealthHud.tick();
            }
        }
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
