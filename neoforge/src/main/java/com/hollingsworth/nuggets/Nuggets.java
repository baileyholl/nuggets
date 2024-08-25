package com.hollingsworth.nuggets;

import com.hollingsworth.nuggets.client.overlay.InWorldTooltip;
import com.hollingsworth.nuggets.common.entity.DataSerializers;
import com.hollingsworth.nuggets.common.event_queue.EventQueue;
import com.hollingsworth.nuggets.internal.ClientEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Constants.MOD_ID)
public class Nuggets
{
    public static Logger logger = LogManager.getLogger(Constants.MOD_ID);

    public Nuggets(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.addListener(EventQueue::serverTick);
        NeoForge.EVENT_BUS.addListener(EventQueue::clientTickEvent);
        NeoForge.EVENT_BUS.addListener(Nuggets::onServerStopped);
        NeoForge.EVENT_BUS.addListener(ClientEvents::renderWorldLastEvent);
        NeoForge.EVENT_BUS.addListener(ClientEvents::clientTickEnd);
        modEventBus.addListener(ClientEvents::registerOverlays);
        DataSerializers.DS.register(modEventBus);
        if(!FMLEnvironment.production){
            InWorldTooltip.ENTITY_CALLBACKS.put(EntityType.CREEPER, (entity, stack) -> {
                stack.add(Component.literal("Nuggets test tooltip"));
            });
        }
    }

    public static void onServerStopped(final ServerStoppingEvent event) {
        EventQueue.getClientQueue().clear();
        EventQueue.getServerInstance().clear();
    }
}
