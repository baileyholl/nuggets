package com.hollingsworth.nuggets.client.area_capture;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;

public class CaptureSchematicScreen extends BaseSchematicScreen {
    public AreaCaptureHandler areaCaptureHandler;
    public SelectPositionTool selectPositionTool;
    public HorizontalMoveTool horizontalMoveTool;
    public VerticalMoveTool verticalMoveTool;
    public ExpandTool expandTool;
    public ConfirmTool confirmTool;
    public CancelTool cancelTool;


    public BiConsumer<BoundingBox, AreaCaptureHandler> onConfirmed;

    protected CaptureSchematicScreen(String modId, KeyMapping focusKey, BiConsumer<BoundingBox, AreaCaptureHandler> onConfirmed, AreaCaptureHandler areaCaptureHandler) {
        super(modId, focusKey, Component.literal("Capture Schematic Screen"));
        this.areaCaptureHandler = areaCaptureHandler;
        this.onConfirmed = onConfirmed;
        selectPositionTool = new SelectPositionTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_place.png"));

        horizontalMoveTool = new HorizontalMoveTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_horizontal.png"));
        this.verticalMoveTool = new VerticalMoveTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_vertical.png"));
        this.expandTool = new ExpandTool(this, ResourceLocation.fromNamespaceAndPath(modId,
                "textures/gui/visualizer_icon_resize.png"));
        this.confirmTool = new ConfirmTool(this, ResourceLocation.fromNamespaceAndPath(modId,
                "textures/gui/visualizer_icon_confirm.png"));
        this.cancelTool = new CancelTool(this, ResourceLocation.fromNamespaceAndPath(modId,
                "textures/gui/visualizer_trash.png"));

        this.tools.add(selectPositionTool);
        this.tools.add(cancelTool);
    }

    public void onBoundarySet() {
        this.tools.clear();

        this.tools.add(horizontalMoveTool);
        this.tools.add(verticalMoveTool);
        this.tools.add(expandTool);
        this.tools.add(confirmTool);
        this.tools.add(cancelTool);
    }


    public static class SelectPositionTool extends ToolType<CaptureSchematicScreen> {
        public SelectPositionTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.place_tool"), icon, captureSchematicScreen);
        }

        @Override
        public void onClick() {
            AreaCaptureHandler captureHandler = placeSchematicScreen.areaCaptureHandler;
            BlockPos pos = placeSchematicScreen.areaCaptureHandler.selectedPos;
            if (pos == null) {
                return;
            }
            if (captureHandler.firstTarget == null) {
                captureHandler.firstTarget = pos.immutable();
            } else if (captureHandler.secondTarget == null && !captureHandler.firstTarget.equals(pos)) {
                captureHandler.secondTarget = pos.immutable();
                placeSchematicScreen.onBoundarySet();
            }
        }

        @Override
        List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.select_position_tool_description"));
        }
    }


    public static class HorizontalMoveTool extends ToolType<CaptureSchematicScreen> {
        public HorizontalMoveTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.move_horizontal_tool"), icon, captureSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            AreaCaptureHandler areaCaptureHandler = placeSchematicScreen.areaCaptureHandler;
            if (delta > 0) {
                areaCaptureHandler.firstTarget = areaCaptureHandler.firstTarget.east();
                areaCaptureHandler.secondTarget = areaCaptureHandler.secondTarget.east();
            } else {
                areaCaptureHandler.firstTarget = areaCaptureHandler.firstTarget.west();
                areaCaptureHandler.secondTarget = areaCaptureHandler.secondTarget.west();
            }
            return true;
        }

        @Override
        public List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.move_horizontal_description"));
        }
    }

    public static class VerticalMoveTool extends ToolType<CaptureSchematicScreen> {
        public VerticalMoveTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.move_vertical_tool"), icon, captureSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            AreaCaptureHandler areaCaptureHandler = placeSchematicScreen.areaCaptureHandler;
            if (delta > 0) {
                areaCaptureHandler.firstTarget = areaCaptureHandler.firstTarget.above();
                areaCaptureHandler.secondTarget = areaCaptureHandler.secondTarget.above();
            } else {
                areaCaptureHandler.firstTarget = areaCaptureHandler.firstTarget.below();
                areaCaptureHandler.secondTarget = areaCaptureHandler.secondTarget.below();
            }
            return true;
        }

        @Override
        public List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.move_vertical_description"));
        }
    }

    public static class ExpandTool extends ToolType<CaptureSchematicScreen> {
        public ExpandTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.expand_tool"), icon, captureSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            AreaCaptureHandler areaCaptureHandler = placeSchematicScreen.areaCaptureHandler;
            boolean showBoundary = areaCaptureHandler.showBoundary;
            BlockPos firstTarget = areaCaptureHandler.firstTarget;
            BlockPos secondTarget = areaCaptureHandler.secondTarget;
            Direction selectedFace = areaCaptureHandler.selectedFace;
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

            areaCaptureHandler.firstTarget = net.minecraft.core.BlockPos.containing(bb.minX, bb.minY, bb.minZ);
            areaCaptureHandler.secondTarget = net.minecraft.core.BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);

            return true;
        }

        @Override
        List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.expand_tool.description"), Component.translatable("nuggets.expand_tool.description2"));
        }
    }

    public static class ConfirmTool extends ToolType<CaptureSchematicScreen> {
        public ConfirmTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.confirm_tool_capture"), icon, captureSchematicScreen);
        }

        @Override
        public void onClick() {
            AreaCaptureHandler areaCaptureHandler = placeSchematicScreen.areaCaptureHandler;
            areaCaptureHandler.showBoundary = false;
            if (areaCaptureHandler.firstTarget != null && areaCaptureHandler.secondTarget != null) {
                this.placeSchematicScreen.onConfirmed.accept(BoundingBox.fromCorners(areaCaptureHandler.firstTarget, areaCaptureHandler.secondTarget), areaCaptureHandler);
            }
        }

        @Override
        List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.capture_confirm_tool"));
        }
    }

    public static class CancelTool extends ToolType<CaptureSchematicScreen> {
        public CancelTool(CaptureSchematicScreen captureSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.delete_tool"), icon, captureSchematicScreen);
        }

        @Override
        public void onClick() {
            AreaCaptureHandler areaCaptureHandler = placeSchematicScreen.areaCaptureHandler;
            areaCaptureHandler.cancelCapture();
        }

        @Override
        List<Component> getDescription() {
            return List.of(Component.translatable("nuggets.delete_description"));
        }
    }
}
