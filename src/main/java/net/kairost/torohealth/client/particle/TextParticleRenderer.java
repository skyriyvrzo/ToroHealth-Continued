package net.kairost.torohealth.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

public class TextParticleRenderer {
    private final Minecraft client;

    public TextParticleRenderer(Minecraft client) {
        this.client = client;
    }

    public void render(String text, Camera camera, float x, float y, float z, float u, float v, int color, MultiBufferSource vertexConsumers, int light) {
        PoseStack matrices = new PoseStack();
        matrices.translate(x, y, z);
        matrices.mulPose(camera.rotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        matrices.translate(-client.font.width(text), -3, 0);

        client.font.drawInBatch(text, u, v, color, false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.NORMAL, 0, light);
    }
}