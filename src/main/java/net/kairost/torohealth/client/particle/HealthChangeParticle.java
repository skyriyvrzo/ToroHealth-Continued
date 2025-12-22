package net.kairost.torohealth.client.particle;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.font.TextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kairost.torohealth.ToroHealth;

@Environment(value=EnvType.CLIENT)
public class HealthChangeParticle
    extends BillboardParticle{
    private final int value;
    HealthChangeParticle(ClientWorld world, int color, int value, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.collidesWithWorld = false;
        this.scale(1.0f);
        this.setBoundingBoxSpacing(0.25f, 0.25f);
        this.maxAge = 50;
        this.gravityStrength = 1.0E-2f;
        this.velocityX = velocityX;
        this.velocityY = velocityY + (double)(this.random.nextFloat() / 500.0f);
        this.velocityZ = velocityZ;
        this.red = (float)(color >> 16 & 0xFF) / 255.0f;
        this.green = (float)(color >> 8 & 0xFF) / 255.0f;
        this.blue = (float)(color & 0xFF) / 255.0f;
        this.value = value;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge || this.alpha <= 0.0f) {
            this.markDead();
            return;
        }
        this.velocityX += (double)(this.random.nextFloat() / 5000.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.velocityZ += (double)(this.random.nextFloat() / 5000.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.velocityY -= (double)this.gravityStrength;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.age >= this.maxAge - 20 && this.alpha > 0.01f) {
            this.alpha -= 0.05f;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Environment(value=EnvType.CLIENT)
    public static class HealthChangeFactory
        implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public HealthChangeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        //@Override
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            Random random = clientWorld.getRandom();
            // use g to encode health change
            int healthChange = (int)Double.doubleToLongBits(g);
            int color = (healthChange > 0) ? ToroHealth.getConfig().particleOptions.healColor : ToroHealth.getConfig().particleOptions.damageColor;
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
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        Vec3d vec3d = camera.getPos();
        float x = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float y = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float z = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        String text = Integer.toString(this.value);
        float h = -client.textRenderer.getWidth(text) / 2.0f;

        int a = (int)(this.alpha * 255.0f) & 0xFF;
        int r = (int)(this.red * 255.0f) & 0xFF;
        int g = (int)(this.green * 255.0f) & 0xFF;
        int b = (int)(this.blue * 255.0f) & 0xFF;
        int color = (a << 24) | (r << 16) | (g << 8) | b;

        int light = this.getBrightness(tickDelta);

        BufferBuilder bufferBuilder = new BufferBuilder(256);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(bufferBuilder);
        client.textRenderer.draw(text, h, -3.0f, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, light);
        immediate.draw();
    }

    @Override
    protected float getMinU() {
        return 0.0F;
    }

    @Override
    protected float getMaxU() {
        return 1.0F;
    }

    @Override
    protected float getMinV() {
        return 0.0F;
    }

    @Override
    protected float getMaxV() {
        return 1.0F;
    }
}