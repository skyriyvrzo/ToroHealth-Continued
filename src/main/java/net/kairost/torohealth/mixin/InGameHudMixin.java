package net.kairost.torohealth.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Final
    @Shadow
    private LayeredDrawer layeredDrawer;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;)V", at = @At("TAIL"))
    private void torohealth$initInstance(MinecraftClient client, CallbackInfo info) {
        this.layeredDrawer.addLayer((context, tickDelta) -> ToroHealth.toroHealthHud.render(context, tickDelta));
    }
}
