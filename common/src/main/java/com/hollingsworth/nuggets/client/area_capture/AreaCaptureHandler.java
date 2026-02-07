package com.hollingsworth.nuggets.client.area_capture;


import com.hollingsworth.nuggets.client.NuggetClientData;
import com.hollingsworth.nuggets.common.util.RaycastHelper;
import com.hollingsworth.nuggets.common.util.VecHelper;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;


public class AreaCaptureHandler {

    public BlockPos firstTarget;
    public BlockPos secondTarget;
    public boolean showBoundary;
    public Direction selectedFace = null;
    BiConsumer<BoundingBox, AreaCaptureHandler> onConfirmedRegion;
    CaptureSchematicScreen captureSchematicScreen;
    private final String modId;
    private final KeyMapping focusKey;

    public AreaCaptureHandler(String modId, KeyMapping focusKey, BiConsumer<BoundingBox, AreaCaptureHandler> onConfirmedRegion){
        this.modId = modId;
        this.focusKey = focusKey;
        this.onConfirmedRegion = onConfirmedRegion;
        this.captureSchematicScreen = new CaptureSchematicScreen(modId, focusKey, onConfirmedRegion, this);
    }

    // Right click event
    public void rightClickEvent() {
        if (!showBoundary) {
            return;
        }
        captureSchematicScreen.getSelectedElement().onClick();
    }

    // Bound tool key pressed event
    public void toolKeyHit(boolean keyPressed){
        if (!showBoundary) {
            return;
        }
        if (keyPressed && !captureSchematicScreen.focused)
            captureSchematicScreen.focused = true;
        if (!keyPressed && captureSchematicScreen.focused) {
            captureSchematicScreen.focused = false;
            captureSchematicScreen.onClose();
        }
    }

    public boolean mouseScrolled(double delta) {
        if (!showBoundary) {
            return false;
        }
        return captureSchematicScreen.scroll(delta);
    }

    // RenderGuiLayerEvent.Post event
    public void renderInstructions(GuiGraphics graphics, Window window) {
        if (!showBoundary)
            return;
        captureSchematicScreen.renderPassive(graphics, 0);
    }


    public void startCapture(){
        showBoundary = true;
        firstTarget = null;
        secondTarget = null;
        this.captureSchematicScreen = new CaptureSchematicScreen(modId, focusKey, onConfirmedRegion, this);
    }

    public void cancelCapture(){
        showBoundary = false;
        firstTarget = null;
        secondTarget = null;
    }

    public BlockPos selectedPos = null;

    public void renderBoundary(PoseStack poseStack, Matrix4f modelViewMatrix) {
        if (!showBoundary)
            return;
        BlockPos firstPos = firstTarget;
        LocalPlayer player = Minecraft.getInstance().player;
        BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);
        if (trace.getType() == HitResult.Type.BLOCK) {

            BlockPos hit = trace.getBlockPos();
            boolean replaceable = player.level().getBlockState(hit)
                    .canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace)));
            if (trace.getDirection()
                    .getAxis()
                    .isVertical() && !replaceable)
                hit = hit.relative(trace.getDirection());
            selectedPos = hit;
        } else {
            selectedPos = null;
        }
        if (firstPos == null && selectedPos != null) {
            renderBbox(new AABB(selectedPos), poseStack, modelViewMatrix);
            return;
        }
        BlockPos secondPos = secondTarget;
        if (secondPos == null) {
            secondPos = selectedPos;
        }
        AABB currentSelectionBox = null;
        if (secondPos == null) {
            if (firstPos == null) {
                currentSelectionBox = selectedPos == null ? null : new AABB(selectedPos);
            } else {
                currentSelectionBox = selectedPos == null ? new AABB(firstPos) : AABB.encapsulatingFullBlocks(firstPos, selectedPos).expandTowards(1, 1, 1);
            }
        } else {
            currentSelectionBox = AABB.encapsulatingFullBlocks(firstPos, secondPos).expandTowards(1, 1, 1);
        }

        renderBbox(currentSelectionBox, poseStack, modelViewMatrix);
    }

    public void renderBbox(AABB currentSelectionBox, PoseStack poseStack, Matrix4f modelViewMatrix) {
        if (currentSelectionBox == null) {
            return;
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
                .getPosition();

        currentSelectionBox.move(camera.scale(-1));
        currentSelectionBox = currentSelectionBox.move(-camera.x, -camera.y, -camera.z);

        poseStack.pushPose();
        poseStack.mulPose(modelViewMatrix);
        VertexConsumer vertexconsumer = NuggetClientData.bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, vertexconsumer, currentSelectionBox, 0.9F, 0.9F, 0.9F, 1.0f);
        NuggetClientData.bufferSource.endBatch();
        poseStack.popPose();
    }

    public void tick(){
        if(!showBoundary){
            return;
        }
        captureSchematicScreen.update();
        selectedFace = null;
        if (secondTarget != null) {
            Player player = Minecraft.getInstance().player;
            AABB bb = AABB.encapsulatingFullBlocks(firstTarget, secondTarget).expandTowards(1, 1, 1)
                    .inflate(.45f);
            Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
                    .getPosition();
            boolean inside = bb.contains(projectedView);
            RaycastHelper.PredicateTraceResult result =
                    RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
            selectedFace = result.missed() ? null
                    : inside ? result.getFacing()
                    .getOpposite() : result.getFacing();
        }

    }
}
