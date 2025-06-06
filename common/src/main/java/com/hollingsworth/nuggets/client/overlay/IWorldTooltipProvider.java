package com.hollingsworth.nuggets.client.overlay;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWorldTooltipProvider {

    /**
     * A list of tool tips to render on the screen when looking at this target.
     */
    void getTooltip(List<Component> tooltip);

}
