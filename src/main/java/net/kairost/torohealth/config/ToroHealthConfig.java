package net.kairost.torohealth.config;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ToroHealthConfig {

    public static final ToroHealthConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ToroHealthConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue enabled;

    public final HudOptions hudOptions;
    public final ParticleOptions particleOptions;
    public final InWorldBarOptions inWorldBarOptions;
    public final BarColor barColor;

    ToroHealthConfig(ModConfigSpec.Builder builder) {
        enabled = builder
            .comment("Enable ToroHealth")
            .translation("text.autoconfig.torohealth.option.enabled")
            .define("enabled", true);

        hudOptions = new HudOptions(builder);
        particleOptions = new ParticleOptions(builder);
        inWorldBarOptions = new InWorldBarOptions(builder);
        barColor = new BarColor(builder);
    }

    public static final class HudOptions {
        public final ModConfigSpec.BooleanValue showHUD;
        public final ModConfigSpec.BooleanValue showEntity;
        public final ModConfigSpec.EnumValue<FrameStyle> frameStyle;
        public final ModConfigSpec.EnumValue<HealthChangeType> healthChangeType;
        public final ModConfigSpec.BooleanValue onlyWhenHurt;
        public final ModConfigSpec.DoubleValue hudDistance;
        public final ModConfigSpec.IntValue hudHideDelay;
        public final ModConfigSpec.EnumValue<AnchorPoint> anchorPoint;
        public final ModConfigSpec.IntValue hudXPosition;
        public final ModConfigSpec.IntValue hudYPosition;
        public final ModConfigSpec.IntValue hudScale;

        HudOptions(ModConfigSpec.Builder builder) {
            builder.push("hudOptions");

            showHUD = builder.comment("Show HUD")
                .translation("text.autoconfig.torohealth.option.hudOptions.showHUD")
                .define("showHUD", true);

            showEntity = builder.comment("Show entity")
                .translation("text.autoconfig.torohealth.option.hudOptions.showEntity")
                .define("showEntity", true);

            frameStyle = builder.defineEnum("frameStyle", FrameStyle.LIGHT);
            healthChangeType = builder.defineEnum("healthChangeType", HealthChangeType.LAST);

            onlyWhenHurt = builder.define("onlyWhenHurt", false);
            hudDistance = builder.defineInRange("hudDistance", 128D, 0D, 1024D);
            hudHideDelay = builder.defineInRange("hudHideDelay", 20, 0, 200);
            anchorPoint = builder.defineEnum("anchorPoint", AnchorPoint.TOP_LEFT);
            hudXPosition = builder.defineInRange("hudXPosition", 2, -500, 500);
            hudYPosition = builder.defineInRange("hudYPosition", 2, -500, 500);
            hudScale = builder.defineInRange("hudScale", 1, 1, 4);

            builder.pop();
        }
    }



    public static final class ParticleOptions {
        public final ModConfigSpec.BooleanValue showParticle;
        public final ModConfigSpec.EnumValue<ParticleLightMode> particleLightMode;
        public final ModConfigSpec.DoubleValue particleDistance;
        public final ModConfigSpec.IntValue damageColor;
        public final ModConfigSpec.IntValue healColor;

        public float particleDistanceSquared;

        ParticleOptions(ModConfigSpec.Builder builder) {
            builder.push("particleOptions");

            showParticle = builder.define("showParticle", true);
            particleLightMode = builder.defineEnum("particleLightMode", ParticleLightMode.WORLD_LIGHT);
            particleDistance = builder.defineInRange("particleDistance", 64D, 0D, 1024D);

            damageColor = builder.defineInRange(
                "damageColor",
                0xff0000,
                0x000000,
                0xffffff
            );

            healColor = builder.defineInRange(
                "healColor",
                0x00ff00,
                0x000000,
                0xffffff
            );
            builder.pop();
        }
    }

    public static final class InWorldBarOptions {
        public final ModConfigSpec.EnumValue<InWorldBarVisibilityMode> inWorldBarVisibilityMode;
        public final ModConfigSpec.EnumValue<InWorldBarLightMode> inWorldBarLightMode;
        public final ModConfigSpec.BooleanValue onlyWhenLookingAt;
        public final ModConfigSpec.BooleanValue onlyWhenHurt;
        public final ModConfigSpec.DoubleValue inWorldBarDistance;

        public float inWorldBarDistanceSquared;

        InWorldBarOptions(ModConfigSpec.Builder builder) {
            builder.push("inWorldBarOptions");

            inWorldBarVisibilityMode =
                builder.defineEnum("inWorldBarVisibilityMode", InWorldBarVisibilityMode.NONE);
            inWorldBarLightMode =
                builder.defineEnum("inWorldBarLightMode", InWorldBarLightMode.ENTITY_LIGHT);
            onlyWhenLookingAt = builder.define("onlyWhenLookingAt", false);
            onlyWhenHurt = builder.define("onlyWhenHurt", false);
            inWorldBarDistance = builder.defineInRange("inWorldBarDistance", 64D, 0D, 1024D);

            builder.pop();
        }
    }


    public static final class BarColor {
        public final ModConfigSpec.IntValue friendColor;
        public final ModConfigSpec.IntValue friendColorSecondary;
        public final ModConfigSpec.IntValue foeColor;
        public final ModConfigSpec.IntValue foeColorSecondary;

        BarColor(ModConfigSpec.Builder builder) {
            builder.push("barColor");

            friendColor = builder.defineInRange(
                "friendColor",
                0x00ff00,
                0x000000,
                0xffffff
            );
            friendColorSecondary = builder.defineInRange(
                "friendColorSecondary",
                0x008000,
                0x000000,
                0xffffff
            );
            foeColor = builder.defineInRange(
                "foeColor",
                0xff0000,
                0x000000,
                0xffffff
            );
            foeColorSecondary = builder.defineInRange(
                "foeColorSecondary",
                0x800000,
                0x000000,
                0xffffff
            );

            builder.pop();
        }
    }


    public enum AnchorPoint {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum FrameStyle {
        LIGHT, HEAVY
    }

    public enum ParticleLightMode {
        WORLD_LIGHT, FULL_BRIGHT
    }

    public enum InWorldBarVisibilityMode {
        NONE, WHEN_HOLDING_WEAPON, ALWAYS
    }

    public enum InWorldBarLightMode {
        ENTITY_LIGHT, FULL_BRIGHT
    }

    public enum HealthChangeType {
        NONE, LAST, CUMULATIVE
    }


    public void postLoad() {
        particleOptions.particleDistanceSquared =
            (float)(particleOptions.particleDistance.get() *
                particleOptions.particleDistance.get());

        inWorldBarOptions.inWorldBarDistanceSquared =
            (float)(inWorldBarOptions.inWorldBarDistance.get() *
                inWorldBarOptions.inWorldBarDistance.get());
    }

    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            CONFIG.postLoad();
        }
    }

    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            CONFIG.postLoad();
        }
    }
}