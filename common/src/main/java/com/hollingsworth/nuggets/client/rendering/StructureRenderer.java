package com.hollingsworth.nuggets.client.rendering;

import com.hollingsworth.nuggets.client.Color;
import com.hollingsworth.nuggets.common.util.RaycastHelper;
import com.hollingsworth.nuggets.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class StructureRenderer {
    private static final List<RenderType> DRAW_ORDER = List.of(RenderType.solid(), RenderType.cutout(), RenderType.cutoutMipped(), RenderType.translucent(), RenderType.tripwire());

    //Start rendering - this is the most expensive part, so we render it, then cache it, and draw it over and over (much cheaper)
    public static void buildRender(CachedStructureRenderData data, PoseStack poseStack, Player player) {
        BlockHitResult lookingAt = RaycastHelper.getLookingAt(data.distanceFromCameraCast, player, true);
        BlockPos renderPos = data.anchorPos == null ? lookingAt.getBlockPos() : data.anchorPos;
        if (renderPos == null) {
            return;
        }
        renderPos = renderPos.above();
        drawBoundBox(data, poseStack, renderPos);

        renderPos = renderPos.above();
        //Start drawing the Render and cache it, used for both Building and Copy/Paste
        if (shouldUpdateRender(data, renderPos)) {
            generateRender(data, player.level(), renderPos, 0.5f);
            data.lastRenderPos = renderPos;
        }
    }

    public static boolean shouldUpdateRender(CachedStructureRenderData data, BlockPos renderPos) {
        return data.lastRenderPos == null || !data.lastRenderPos.equals(renderPos);
    }

    public static void clearByteBuffers(CachedStructureRenderData data) { //Prevents leaks - Unused?
        for (Map.Entry<RenderType, ByteBufferBuilder> entry : data.builders.entrySet()) {
            entry.getValue().clear();
        }
        data.bufferBuilders.clear();
        data.sortStates.clear();
        data.meshDatas.clear();
    }

    public static void generateRender(CachedStructureRenderData data, Level level, BlockPos renderPos, float transparency) {
        generateRender(data, level, renderPos, transparency, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
    }

    /**
     * This method creates a Map<RenderType, VertexBuffer> when given an ArrayList<StatePos> statePosCache - its used both here to draw in-game AND in the TemplateManagerGUI.java class
     */
    public static void generateRender(CachedStructureRenderData data, Level level, BlockPos renderPos, float transparency, Vec3 cameraPosition) {
        List<StatePos> statePosCache = data.statePosCache;
        Map<RenderType, VertexBuffer> vertexBuffers = data.vertexBuffers;
        if (statePosCache == null || statePosCache.isEmpty()) return;
        data.fakeRenderingWorld = new FakeRenderingWorld(level, statePosCache, renderPos);
        PoseStack matrix = new PoseStack(); //Create a new matrix stack for use in the buffer building process
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelBlockRenderer = dispatcher.getModelRenderer();
        final RandomSource random = RandomSource.create();
        clearByteBuffers(data);
        //Iterate through the state pos cache and start drawing to the VertexBuffers - skip modelRenders(like chests) - include fluids (even though they don't work yet)
        for (StatePos pos : statePosCache) {
            BlockState renderState = data.fakeRenderingWorld.getBlockStateWithoutReal(pos.pos);
            if (renderState.isAir() || !(isModelRender(pos.state) || !pos.state.getFluidState().isEmpty()))
                continue;

            BakedModel ibakedmodel = dispatcher.getBlockModel(renderState);
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());

            for (RenderType renderType : Services.PLATFORM.getRenderTypes(ibakedmodel, renderState, random)) {
                //Flowers render weirdly so we use a custom renderer to make them look better. Glass and Flowers are both cutouts, so we only want this for non-cube blocks
                if (renderType.equals(RenderType.cutout()) && renderState.getShape(level, pos.pos.offset(renderPos)).equals(Shapes.block()))
                    renderType = RenderType.translucent();
                BufferBuilder builder = data.bufferBuilders.computeIfAbsent(renderType, rt -> new BufferBuilder(data.getByteBuffer(rt), rt.mode(), rt.format()));
                TintedVertexConsumer vertexConsumer = new TintedVertexConsumer(builder, 1f, 1f, 1f, transparency);
                //Use tesselateBlock to skip the block.isModel check - this helps render Create blocks that are both models AND animated
                if (renderState.getFluidState().isEmpty()) {
                    try {
                        modelBlockRenderer.tesselateBlock(data.fakeRenderingWorld, ibakedmodel, renderState, pos.pos.offset(renderPos).above(255), matrix, vertexConsumer, false, random, renderState.getSeed(pos.pos.offset(renderPos)), OverlayTexture.NO_OVERLAY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        RenderFluidBlock.renderFluidBlock(renderState, level, pos.pos.offset(renderPos).above(255), matrix, vertexConsumer, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            matrix.popPose();
        }

        Vec3 subtracted = cameraPosition.subtract(renderPos.getX(), renderPos.getY(), renderPos.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);
        for (Map.Entry<RenderType, BufferBuilder> entry : data.bufferBuilders.entrySet()) {
            RenderType renderType = entry.getKey();
            ByteBufferBuilder byteBufferBuilder = data.getByteBuffer(renderType);
            BufferBuilder builder = entry.getValue();
            var meshDatas = data.meshDatas;
            if (meshDatas.containsKey(renderType) && meshDatas.get(renderType) != null)
                meshDatas.get(renderType).close();
            meshDatas.put(renderType, builder.build());
            if (meshDatas.containsKey(renderType) && meshDatas.get(renderType) != null) {
                data.sortStates.put(renderType, meshDatas.get(renderType).sortQuads(byteBufferBuilder, VertexSorting.byDistance(v -> -sortPos.distanceSquared(v))));
                VertexBuffer vertexBuffer = vertexBuffers.get(entry.getKey());
                vertexBuffer.bind();
                vertexBuffer.upload(meshDatas.get(renderType));
                VertexBuffer.unbind();
            }
        }
    }

    public static void drawBoundBox(CachedStructureRenderData data, PoseStack matrix, BlockPos blockPos) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        Color color = Color.BLUE;
        var boundingBox = data.boundingBox;
        if (boundingBox != null) {
            BlockPos min = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            BlockPos max = new BlockPos(boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ());
            min = min.offset(blockPos);
            max = max.offset(blockPos);
            DireRenderMethods.renderCopy(matrix, min, max, color);
        }
        matrix.popPose();
    }

    public static boolean isModelRender(BlockState state) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel ibakedmodel = dispatcher.getBlockModel(state);
        for (Direction direction : Direction.values()) {
            if (!ibakedmodel.getQuads(state, direction, RandomSource.create()).isEmpty()) {
                return true;
            }
            if (!ibakedmodel.getQuads(state, null, RandomSource.create()).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    //Draw what we've cached
    public static void drawRender(CachedStructureRenderData data, PoseStack poseStack, Matrix4f projectionMatrix, Matrix4f modelViewMatrix, Player player) {
        BlockPos anchorPos = data.anchorPos;
        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockHitResult lookingAt = RaycastHelper.getLookingAt(data.distanceFromCameraCast, player, true);
        BlockPos renderPos = anchorPos == null ? lookingAt.getBlockPos() : anchorPos;
        BlockState lookingAtState = player.level().getBlockState(renderPos);

        if (lookingAtState.isAir() && anchorPos == null)
            return;
        renderPos = renderPos.above();
        //Sort every <X> Frames to prevent screendoor effect
        if (data.sortCounter > 20) {
            sortAll(data, renderPos);
            data.sortCounter = 0;
        } else {
            data.sortCounter++;
        }

        poseStack.pushPose();
        poseStack.mulPose(modelViewMatrix);
        poseStack.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        poseStack.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
        drawVertexBuffers(data, poseStack.last().pose(), projectionMatrix, renderType -> renderType.equals(RenderType.cutout()) ? DireRenderTypes.RenderBlock : RenderType.translucent());
        poseStack.popPose();

        DireRenderMethods.MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new DireRenderMethods.MultiplyAlphaRenderTypeBuffer(buffersource, 0.5f);
        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        data.fakeRenderingWorld = new FakeRenderingWorld(player.level(), data.statePosCache, renderPos);
        BlockEntityRenderDispatcher blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        for (StatePos pos : data.statePosCache) {
            if (pos.state.isAir() || isModelRender(pos.state))
                continue;
            poseStack.pushPose();
            poseStack.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            poseStack.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
            poseStack.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            BlockEntity blockEntity = data.fakeRenderingWorld.getBlockEntity(pos.pos);
            if (blockEntity != null)
                blockEntityRenderer.render(blockEntity, 0, poseStack, multiplyAlphaRenderTypeBuffer);
            else
                DireRenderMethods.renderBETransparent(data.fakeRenderingWorld.getBlockState(pos.pos), poseStack, buffersource, 15728640, 655360, 0.5f);
            poseStack.popPose();
        }
    }

    public static void drawVertexBuffers(CachedStructureRenderData data, Matrix4f modelView, Matrix4f projection, UnaryOperator<RenderType> stateOverride) {
        try {
            for (RenderType renderType : DRAW_ORDER) {
                VertexBuffer vertexBuffer = data.vertexBuffers.get(renderType);
                if (vertexBuffer.getFormat() == null)
                    continue; //IDE says this is never null, but if we remove this check we crash because its null so....
                RenderType drawRenderType = stateOverride.apply(renderType);
                drawRenderType.setupRenderState();
                vertexBuffer.bind();
                vertexBuffer.drawWithShader(modelView, projection, RenderSystem.getShader());
                VertexBuffer.unbind();
                drawRenderType.clearRenderState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Sort all the RenderTypes
    public static void sortAll(CachedStructureRenderData data, BlockPos lookingAt) {
        for (Map.Entry<RenderType, MeshData.SortState> entry : data.sortStates.entrySet()) {
            RenderType renderType = entry.getKey();
            var renderedBuffer = sort(data, lookingAt, renderType);
            VertexBuffer vertexBuffer = data.vertexBuffers.get(renderType);
            vertexBuffer.bind();
            vertexBuffer.uploadIndexBuffer(renderedBuffer);
            VertexBuffer.unbind();
        }
    }

    //Sort the render type we pass in - using DireBufferBuilder because we want to sort in the opposite direction from normal
    public static ByteBufferBuilder.Result sort(CachedStructureRenderData data, BlockPos lookingAt, RenderType renderType) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 subtracted = projectedView.subtract(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);
        return data.sortStates.get(renderType).buildSortedIndexBuffer(data.getByteBuffer(renderType), VertexSorting.byDistance(v -> -sortPos.distanceSquared(v)));
    }
}
