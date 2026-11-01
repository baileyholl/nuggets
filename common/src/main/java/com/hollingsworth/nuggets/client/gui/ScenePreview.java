package com.hollingsworth.nuggets.client.gui;

import com.hollingsworth.nuggets.client.rendering.CachedStructureRenderData;
import com.hollingsworth.nuggets.client.rendering.DireRenderMethods;
import com.hollingsworth.nuggets.client.rendering.StatePos;
import com.hollingsworth.nuggets.client.rendering.StructureRenderer;
import com.hollingsworth.nuggets.client.scene.CameraSettings;
import com.hollingsworth.nuggets.client.scene.LytSize;
import com.hollingsworth.nuggets.client.scene.Scene;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.function.UnaryOperator;

public class ScenePreview extends AbstractWidget implements AutoCloseable {
    private int yaw = 225;
    private int pitch = 30;
    private int roll;
    public final Scene scene;
    private final CachedStructureRenderData data;
    private ScreenRectangle imageRect = ScreenRectangle.empty();

    private boolean doSort;
    private int sortDebounce;

    public ScenePreview(int x, int y, int width, int height, Scene scene, StructureTemplate template) {
        super(x, y, width, height, Component.empty());
        this.scene = scene;
        data = new CachedStructureRenderData(template);
        StructureRenderer.generateRender(data, Minecraft.getInstance().level, BlockPos.ZERO, 1f, Vec3.ZERO);
        scheduleSort();
    }

    public void tick() {
        if (doSort) {
            if (sortDebounce > 0) {
                sortDebounce--;
            } else {
                doSort = false;
                sortAll(data, Vec3.ZERO);
            }
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.flush();
        Window window = Minecraft.getInstance().getWindow();
        final float scale = (float) window.getGuiScale();
        CameraSettings cameraSettings = scene.getCameraSettings();
        cameraSettings.setIsometricYawPitchRoll(yaw, pitch, roll);
        cameraSettings.setRotationCenter(scene.getWorldCenter());

        cameraSettings.setZoom(1.0f);
        scene.centerScene();
        LytSize size = scene.getPreferredSize();
        cameraSettings.setViewportSize(size);

        Matrix4f projectionMatrix = cameraSettings.getProjectionMatrix();
        Matrix4f viewMatrix = cameraSettings.getViewMatrix();

        // We only scale the viewport, not scaling the view matrix means the scene will still fill it
        var renderWidth = (int) Math.max(1, size.width() * scale);
        var renderHeight = (int) Math.max(1, size.height() * scale);

        LytSize newDim = new LytSize(renderWidth, renderHeight).scaledToFit(new LytSize(width, height));
        int x = getX() + width / 2 - newDim.width() / 2;
        int y = getY() + height / 2 - newDim.height() / 2;
        imageRect = new ScreenRectangle(x, y, newDim.width(), newDim.height());

        RenderSystem.viewport((int) (x * scale), (int) (y * scale), (int) (newDim.width() * scale), (int) (newDim.height() * scale)); //The viewport is like a mini world where things get drawn
        RenderSystem.backupProjectionMatrix();

        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z); //This is needed to switch to 3d rendering instead of 2d for the screen

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.mulPose(viewMatrix);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false); //Clear the depth buffer so it can draw where it is

        RenderSystem.runAsFancy(() -> drawRenderScreen(poseStack));

        poseStack.popPose();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        RenderSystem.restoreProjectionMatrix();
    }

    public ScreenRectangle getImageRect() {
        return imageRect;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }

    public void drawRenderScreen(PoseStack matrix) {
        StructureRenderer.drawVertexBuffers(data, matrix.last().pose(), RenderSystem.getProjectionMatrix(), UnaryOperator.identity());

        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity(); // BufferSource draws with the global model-view, which the GUI offsets by -11000 on z; keep the camera in the pose stack instead
        RenderSystem.applyModelViewMatrix();
        DireRenderMethods.MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new DireRenderMethods.MultiplyAlphaRenderTypeBuffer(buffersource, 1f);
        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        BlockEntityRenderDispatcher blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        for (StatePos pos : data.statePosCache) {
            if (pos.state.isAir() || StructureRenderer.isModelRender(pos.state)) continue;
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            BlockEntity blockEntity = data.fakeRenderingWorld.getBlockEntity(pos.pos);
            if (blockEntity != null) {
                var renderer = blockEntityRenderer.getRenderer(blockEntity);
                try {
                    renderer.render(blockEntity, 0, matrix, multiplyAlphaRenderTypeBuffer, 15728640, OverlayTexture.NO_OVERLAY);
                } catch (Exception e) {
                    //No Op
                }
            } else {
                try {
                    DireRenderMethods.renderBETransparent(data.fakeRenderingWorld.getBlockState(pos.pos), matrix, buffersource, 15728640, 655360, 1.0f);
                } catch (Exception e) {
                    //No Op
                }
            }
            matrix.popPose();
        }
        buffersource.endBatch();
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    //Sort all the RenderTypes
    public static void sortAll(CachedStructureRenderData data, Vec3 projectedView) {
        for (Map.Entry<RenderType, MeshData.SortState> entry : data.sortStates.entrySet()) {
            RenderType renderType = entry.getKey();
            var renderedBuffer = sort(data, projectedView, renderType);
            VertexBuffer vertexBuffer = data.vertexBuffers.get(renderType);
            vertexBuffer.bind();
            vertexBuffer.uploadIndexBuffer(renderedBuffer);
            VertexBuffer.unbind();
        }
    }

    //Sort the render type we pass in - using DireBufferBuilder because we want to sort in the opposite direction from normal
    public static ByteBufferBuilder.Result sort(CachedStructureRenderData data, Vec3 projectedView, RenderType renderType) {
        // Move our projected view in the direction of 0,0,0 by a tiny amount, accounting for negative values
        Vec3 inverted = projectedView.scale(-1);
        Vec3 subtracted = projectedView.add(inverted.normalize().scale(0.1));

        Vector3f sortPos = new Vector3f((float) -subtracted.x, (float) -subtracted.y, (float) -subtracted.z);
        return data.sortStates.get(renderType).buildSortedIndexBuffer(data.getByteBuffer(renderType), VertexSorting.byDistance(v -> -sortPos.distanceSquared(v)));
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
        scheduleSort();
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
        scheduleSort();
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
        scheduleSort();
    }

    private void scheduleSort() {
        doSort = true;
        sortDebounce = 20;
    }

    @Override
    public void close() {
        data.close();
    }
}
