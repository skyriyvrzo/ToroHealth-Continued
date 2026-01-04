package net.kairost.torohealth.mixin.accessor;

import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherEntity.class)
public interface WitherEntityAccessor {
    @Accessor("sideHeadYaws")
    float[] torohealth$getSideHeadYaws();

    @Accessor("lastSideHeadYaws")
    float[] torohealth$getLastSideHeadYaws();
}
