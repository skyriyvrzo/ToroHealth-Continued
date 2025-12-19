package net.torocraft.torohealth.mixin;

import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World world;

    @Shadow
    public abstract int getId();

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public abstract float getHeight();
}
