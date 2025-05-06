package com.hollingsworth.nuggets.client.gui;

import com.hollingsworth.nuggets.client.BlitInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class VerticalSlider extends BaseSlider {
    public Consumer<Integer> onChange;
    public BlitInfo scrollBar;
    public BlitInfo scrollContainer;

    public VerticalSlider(int x, int y, int width, int height, BlitInfo scrollBar, BlitInfo scrollContainer, double maxValue, double stepSize, int precision, Consumer<Integer> onChange) {
        super(x, y, width, height, Component.empty(), Component.empty(), 0, maxValue, 0, stepSize, precision, false);
        this.onChange = onChange;
        this.scrollBar = scrollBar;
        this.scrollContainer = scrollContainer;
    }

    public VerticalSlider(int x, int y, int width, int height, BlitInfo scrollBar, BlitInfo scrollContainer, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
        this.scrollBar = scrollBar;
        this.scrollContainer = scrollContainer;
    }

    @Override
    protected void applyValue() {
        super.applyValue();
        onChange.accept(this.getValueInt());
    }


    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiHelpers.blit(guiGraphics, scrollContainer, getX(), getY());
        GuiHelpers.blit(guiGraphics, scrollBar, getX() + 3, getY() + (int) (this.value * (double) (this.height - 3)));
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if(pScrollY != 0){
            this.setValue(this.getValue() + (pScrollY > 0 ? -1 : 1) * stepSize);
            applyValue();
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean flag = keyCode == GLFW.GLFW_KEY_LEFT;
        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue)
                flag = !flag;
            float f = flag ? -1F : 1F;
            if (stepSize <= 0D)
                this.setSliderValue(this.value + (f / (this.height - 8)));
            else
                this.setValue(this.getValue() + f * this.stepSize);
        }

        return false;
    }

    @Override
    public void setValueFromMouse(double mouseX, double mouseY) {
        this.setSliderValue((mouseY - (this.getY() + 4)) / (this.height - 8));
    }
}
