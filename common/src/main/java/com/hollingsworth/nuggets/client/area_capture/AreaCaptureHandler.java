package com.hollingsworth.nuggets.client.area_capture;


import com.hollingsworth.nuggets.client.NuggetClientData;
import com.hollingsworth.nuggets.common.util.RaycastHelper;
import com.hollingsworth.nuggets.common.util.VecHelper;
import com.hollingsworth.nuggets.common.util.WorldHelpers;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class AreaCaptureHandler {

    public BlockPos firstTarget;
    public BlockPos secondTarget;
    public boolean showBoundary;
    public Direction selectedFace = null;
    public BiConsumer<GuiGraphics, Window> onRender;
    Consumer<StructureTemplate> onConfirmedStructure;

    public AreaCaptureHandler(BiConsumer<GuiGraphics, Window> onRender, Consumer<StructureTemplate> onConfirmedStructure){
        this.onRender = onRender;
        this.onConfirmedStructure = onConfirmedStructure;
    }

    public void startCapture(){
        showBoundary = true;
        firstTarget = null;
        secondTarget = null;
    }

    public void cancelCapture(){
        showBoundary = false;
        firstTarget = null;
        secondTarget = null;
    }

    public void onConfirmHit() {
        if (!showBoundary) {
            return;
        }
        showBoundary = false;
        if (firstTarget != null && secondTarget != null) {
            StructureTemplate structure = WorldHelpers.getStructure(Minecraft.getInstance().level, firstTarget, secondTarget);
            this.onConfirmedStructure.accept(structure);
        }
    }

    public void onCancelHit() {
        if (!showBoundary) {
            return;
        }
        cancelCapture();
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

    public boolean positionClicked() {
        if (!showBoundary) {
            return false;
        }
        BlockPos pos = selectedPos;
        if (pos == null) {
            return false;
        }
        if (firstTarget == null) {
            firstTarget = pos.immutable();
            return true;
        } else if (secondTarget == null && !firstTarget.equals(pos)) {
            secondTarget = pos.immutable();
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double delta){
        if (!showBoundary || firstTarget == null || secondTarget == null) {
            return false;
        }

        if (!Screen.hasControlDown())
            return false;

        if (selectedFace == null)
            return true;

        AABB bb = new AABB(firstTarget.getX(), firstTarget.getY(), firstTarget.getZ(), secondTarget.getX(), secondTarget.getY(), secondTarget.getZ());
        Vec3i vec = selectedFace.getNormal();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
                .getPosition();
        if (bb.contains(projectedView))
            delta *= -1;

        int x = (int) (vec.getX() * delta);
        int y = (int) (vec.getY() * delta);
        int z = (int) (vec.getZ() * delta);

        Direction.AxisDirection axisDirection = selectedFace.getAxisDirection();
        if (axisDirection == Direction.AxisDirection.NEGATIVE)
            bb = bb.move(-x, -y, -z);

        double maxX = Math.max(bb.maxX - x * axisDirection.getStep(), bb.minX);
        double maxY = Math.max(bb.maxY - y * axisDirection.getStep(), bb.minY);
        double maxZ = Math.max(bb.maxZ - z * axisDirection.getStep(), bb.minZ);
        bb = new AABB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

        firstTarget = net.minecraft.core.BlockPos.containing(bb.minX, bb.minY, bb.minZ);
        secondTarget = net.minecraft.core.BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);
        LocalPlayer player = Minecraft.getInstance().player;
        player.displayClientMessage(Component.translatable("blockprints.dimensions", (int) bb.getXsize() + 1, (int) bb.getYsize() + 1,
                        (int) bb.getZsize() + 1), true);


        return true;
    }

    public void tick(){
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

    public void renderBoundaryUI(GuiGraphics graphics, Window window) {
        if (!showBoundary || Minecraft.getInstance().options.hideGui)
            return;
        onRender.accept(graphics, window);
    }

}
