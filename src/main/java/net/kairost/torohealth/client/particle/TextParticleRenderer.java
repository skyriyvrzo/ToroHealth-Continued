package net.kairost.torohealth.client.particle;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;

public class TextParticleRenderer {
    private final MinecraftClient client;

    public TextParticleRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(String text, Camera camera, float x, float y, float z, float u, float v, int color, OrderedRenderCommandQueue queue, int light) {
        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        matrices.translate(-this.client.textRenderer.getWidth(text), -3, 0);

        queue.submitText(
            matrices,
            u, v,
            Text.literal(text).asOrderedText(),
            false,
            TextRenderer.TextLayerType.NORMAL,
            light,
            color,
            0,
            0
        );
    }
}