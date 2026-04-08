package net.kairost.torohealth.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;



//modified on BillboardParticleSubmittable
public class TextureSubmittable implements SubmitNodeCollector.ParticleGroupRenderer, ParticleGroupRenderState {
    private static final int INITIAL_BUFFER_MAX_LENGTH = 1024;
    private static final int BUFFER_FLOAT_FIELDS = 14;
    private static final int BUFFER_INT_FIELDS = 2;
    private final Map<SingleQuadParticle.Layer, TextureSubmittable.Vertices> bufferByType = new HashMap();
    private int textures;

    public void render(
        SingleQuadParticle.Layer renderType,
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
        this.bufferByType.computeIfAbsent(renderType, renderTypex -> new Vertices())
            .vertex(x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness);
        this.textures++;
    }

    @Override
    public void clear() {
        this.bufferByType.values().forEach(TextureSubmittable.Vertices::reset);
        this.textures = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.textures == 0;
    }

    @Nullable
    @Override
    public QuadParticleRenderState.PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache cache, boolean translucent) {
        int i = this.textures * 4;

        Object var13;
        try (ByteBufferBuilder bufferAllocator = ByteBufferBuilder.exactlySized(i * DefaultVertexFormat.PARTICLE.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            Map<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer> map = new HashMap();
            int j = 0;

            for (Entry<SingleQuadParticle.Layer, TextureSubmittable.Vertices> entry : this.bufferByType.entrySet()) {
                entry.getValue()
                    .render(
                        (x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW,size, minU, maxU, minV, maxV, color, brightness) -> this.drawFace(
                            bufferBuilder, x, y, z, width, height, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness
                        )
                    );
                if (entry.getValue().nextVertexIndex() > 0) {
                    map.put(
                        entry.getKey(),
                        new QuadParticleRenderState.PreparedLayer(j, entry.getValue().nextVertexIndex() * 6)
                    );
                }

                j += entry.getValue().nextVertexIndex() * 4;
            }

            MeshData builtBuffer = bufferBuilder.build();
            if (builtBuffer != null) {
                cache.write(builtBuffer.vertexBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(builtBuffer.drawState().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
                return new QuadParticleRenderState.PreparedBuffers(builtBuffer.drawState().indexCount(), gpuBufferSlice, map);
            }

            var13 = null;
        }

        return (QuadParticleRenderState.PreparedBuffers)var13;
    }


    @Override
    public void render(
        final QuadParticleRenderState.PreparedBuffers buffers,
        final ParticleFeatureRenderer.ParticleBufferCache cache,
        final RenderPass renderPass,
        final TextureManager manager
    ) {
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        renderPass.setVertexBuffer(0, cache.get());
        renderPass.setIndexBuffer(shapeIndexBuffer.getBuffer(buffers.indexCount()), shapeIndexBuffer.type());
        renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms());

        for (Entry<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer> entry : buffers.layers().entrySet()) {
            renderPass.setPipeline(entry.getKey().pipeline());
            AbstractTexture abstractTexture = manager.getTexture(entry.getKey().textureAtlasLocation());
            renderPass.bindTexture("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
            renderPass.drawIndexed(
                entry.getValue().vertexOffset(), 0, entry.getValue().indexCount(), 1
            );
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
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(maxU, maxV).setColor(color).setLight(brightness);
    }

    @Override
    public void submit(SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        if (this.textures > 0) {
            orderedRenderCommandQueue.submitParticleGroup(this);
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
