package src.main.java.com.hollingsworth.nuggets.internal;

import src.main.java.com.hollingsworth.nuggets.Nuggets;
import src.main.java.com.hollingsworth.nuggets.client.NuggetClientData;
import src.main.java.com.hollingsworth.nuggets.client.overlay.InWorldTooltip;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ClientEvents {

    public static void clientTickEnd(ClientTickEvent.Post event) {
        NuggetClientData.ticksInGame++;
    }

    public static void renderWorldLastEvent(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            NuggetClientData.partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        }
    }

    public static void registerOverlays(final RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, Nuggets.prefix("in_world_tooltip"), InWorldTooltip.OVERLAY);
    }
}
