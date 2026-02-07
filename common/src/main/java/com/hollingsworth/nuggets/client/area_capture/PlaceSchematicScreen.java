package com.hollingsworth.nuggets.client.area_capture;

import com.hollingsworth.nuggets.common.util.RaycastHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlaceSchematicScreen extends BaseSchematicScreen {


    public RenderStructureHandler renderStructureHandler;

    public PlaceTool placeTool;
    public RotateTool rotateTool;
    public MoveHorizontalTool moveHorizontalTool;
    public MoveVerticalTool moveVerticalTool;
    public ConfirmTool confirmTool;
    public PrintTool printTool;
    public DeleteTool deleteTool;
    public MirrorTool mirrorTool;

    public PlaceSchematicScreen(String modId, KeyMapping focusKey, RenderStructureHandler<?> renderStructureHandler) {
        super(modId, focusKey, Component.literal("Tool Selection"));
        this.renderStructureHandler = renderStructureHandler;

        placeTool = new PlaceTool(() -> renderStructureHandler.placingData, this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_place.png"));
        rotateTool = new RotateTool(() -> renderStructureHandler.placingData,this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_rotate.png"));
        moveHorizontalTool = new MoveHorizontalTool(() -> renderStructureHandler.placingData,this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_horizontal.png"));
        moveVerticalTool = new MoveVerticalTool(() -> renderStructureHandler.placingData,this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_vertical.png"));
        confirmTool = new ConfirmTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_confirm.png"));
        printTool = new PrintTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_print.png"));
        deleteTool = new DeleteTool(this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_trash.png"));
        mirrorTool = new MirrorTool(() -> renderStructureHandler.placingData,this, ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/visualizer_icon_mirror.png"));

        tools.add(placeTool);
    }

    public void setupManipulationTools(){
        tools.clear();
        tools.add(moveHorizontalTool);
        tools.add(moveVerticalTool);
        tools.add(rotateTool);
        tools.add(confirmTool);
        if(renderStructureHandler.onPrint != null) {
            tools.add(printTool);
        }
        tools.add(deleteTool);
    }

    public static class DeleteTool extends ToolType<PlaceSchematicScreen> {
        public DeleteTool(PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.delete_tool"), icon, placeSchematicScreen);
        }

        @Override
        public void onClick() {
            placeSchematicScreen.renderStructureHandler.onDelete.accept(placeSchematicScreen.renderStructureHandler);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.delete_description"));
            return list;
        }
    }

    public static class MoveHorizontalTool extends ToolType {
        Supplier<StructureRenderData> getStructure;
        public MoveHorizontalTool(Supplier<StructureRenderData> getStructure, PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.move_horizontal_tool"), icon, placeSchematicScreen);
            this.getStructure = getStructure;
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            StructureRenderData structureData = getStructure.get();
            Direction direction = Minecraft.getInstance().player.getNearestViewDirection();
            BlockPos offset = new BlockPos((int) delta * direction.getStepX(), 0, (int) delta * direction.getStepZ());
            structureData.anchorPos = structureData.anchorPos.offset(offset);
            return true;
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.move_horizontal_description"));
            return list;
        }
    }

    public static class MoveVerticalTool extends ToolType {
        Supplier<StructureRenderData> getStructure;
        public MoveVerticalTool(Supplier<StructureRenderData> getStructure, PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.move_vertical_tool"), icon, placeSchematicScreen);
            this.getStructure = getStructure;
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            StructureRenderData structureData = getStructure.get();
            if(structureData == null || structureData.anchorPos == null){
                return false;
            }
            structureData.anchorPos = structureData.anchorPos.offset(new BlockPos(0, (int) delta, 0));
            return true;
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.move_vertical_description"));
            return list;
        }
    }

    public static class MirrorTool extends ToolType {
        Supplier<StructureRenderData> getStructure;
        public MirrorTool(Supplier<StructureRenderData> getStructure,PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.mirror_tool"), icon, placeSchematicScreen);
            this.getStructure = getStructure;
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            StructureRenderData structureData = getStructure.get();
            structureData.flip();
            structureData.lastRenderPos = null;
            return true;
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.mirror_description"));
            return list;
        }
    }


    public static class ConfirmTool extends ToolType<PlaceSchematicScreen>{

        public ConfirmTool(PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.confirm_tool"), icon, placeSchematicScreen);
        }

        @Override
        public void onClick() {
            placeSchematicScreen.renderStructureHandler.onConfirm.accept(placeSchematicScreen.renderStructureHandler);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.confirm_description"));
            return list;
        }
    }

    public static class PrintTool extends ToolType<PlaceSchematicScreen>{
        public PrintTool(PlaceSchematicScreen placeSchematicScreen , ResourceLocation icon) {
            super(Component.translatable("nuggets.print_tool"), icon, placeSchematicScreen);
        }

        @Override
        public void onClick() {
            placeSchematicScreen.renderStructureHandler.onPrint.accept(placeSchematicScreen.renderStructureHandler);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.print_description"));
            return list;
        }
    }


    public static class RotateTool extends ToolType{
        Supplier<StructureRenderData> getStructure;
        public RotateTool(Supplier<StructureRenderData> getStructure, PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.rotate_tool"), icon, placeSchematicScreen);
            this.getStructure = getStructure;
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.rotate_description"));
            return list;
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            StructureRenderData structureData = getStructure.get();
            if (structureData == null) {
                return false;
            }
            structureData.rotate(delta > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
            structureData.lastRenderPos = null;
            return true;
        }
    }

    public static class PlaceTool extends ToolType<PlaceSchematicScreen>{
        public Supplier<StructureRenderData> getStructure;

        public PlaceTool(Supplier<StructureRenderData> getStructure, PlaceSchematicScreen placeSchematicScreen, ResourceLocation icon) {
            super(Component.translatable("nuggets.place_tool"), icon, placeSchematicScreen);
            this.getStructure = getStructure;
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.place_description"));
            return list;
        }

        @Override
        public void onClick() {
            StructureRenderData structureData = getStructure.get();
            structureData.anchorPos = RaycastHelper.getLookingAt(structureData.distanceFromCameraCast, Minecraft.getInstance().player, true).getBlockPos();
            placeSchematicScreen.setupManipulationTools();
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            StructureRenderData structureData = getStructure.get();
            if(structureData == null){
                return false;
            }
            if(delta > 0){
                structureData.distanceFromCameraCast += 1;
            }else{
                structureData.distanceFromCameraCast -= 1;
            }
            return true;
        }
    }
}
