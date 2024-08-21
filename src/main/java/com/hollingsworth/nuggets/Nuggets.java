package com.hollingsworth.nuggets;

import com.hollingsworth.nuggets.common.event_queue.EventQueue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Nuggets.MODID)
public class Nuggets
{
    public static final String MODID = "nuggets";
    public static Logger logger = LogManager.getLogger(MODID);

    public Nuggets(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.addListener(EventQueue::serverTick);
        NeoForge.EVENT_BUS.addListener(EventQueue::clientTickEvent);
        NeoForge.EVENT_BUS.addListener(Nuggets::onServerStopped);
    }

    public static void onServerStopped(final ServerStoppingEvent event) {
        EventQueue.getClientQueue().clear();
        EventQueue.getServerInstance().clear();
    }
}
