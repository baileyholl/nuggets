package com.hollingsworth.nuggets.client.rendering;

import com.hollingsworth.nuggets.client.area_capture.StructureRenderData;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CachedStructureRenderData extends StructureRenderData implements AutoCloseable {
    public final Map<RenderType, MeshData.SortState> sortStates = new HashMap<>();
    public final Map<RenderType, MeshData> meshDatas = new HashMap<>();
    public final Map<RenderType, ByteBufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(renderType -> renderType, type -> new ByteBufferBuilder(type.bufferSize())));
    public final Map<RenderType, VertexBuffer> vertexBuffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(renderType -> renderType, type -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
    public final Map<RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
    public FakeRenderingWorld fakeRenderingWorld;
    public int sortCounter;

    public CachedStructureRenderData(StructureTemplate structureTemplate) {
        super(structureTemplate);
    }

    public ByteBufferBuilder getByteBuffer(RenderType renderType) {
        return builders.get(renderType);
    }

    @Override
    public void close() {
        vertexBuffers.values().forEach(VertexBuffer::close);
        meshDatas.values().stream().filter(Objects::nonNull).forEach(MeshData::close);
        builders.values().forEach(ByteBufferBuilder::close);
    }
}
