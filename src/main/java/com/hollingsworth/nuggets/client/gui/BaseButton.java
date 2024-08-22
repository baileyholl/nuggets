package com.hollingsworth.nuggets.client.gui;

import com.hollingsworth.nuggets.client.overlay.ITooltipProvider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BaseButton extends Button implements ITooltipProvider {

    public BaseButton(int x, int y, int w, int h, @NotNull Component text, OnPress onPress) {
        super(x, y, w, h, text, onPress, Button.DEFAULT_NARRATION);
    }

    public BaseButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    public BaseButton(Builder builder) {
        super(builder);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {

    }
}
