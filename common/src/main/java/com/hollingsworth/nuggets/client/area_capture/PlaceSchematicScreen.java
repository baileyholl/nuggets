package com.hollingsworth.nuggets.client.area_capture;

import com.hollingsworth.nuggets.Constants;
import com.hollingsworth.nuggets.client.gui.GuiHelpers;
import com.hollingsworth.nuggets.common.util.RaycastHelper;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PlaceSchematicScreen extends Screen {

    public final String holdToFocus = "nuggets.gui.toolmenu.focusKey";

    public boolean focused;
    private float yOffset;
    protected int selection;
    private boolean initialized;

    protected int w;
    protected int h;
    public List<ToolType> tools = new ArrayList<>();
    public RenderStructureHandler renderStructureHandler;
    public BiFunction<Player, StructureRenderData, List<ToolType>> setupToolsFunc;
    public KeyMapping focusKey;

    public PlaceSchematicScreen(KeyMapping focusKey, RenderStructureHandler renderStructureHandler, StructureRenderData data, BiFunction<Player, StructureRenderData, List<ToolType>> setupToolsFunc) {
        super(Component.literal("Tool Selection"));
        this.focusKey = focusKey;
        this.renderStructureHandler = renderStructureHandler;
        this.minecraft = Minecraft.getInstance();
        focused = false;
        yOffset = 0;
        selection = 0;
        initialized = false;
        h = 34;
        tools.add(new PlaceTool(data, this));
        this.setupToolsFunc = setupToolsFunc;
    }

    public void setTools(List<ToolType> tools){
        this.tools = tools;
    }

    public void setSelectedElement(ToolType tool) {
        if (!tools.contains(tool))
            return;
        selection = tools.indexOf(tool);
    }

    public ToolType getSelectedElement(){
        return tools.get(selection);
    }

    public void cycle(int direction) {
        selection += (direction < 0) ? 1 : -1;
        selection = (selection + tools.size()) % tools.size();
    }

    private void draw(GuiGraphics graphics, float partialTicks) {
        w = Math.max(tools.size() * 50 + 32, 220);
        PoseStack matrixStack = graphics.pose();
        Window mainWindow = minecraft.getWindow();
        if (!initialized)
            init(minecraft, mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());

        int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 14;
        int y = mainWindow.getGuiScaledHeight() - h - 34;

        matrixStack.pushPose();
        matrixStack.translate(0, -yOffset, focused ? 100 : 0);

        graphics.blit(Constants.prefix("textures/gui/hud_background.png"), x - 15, y, 0, 0, w, h, 16, 16);

        float toolTipAlpha = yOffset / 10;
        List<Component> toolTip = tools.get(selection)
                .getDescription();

        if (toolTipAlpha > 0.25f) {
            graphics.blit(Constants.prefix("textures/gui/hud_background.png"), x - 15, y + 16, 0, 0, w, h, 16, 16);
            if (!toolTip.isEmpty())
                GuiHelpers.drawOutlinedText(minecraft.font, graphics, toolTip.get(0), x - 10, y + 38);
            if (toolTip.size() > 1)
                GuiHelpers.drawOutlinedText(minecraft.font, graphics, toolTip.get(1), x - 10, y + 50);
        }

        if (tools.size() > 1) {
            String keyName = focusKey.getTranslatedKeyMessage().getString();
            int width = minecraft.getWindow().getGuiScaledWidth();
            if (!focused)
                GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, Component.translatable(holdToFocus, keyName), width / 2, y - 10);
            else {
                GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, Component.translatable("nuggets.scroll"), width / 2, y - 10);
            }
        } else {
            GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, Component.translatable("nuggets.place_description"), width / 2, y - 10);
            x += 65;
        }


        for (int i = 0; i < tools.size(); i++) {
            matrixStack.pushPose();

            if (i == selection) {
                matrixStack.translate(0, -10, 0);

                GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, tools.get(i)
                        .getDisplayName(), x + i * 50 + 26, y + 28);
            }
            ResourceLocation icon = tools.get(i)
                    .getIcon();

            graphics.blit(icon,  x + i * 50 + 16, y + 11, 0, 0, 16, 16, 16, 16);

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    public void update() {
        if (focused)
            yOffset += (10 - yOffset) * .1f;
        else
            yOffset *= .9f;
    }

    public boolean scroll(double delta) {
        if(focused){
            cycle((int) delta);
            return true;
        }else if(hasControlDown()){
            return tools.get(selection)
                    .handleMouseWheel(delta);
        }
        return false;
    }

    public void renderPassive(GuiGraphics graphics, float partialTicks) {
        draw(graphics, partialTicks);
    }

    @Override
    public void onClose() {
       // callback.accept(tools.get(selection));
    }

    @Override
    protected void init() {
        super.init();
        initialized = true;
    }

    public static class DeleteTool extends ToolType {
        public Consumer<DeleteTool> onClickConsumer;
        public DeleteTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen, Consumer<DeleteTool> onClickConsumer) {
            super(Component.translatable("nuggets.delete_tool"), Constants.prefix("textures/gui/visualizer_trash.png"), structureData, placeSchematicScreen);
            this.onClickConsumer = onClickConsumer;
        }

        @Override
        public void onClick() {
            onClickConsumer.accept(this);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.delete_description"));
            return list;
        }
    }

    public static class MoveHorizontalTool extends ToolType {

        public MoveHorizontalTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen) {
            super(Component.translatable("nuggets.move_horizontal_tool"), Constants.prefix("textures/gui/visualizer_icon_horizontal.png"), structureData, placeSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
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

        public MoveVerticalTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen) {
            super(Component.translatable("nuggets.move_vertical_tool"), Constants.prefix("textures/gui/visualizer_icon_vertical.png"), structureData, placeSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
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

        public MirrorTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen) {
            super(Component.translatable("nuggets.mirror_tool"), Constants.prefix("textures/gui/visualizer_icon_mirror.png"), structureData, placeSchematicScreen);
        }

        @Override
        public boolean handleMouseWheel(double delta) {
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


    public static class ConfirmTool extends ToolType{
        public Consumer<ConfirmTool> onClickConsumer;

        public ConfirmTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen, Consumer<ConfirmTool> onClickConsumer) {
            super(Component.translatable("nuggets.confirm_tool"), Constants.prefix("textures/gui/visualizer_icon_confirm.png"), structureData, placeSchematicScreen);
            this.onClickConsumer = onClickConsumer;
        }

        @Override
        public void onClick() {
            onClickConsumer.accept(this);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.confirm_description"));
            return list;
        }
    }

    public static class PrintTool extends ToolType{
        public Consumer<PrintTool> onClickConsumer;
        public PrintTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen, Consumer<PrintTool> onClickConsumer) {
            super(Component.translatable("nuggets.print_tool"), Constants.prefix("textures/gui/visualizer_print.png"), structureData, placeSchematicScreen);
            this.onClickConsumer = onClickConsumer;
        }

        @Override
        public void onClick() {
            onClickConsumer.accept(this);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.print_description"));
            return list;
        }
    }


    public static class RotateTool extends ToolType{

        public RotateTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen) {
            super(Component.translatable("nuggets.rotate_tool"), Constants.prefix("textures/gui/visualizer_icon_rotate.png"), structureData, placeSchematicScreen);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.rotate_description"));
            return list;
        }

        @Override
        public boolean handleMouseWheel(double delta) {
            if (structureData == null) {
                return false;
            }
            structureData.rotate(delta > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
            structureData.lastRenderPos = null;
            return true;
        }
    }

    public static class PlaceTool extends ToolType{


        public PlaceTool(StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen) {
            super(Component.translatable("nuggets.place_tool"), Constants.prefix("textures/gui/visualizer_icon_place.png"), structureData, placeSchematicScreen);
        }

        @Override
        List<Component> getDescription() {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("nuggets.place_description"));
            return list;
        }

        @Override
        public void onClick() {
            structureData.anchorPos = RaycastHelper.getLookingAt(structureData.distanceFromCameraCast, Minecraft.getInstance().player, true).getBlockPos();
            placeSchematicScreen.setTools(placeSchematicScreen.setupToolsFunc.apply(Minecraft.getInstance().player, structureData));
        }

        @Override
        public boolean handleMouseWheel(double delta) {
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



    public abstract static class ToolType{

        public Component name;
        public ResourceLocation icon;
        public StructureRenderData structureData;
        public PlaceSchematicScreen placeSchematicScreen;

        public ToolType(Component name, ResourceLocation icon, StructureRenderData structureData, PlaceSchematicScreen placeSchematicScreen){
            this.name = name;
            this.icon = icon;
            this.structureData = structureData;
            this.placeSchematicScreen = placeSchematicScreen;
        }

        public void onClick(){

        }


        public Component getDisplayName(){
            return name;
        }

        abstract List<Component> getDescription();

        public ResourceLocation getIcon(){
            return icon;
        }

        public boolean handleMouseWheel(double delta){
            return false;
        }
    }

}
