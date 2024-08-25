package com.hollingsworth.nuggets.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class NuggetImageButton extends BaseButton {

    public ResourceLocation image;
    public int u, v, image_width, image_height;
    public Component toolTip;
    public boolean soundDisabled = false;

    public NuggetImageButton(int x, int y, int w, int h, ResourceLocation image, Button.OnPress onPress) {
        this(x, y, 0, 0, w, h, w, h, image, onPress);
    }


    public NuggetImageButton(int x, int y, int u, int v, int w, int h, int image_width, int image_height, ResourceLocation image, Button.OnPress onPress) {
        super(x, y, w, h, Component.empty(), onPress);
        this.u = u;
        this.v = v;
        this.image_height = image_height;
        this.image_width = image_width;
        this.image = image;
    }

    public NuggetImageButton withTooltip(Component toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.blit(image, getX(), getY(), u, v, width, height, image_width, image_height);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        if (toolTip != null)
            tooltip.add(toolTip);
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
        if (soundDisabled)
            return;
        super.playDownSound(pHandler);
    }
}