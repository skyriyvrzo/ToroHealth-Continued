package net.kairost.torohealth.client.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;

public class EntityUtil {

    public enum Relation {
        FRIEND, FOE, UNKNOWN
    }

    public static Relation getRelation(Entity entity) {
        if (entity instanceof Monster) {
            return Relation.FOE;
        } else if (entity instanceof PassiveEntity) {
            return Relation.FRIEND;
        } else if (entity instanceof WaterCreatureEntity) {
            return Relation.FRIEND;
        } else if (entity instanceof AmbientEntity) {
            return Relation.FRIEND;
        } else {
            return Relation.UNKNOWN;
        }
    }

    public static boolean showHealthBar(Entity entity, PlayerEntity player) {
        return entity instanceof LivingEntity
            && !(entity instanceof ArmorStandEntity)
            && entity != player
            && !entity.hasPassengers()
            && isDetectable(entity, player);
    }

    public static boolean isDetectable(Entity entity, PlayerEntity player) {
        return (!entity.isInvisibleTo(player)
            || entity.isGlowing()
            || entity.isOnFire()
            || entity instanceof CreeperEntity && ((CreeperEntity) entity).shouldRenderOverlay() // charged creeper
            || entity instanceof ShulkerEntity
            || (entity instanceof LivingEntity && (((LivingEntity) entity).getArmorVisibility() > 0)))
            && !entity.isSpectator();
    }

    public static boolean isFloating(LivingEntity entity) {
        if (entity.isTouchingWater() && !entity.isOnGround())
            return true;

        // air, FlyingEntity
        if (entity instanceof ParrotEntity parrot && parrot.isInAir())
            return true;

        if (entity instanceof BatEntity bat && bat.isRoosting())
            return true;

        if (entity instanceof PhantomEntity || entity instanceof BeeEntity || entity instanceof VexEntity || entity instanceof AllayEntity || entity instanceof GhastEntity || entity instanceof EnderDragonEntity || entity instanceof WitherEntity)
            return true;

        return false;
    }

    public static double getSquaredDistanceToCamera(LivingEntity entity) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        PlayerEntity player = minecraft.player;
        if (player != null) {
            Vec3d vec3d = player.getCameraPosVec(0f);
            return entity.getPos().squaredDistanceTo(vec3d);
        } else {
            return 0d;
        }
    }
}
