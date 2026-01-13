package net.kairost.torohealth.mixin.accessor;

import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherBoss.class)
public interface WitherEntityAccessor {
    @Accessor("yRotHeads")
    float[] torohealth$getSideHeadYaws();

    @Accessor("yRotOHeads")
    float[] torohealth$getPrevSideHeadYaws();
}
