package net.kairost.torohealth.client.util;

import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;

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

    public static boolean showHealthBar(Entity entity, MinecraftClient client) {
        return entity instanceof LivingEntity
            && !(entity instanceof ArmorStandEntity)
            && (!entity.isInvisibleTo(client.player)
                || entity.isGlowing()
                || entity.isOnFire()
                || entity instanceof CreeperEntity && ((CreeperEntity) entity).shouldRenderOverlay() // charged creeper
                || StreamSupport.stream(entity.getItemsEquipped().spliterator(), false).anyMatch(is -> !is.isEmpty()))
            && entity != client.player
            && !entity.hasPassengers()
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
}
