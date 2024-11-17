package com.hollingsworth.nuggets.client.gui;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

@FunctionalInterface
public interface NestedWidgets {
    List<AbstractWidget> getExtras();
}

