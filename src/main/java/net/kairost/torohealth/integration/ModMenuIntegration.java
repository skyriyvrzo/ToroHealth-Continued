package net.kairost.torohealth.integration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.kairost.torohealth.config.ModConfig;


@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return parent -> AutoConfigClient.getConfigScreen(ModConfig.class, parent).get();
    }
}