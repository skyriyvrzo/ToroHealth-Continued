package net.kairost.torohealth.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;

public class TextParticleRenderer {
    private final MinecraftClient client;

    public TextParticleRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(String text, Camera camera, float x, float y, float z, float u, float v, int color, VertexConsumerProvider vertexConsumers, int light) {
        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        matrices.translate(-client.textRenderer.getWidth(text), -3, 0);

        client.textRenderer.draw(text, u, v, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    }
}