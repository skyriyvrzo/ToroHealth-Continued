package net.kairost.torohealth.mixin;

import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.data.BarState;
import net.kairost.torohealth.data.BarStateAccessor;
import net.kairost.torohealth.mixin.accessor.LivingEntityAccessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements BarStateAccessor{
    @Shadow
    public abstract float getHealth();

    @Unique
    private BarState barState;

    @Override
    public BarState torohealth$getBarState() {
        if (this.barState == null) {
            this.barState = BarState.create((LivingEntity) (Object) this);
        }
        return barState;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void torohealth$tick(CallbackInfo info) {
        if (this.barState != null) {
            this.barState.tick();
            return;
        }
        this.barState = BarState.create((LivingEntity) (Object) this);
    }

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void torohealth$onTrackedData(TrackedData<?> data, CallbackInfo callbackInfo) {
        if (data.equals(LivingEntityAccessor.getHealthData())) {
            if (this.barState == null) {
                this.barState = BarState.create((LivingEntity) (Object) this);
            } else {
                this.barState.updateHealth(this.getHealth());
                if (this.barState.health != this.barState.lastHealth) {
                    this.barState.handleHealthChange();
                    // create healthChangeParticle
                    if (this.barState.healthChangeLast != 0 && ToroHealth.getConfig().particleOptions.showParticle && ToroHealth.getConfig().enabled) {
                        Vec3d entityLocation = this.getEntityPos();
                        this.getEntityWorld().addImportantParticleClient(ToroHealth.HEALTH_CHANGE, true, entityLocation.x, entityLocation.y + this.getHeight() / 2, entityLocation.z, Double.longBitsToDouble(this.barState.healthChangeLast & 0xFFFFFFFFL), 0, 0);
                    }
                }
            }
        }
    }
}
