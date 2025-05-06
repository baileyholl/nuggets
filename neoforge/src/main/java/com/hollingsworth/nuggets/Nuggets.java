package com.hollingsworth.nuggets;

import com.hollingsworth.nuggets.common.entity.DataSerializers;
import com.hollingsworth.nuggets.common.event_queue.EventQueue;
import com.hollingsworth.nuggets.common.inventory.IFiltersetProvider;
import com.hollingsworth.nuggets.internal.ClientEvents;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Constants.MOD_ID)
public class Nuggets
{
    public static Logger logger = LogManager.getLogger(Constants.MOD_ID);
    public static final BlockCapability<IFiltersetProvider, Direction> FILTERSET_CAPABILITY = BlockCapability.createSided(prefix("filterset"), IFiltersetProvider.class);


    public Nuggets(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.addListener(EventQueue::serverTick);
        NeoForge.EVENT_BUS.addListener(EventQueue::clientTickEvent);
        NeoForge.EVENT_BUS.addListener(Nuggets::onServerStopped);
        NeoForge.EVENT_BUS.addListener(ClientEvents::renderWorldLastEvent);
        NeoForge.EVENT_BUS.addListener(ClientEvents::clientTickEnd);
        NeoForge.EVENT_BUS.addListener(ClientEvents::updateInputEvent);
        modEventBus.addListener(ClientEvents::registerOverlays);
        modEventBus.addListener(ClientEvents::clientSetup);
        DataSerializers.DS.register(modEventBus);
    }

    private static void onServerStopped(final ServerStoppingEvent event) {
        EventQueue.getClientQueue().clear();
        EventQueue.getServerInstance().clear();
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
