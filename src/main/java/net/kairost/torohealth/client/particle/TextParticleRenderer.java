package net.kairost.torohealth.client.particle;

import org.joml.Quaternionfc;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;

public class TextParticleRenderer {
    private final Minecraft client;

    public TextParticleRenderer(Minecraft client) {
        this.client = client;
    }

    public void render(String text, Quaternionfc cameraRotation, float x, float y, float z, float u, float v, int color, SubmitNodeCollector queue, int light) {
        PoseStack matrices = new PoseStack();
        matrices.translate(x, y, z);
        matrices.mulPose(cameraRotation);
        matrices.scale(0.025f, -0.025f, 0.025f);
        matrices.translate(-this.client.font.width(text), -3, 0);

        queue.submitText(
            matrices,
            u, v,
            Component.literal(text).getVisualOrderText(),
            false,
            Font.DisplayMode.NORMAL,
            light,
            color,
            0,
            0
        );
    }
}