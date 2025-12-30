package net.kairost.torohealth.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.LayeredCustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;


//modified on BillboardParticleSubmittable
public class TextureSubmittable implements OrderedRenderCommandQueue.LayeredCustom, Submittable {
    private static final int INITIAL_BUFFER_MAX_LENGTH = 1024;
    private static final int BUFFER_FLOAT_FIELDS = 14;
    private static final int BUFFER_INT_FIELDS = 2;
    private final Map<BillboardParticle.RenderType, TextureSubmittable.Vertices> bufferByType = new HashMap();
    private int textures;

    public void render(
        BillboardParticle.RenderType renderType,
        float x,
        float y,
        float z,
        float width,
        float height,
        float rotationX,
        float rotationY,
        float rotationZ,
        float rotationW,
        float size,
        float minU,
        float maxU,
        float minV,
        float maxV,
        int color,
        int brightness
    ) {
        ((TextureSubmittable.Vertices)this.bufferByType.computeIfAbsent(renderType, renderTypex -> new TextureSubmittable.Vertices()))
            .vertex(x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness);
        this.textures++;
    }

    @Override
    public void onFrameEnd() {
        this.bufferByType.values().forEach(TextureSubmittable.Vertices::reset);
        this.textures = 0;
    }

    @Nullable
    @Override
    public BillboardParticleSubmittable.Buffers submit(LayeredCustomCommandRenderer.VerticesCache cache) {
        int i = this.textures * 4;

        Object var13;
        try (BufferAllocator bufferAllocator = BufferAllocator.fixedSized(i * VertexFormats.POSITION_TEXTURE_COLOR_LIGHT.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            Map<BillboardParticle.RenderType, BillboardParticleSubmittable.Layer> map = new HashMap();
            int j = 0;

            for (Entry<BillboardParticle.RenderType, TextureSubmittable.Vertices> entry : this.bufferByType.entrySet()) {
                ((TextureSubmittable.Vertices)entry.getValue())
                    .render(
                        (x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW,size, minU, maxU, minV, maxV, color, brightness) -> this.drawFace(
                            bufferBuilder, x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness
                        )
                    );
                if (((TextureSubmittable.Vertices)entry.getValue()).nextVertexIndex() > 0) {
                    map.put(
                        (BillboardParticle.RenderType)entry.getKey(),
                        new BillboardParticleSubmittable.Layer(j, ((TextureSubmittable.Vertices)entry.getValue()).nextVertexIndex() * 6)
                    );
                }

                j += ((TextureSubmittable.Vertices)entry.getValue()).nextVertexIndex() * 4;
            }

            BuiltBuffer builtBuffer = bufferBuilder.endNullable();
            if (builtBuffer != null) {
                cache.write(builtBuffer.getBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS).getIndexBuffer(builtBuffer.getDrawParameters().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .write(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                        new Vector3f(),
                        RenderSystem.getTextureMatrix(),
                        RenderSystem.getShaderLineWidth()
                    );
                return new BillboardParticleSubmittable.Buffers(builtBuffer.getDrawParameters().indexCount(), gpuBufferSlice, map);
            }

            var13 = null;
        }

        return (BillboardParticleSubmittable.Buffers)var13;
    }


    @Override
    public void render(
        BillboardParticleSubmittable.Buffers buffers,
        LayeredCustomCommandRenderer.VerticesCache cache,
        RenderPass renderPass,
        TextureManager manager,
        boolean translucent
    ) {
        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        renderPass.setVertexBuffer(0, cache.get());
        renderPass.setIndexBuffer(shapeIndexBuffer.getIndexBuffer(buffers.indexCount()), shapeIndexBuffer.getIndexType());
        renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms());

        for (Entry<BillboardParticle.RenderType, BillboardParticleSubmittable.Layer> entry : buffers.layers().entrySet()) {
            if (translucent == ((BillboardParticle.RenderType)entry.getKey()).translucent()) {
                renderPass.setPipeline(((BillboardParticle.RenderType)entry.getKey()).pipeline());
                renderPass.bindSampler("Sampler0", manager.getTexture(((BillboardParticle.RenderType)entry.getKey()).textureAtlasLocation()).getGlTextureView());
                renderPass.drawIndexed(
                    ((BillboardParticleSubmittable.Layer)entry.getValue()).vertexOffset(), 0, ((BillboardParticleSubmittable.Layer)entry.getValue()).indexCount(), 1
                );
            }
        }
    }

    protected void drawFace(
        VertexConsumer vertexConsumer,
        float x,
        float y,
        float z,
        float width,
        float height,
        float rotationX,
        float rotationY,
        float rotationZ,
        float rotationW,
        float size,
        float minU,
        float maxU,
        float minV,
        float maxV,
        int color,
        int brightness
    ) {
        Quaternionf quaternionf = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, width, 0.0f, size, maxU, maxV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, width, height, size, maxU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 0.0f, height, size, minU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 0.0f, 0.0f, size, minU, maxV, color, brightness);
    }

    private void renderVertex(
        VertexConsumer vertexConsumer,
        Quaternionf rotation,
        float x,
        float y,
        float z,
        float localX,
        float localY,
        float size,
        float maxU,
        float maxV,
        int color,
        int brightness
    ) {
        Vector3f vector3f = new Vector3f(localX, localY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        vertexConsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z()).texture(maxU, maxV).color(color).light(brightness);
    }

    @Override
    public void submit(OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        if (this.textures > 0) {
            orderedRenderCommandQueue.submitCustom(this);
        }
    }


    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface Consumer {
        void consume(
            float x,
            float y,
            float z,
            float width,
            float height,
            float rotationX,
            float rotationY,
            float rotationZ,
            float rotationW,
            float size,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color,
            int brightness
        );
    }

    @Environment(EnvType.CLIENT)
    static class Vertices {
        private int maxVertices = INITIAL_BUFFER_MAX_LENGTH;
        private float[] floatData = new float[BUFFER_FLOAT_FIELDS * INITIAL_BUFFER_MAX_LENGTH];
        private int[] intData = new int[BUFFER_INT_FIELDS * INITIAL_BUFFER_MAX_LENGTH];
        private int nextVertexIndex;

        public void vertex(
            float x,
            float y,
            float z,
            float width,
            float height,
            float rotationX,
            float rotationY,
            float rotationZ,
            float rotationW,
            float size,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color,
            int brightness
        ) {
            if (this.nextVertexIndex >= this.maxVertices) {
                this.increaseCapacity();
            }

            int i = this.nextVertexIndex * BUFFER_FLOAT_FIELDS;
            this.floatData[i++] = x;
            this.floatData[i++] = y;
            this.floatData[i++] = z;
            this.floatData[i++] = width;
            this.floatData[i++] = height;
            this.floatData[i++] = rotationX;
            this.floatData[i++] = rotationY;
            this.floatData[i++] = rotationZ;
            this.floatData[i++] = rotationW;
            this.floatData[i++] = size;
            this.floatData[i++] = minU;
            this.floatData[i++] = maxU;
            this.floatData[i++] = minV;
            this.floatData[i] = maxV;
            i = this.nextVertexIndex * BUFFER_INT_FIELDS;
            this.intData[i++] = color;
            this.intData[i] = brightness;
            this.nextVertexIndex++;
        }

        public void render(TextureSubmittable.Consumer vertexConsumer) {
            for (int i = 0; i < this.nextVertexIndex; i++) {
                int j = i * BUFFER_FLOAT_FIELDS;
                int k = i * BUFFER_INT_FIELDS;
                vertexConsumer.consume(
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j++],
                    this.floatData[j],
                    this.intData[k++],
                    this.intData[k]
                );
            }
        }

        public void reset() {
            this.nextVertexIndex = 0;
        }

        private void increaseCapacity() {
            this.maxVertices *= 2;
            this.floatData = Arrays.copyOf(this.floatData, this.maxVertices * BUFFER_FLOAT_FIELDS);
            this.intData = Arrays.copyOf(this.intData, this.maxVertices * BUFFER_INT_FIELDS);
        }

        public int nextVertexIndex() {
            return this.nextVertexIndex;
        }
    }
}
