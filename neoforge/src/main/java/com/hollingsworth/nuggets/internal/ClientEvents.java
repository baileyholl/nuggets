package com.hollingsworth.nuggets.internal;

import com.hollingsworth.nuggets.Constants;
import com.hollingsworth.nuggets.client.NuggetClientData;
import com.hollingsworth.nuggets.client.overlay.InWorldTooltip;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ClientEvents {
    public static final LayeredDraw.Layer OVERLAY = InWorldTooltip::renderOverlay;
    public static void clientTickEnd(ClientTickEvent.Post event) {
        NuggetClientData.ticksInGame++;
    }

    public static void renderWorldLastEvent(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            NuggetClientData.partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        }
    }

    public static void registerOverlays(final RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.prefix("in_world_tooltip"), OVERLAY);
    }
}
