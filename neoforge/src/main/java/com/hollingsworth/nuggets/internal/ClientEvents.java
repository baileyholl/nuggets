package com.hollingsworth.nuggets.internal;

import com.hollingsworth.nuggets.Constants;
import com.hollingsworth.nuggets.client.NuggetClientData;
import com.hollingsworth.nuggets.client.gui.radial.GuiRadialMenu;
import com.hollingsworth.nuggets.client.overlay.InWorldTooltip;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
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

    public static void updateInputEvent(final MovementInputUpdateEvent event){
        GuiRadialMenu.updateInputEvent(event.getInput());
    }

    public static void clientSetup(final FMLClientSetupEvent event) {
        if(!FMLEnvironment.production){
            InWorldTooltip.registerEntityCallback(EntityType.CREEPER, (entity, stack) -> {
                stack.add(Component.literal("Nuggets test tooltip"));
            });
        }
    }
}
