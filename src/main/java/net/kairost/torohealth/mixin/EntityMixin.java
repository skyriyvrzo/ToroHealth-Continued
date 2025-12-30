package net.kairost.torohealth.mixin;

import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getEntityWorld();

    @Shadow
    public abstract Vec3d getEntityPos();

    @Shadow
    public abstract float getHeight();
}
