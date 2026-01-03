package net.kairost.torohealth.config;

import com.google.gson.annotations.JsonAdapter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;


@Config(name = "torohealth")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    public boolean enabled = true;

    @ConfigEntry.Gui.CollapsibleObject
    public HudOptions hudOptions = new HudOptions();
    public static class HudOptions {
        @ConfigEntry.Gui.Tooltip
        public boolean showHUD = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showEntity = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public FrameStyle frameStyle = FrameStyle.LIGHT;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public HealthChangeType healthChangeType= HealthChangeType.LAST;

        @ConfigEntry.Gui.Tooltip
        public boolean onlyWhenHurt = false;

        @ConfigEntry.Gui.Tooltip
        public float hudDistance = 128f;

        @ConfigEntry.Gui.Tooltip
        public int hudHideDelay = 20;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public AnchorPoint anchorPoint = AnchorPoint.TOP_LEFT;

        @ConfigEntry.Gui.Tooltip
        public int hudXPosition = 2;

        @ConfigEntry.Gui.Tooltip
        public int hudYPosition = 2;

        @ConfigEntry.Gui.Tooltip
        public int hudScale = 1;
    }


    @ConfigEntry.Gui.CollapsibleObject
    public ParticleOptions particleOptions = new ParticleOptions();
    public static class ParticleOptions {
        @ConfigEntry.Gui.Tooltip
        public boolean showParticle = true;

        @ConfigEntry.Gui.Tooltip
        public float particleDistance = 64f;

        @ConfigEntry.Gui.Excluded
        public transient float particleDistanceSquared = 0;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int damageColor = 0xff0000;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int healColor = 0x00ff00;
    }


    @ConfigEntry.Gui.CollapsibleObject
    public InWorldBarOptions inWorldBarOptions = new InWorldBarOptions();
    public static class InWorldBarOptions {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public InWorldBarVisibilityMode inWorldBarVisibilityMode = InWorldBarVisibilityMode.NONE;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public InWorldBarLightMode inWorldBarLightMode = InWorldBarLightMode.ENTITY_LIGHT;

        @ConfigEntry.Gui.Tooltip
        public boolean onlyWhenLookingAt = false;

        @ConfigEntry.Gui.Tooltip
        public boolean onlyWhenHurt = false;

        @ConfigEntry.Gui.Tooltip
        public float inWorldBarDistance = 64f;

        @ConfigEntry.Gui.Excluded
        public transient float inWorldBarDistanceSquared = 0;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public BarColor barColor = new BarColor();
    public static class BarColor {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int friendColor = 0x00ff00;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int friendColorSecondary = 0x008000;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int foeColor = 0xff0000;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        @JsonAdapter(ColorJsonAdapter.class)
        public int foeColorSecondary = 0x800000;
    }

    public static void init()
    {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        INSTANCE.postLoad();
    }


    public enum AnchorPoint {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum FrameStyle {
        LIGHT, HEAVY
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
        // Recalculate dependent field
        particleOptions.particleDistanceSquared = particleOptions.particleDistance * particleOptions.particleDistance;
        inWorldBarOptions.inWorldBarDistanceSquared = inWorldBarOptions.inWorldBarDistance * inWorldBarOptions.inWorldBarDistance;
    }
}