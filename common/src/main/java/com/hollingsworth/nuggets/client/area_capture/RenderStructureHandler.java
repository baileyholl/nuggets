package com.hollingsworth.nuggets.client.area_capture;


import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public class RenderStructureHandler<T extends StructureRenderData> {
    public T placingData;
    public PlaceSchematicScreen schematicTools;
    public Consumer<RenderStructureHandler<T>> onConfirm;
    public Consumer<RenderStructureHandler<T>> onDelete;
    public Consumer<RenderStructureHandler<T>> onPrint;

    public RenderStructureHandler(String modId, KeyMapping focusKey, T placingData,
                                  Consumer<RenderStructureHandler<T>> onConfirmed, Consumer<RenderStructureHandler<T>> onDeleted, @Nullable Consumer<RenderStructureHandler<T>> onPrint){
        this.placingData = placingData;
        schematicTools = new PlaceSchematicScreen(modId, focusKey, this);
        this.onDelete = onDeleted;
        this.onPrint = onPrint;
        this.onConfirm = onConfirmed;
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
