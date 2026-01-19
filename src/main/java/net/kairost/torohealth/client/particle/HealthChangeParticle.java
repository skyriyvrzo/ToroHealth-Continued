package net.kairost.torohealth.client.particle;

import org.jetbrains.annotations.NotNull;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.kairost.torohealth.config.ToroHealthConfig;

public class HealthChangeParticle
    extends SingleQuadParticle{
    private final int value;
    HealthChangeParticle(ClientLevel world, int color, int value, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.hasPhysics = false;
        this.scale(1.0f);
        this.setSize(0.25f, 0.25f);
        this.lifetime = 50;
        this.gravity = 1.0E-2f;
        this.xd = velocityX;
        this.yd = velocityY + (double)(this.random.nextFloat() / 500.0f);
        this.zd = velocityZ;
        this.rCol = (float)(color >> 16 & 0xFF) / 255.0f;
        this.gCol = (float)(color >> 8 & 0xFF) / 255.0f;
        this.bCol = (float)(color & 0xFF) / 255.0f;
        this.value = value;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd = this.xd + this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.zd = this.zd + this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.yd = this.yd - this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 20 && this.alpha > 0.01f) {
                this.alpha -= 0.05f;
            }
        } else {
            this.remove();
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class HealthChangeFactory
        implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public HealthChangeFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        //@Override
        @Override
        public Particle createParticle(@NotNull SimpleParticleType simpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            RandomSource random = clientWorld.getRandom();
            // use g to encode health change
            int healthChange = (int)Double.doubleToLongBits(g);
            int color = (healthChange > 0) ? ToroHealthConfig.CONFIG.particleOptions.healColor.get() : ToroHealthConfig.CONFIG.particleOptions.damageColor.get();
            int value = Math.abs(healthChange);
            double vx = random.nextGaussian() * 0.035;
            double vy = 0.15 + (random.nextGaussian() * 0.01);
            double vz = random.nextGaussian() * 0.035;
            HealthChangeParticle healthChangeParticle = new HealthChangeParticle(clientWorld, color, value, d, e, f, vx, vy, vz);
            healthChangeParticle.setAlpha(1.0f);
            return healthChangeParticle;
        }
    }

    @Override
    public void render(@NotNull VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Minecraft client = Minecraft.getInstance();

        Vec3 vec3d = camera.getPosition();
        float x = (float)(Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
        float y = (float)(Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
        float z = (float)(Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());

        String text = Integer.toString(this.value);
        float h = -client.font.width(text) / 2.0f;

        int a = (int)(this.alpha * 255.0f) & 0xFF;
        int r = (int)(this.rCol * 255.0f) & 0xFF;
        int g = (int)(this.gCol * 255.0f) & 0xFF;
        int b = (int)(this.bCol * 255.0f) & 0xFF;
        int color = (a << 24) | (r << 16) | (g << 8) | b;

        int light = ToroHealthConfig.CONFIG.particleOptions.particleLightMode.get().equals(ToroHealthConfig.ParticleLightMode.FULL_BRIGHT) ? LightTexture.FULL_BRIGHT : this.getLightColor(tickDelta);

        TextRenderQueue.submit(
            new TextRenderEntry(text, x, y, z, h, -3.0f, color, light)
        );
    }

    @Override
    protected float getU0() {
        return 0.0F;
    }

    @Override
    protected float getU1() {
        return 1.0F;
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }
}
