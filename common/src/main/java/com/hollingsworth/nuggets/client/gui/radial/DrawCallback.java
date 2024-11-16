package com.hollingsworth.nuggets.client.gui.radial;

import net.minecraft.client.gui.GuiGraphics;

public interface DrawCallback<T> {
    void accept(T objectToBeDrawn, GuiGraphics poseStack, int positionX, int positionY, int size, boolean renderTransparent);
}
