package net.kairost.torohealth.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RayTrace {
    // modified from minecraft.client.render.GameRender.updateCrosshairTarget
    public static LivingEntity getEntityInCrosshair(float tickDelta, float reachDistance) {
        Minecraft client = Minecraft.getInstance();

        Entity entity = client.getCameraEntity();
        if (entity == null) {
            return null;
        }
        if (client.level == null) {
            return null;
        }
        HitResult crosshairTarget = raycastEntity(entity, reachDistance, tickDelta, false);
        Vec3 vec3d = entity.getEyePosition(tickDelta);
        double e = reachDistance;

        e *= e;
        if (crosshairTarget != null) {
            e = crosshairTarget.getLocation().distanceToSqr(vec3d);
        }

        Vec3 vec3d2 = entity.getViewVector(1.0f);
        Vec3 vec3d3 = vec3d.add(vec3d2.x * reachDistance, vec3d2.y * reachDistance, vec3d2.z * reachDistance);
        AABB box = entity.getBoundingBox().expandTowards(vec3d2.scale(reachDistance)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3d, vec3d3, box, entityx -> !entityx.isSpectator() && entityx.isPickable(), e);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3 vec3d4 = entityHitResult.getLocation();
            double g = vec3d.distanceToSqr(vec3d4);
            if (g < e || crosshairTarget == null) {
                if (entity2 instanceof LivingEntity) {
                    return (LivingEntity) entity2;
                }
                //end dragon
                else if (entity2 instanceof EnderDragonPart part && !part.parentMob.isRemoved()) {
                    return (part.parentMob);
                }
            }
        }
        return null;
    }

    // modified from net.minecraft.entity.Entity.raycast, adding ignore opaque blocks feature
    private static BlockHitResult raycastEntity(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 vec3d = entity.getEyePosition(tickDelta);
        Vec3 vec3d2 = entity.getViewVector(tickDelta);
        Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        return raycastBlockView(entity.level(), new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
    }



    private static BlockHitResult raycastBlockView(Level world, ClipContext context) {
        return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (c, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!blockState.canOcclude()) {
                return null;
            }
            VoxelShape blockShape = c.getBlockShape(blockState, world, pos);
            return world.clipWithInteractionOverride(c.getFrom(), c.getTo(), pos, blockShape, blockState);
        }, (c) -> {
            Vec3 v = c.getFrom().subtract(c.getTo());
            return BlockHitResult.miss(c.getTo(), Direction.getApproximateNearest(v.x, v.y, v.z), BlockPos.containing(c.getTo()));
        });
    }
}
