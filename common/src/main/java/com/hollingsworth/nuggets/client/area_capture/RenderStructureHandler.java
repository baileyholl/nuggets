package com.hollingsworth.nuggets.client.area_capture;


import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiFunction;


public class RenderStructureHandler {
    private StructureRenderData placingData;
    private PlaceSchematicScreen schematicTools;

    public RenderStructureHandler(KeyMapping focusKey, StructureRenderData placingData, BiFunction<Player, StructureRenderData, List<PlaceSchematicScreen.ToolType>> setupTools){
        this.placingData = placingData;
        schematicTools = new PlaceSchematicScreen(focusKey, this, placingData, setupTools);
    }
    // Client tick event
    public void tick(){
        if(placingData == null){
            return;
        }
        schematicTools.update();
    }

    // Right click event
    public void rightClickEvent() {
        if (placingData == null) {
            return;
        }
        schematicTools.getSelectedElement().onClick();
    }

    // Bound tool key pressed event
    public void toolKeyHit(boolean keyPressed){
        if (placingData == null) {
            return;
        }
        if (keyPressed && !schematicTools.focused)
            schematicTools.focused = true;
        if (!keyPressed && schematicTools.focused) {
            schematicTools.focused = false;
            schematicTools.onClose();
        }
    }

    public boolean mouseScrolled(double delta) {
        if (placingData == null) {
            return false;
        }
        return schematicTools.scroll(delta);
    }

    // RenderGuiLayerEvent.Post event
    public void renderInstructions(GuiGraphics graphics, Window window) {
        if (placingData == null)
            return;
        schematicTools.renderPassive(graphics, 0);
    }
}
