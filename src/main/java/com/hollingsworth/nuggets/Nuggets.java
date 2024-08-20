package com.hollingsworth.nuggets;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Nuggets.MODID)
public class Nuggets
{
    public static final String MODID = "nuggets";
    public static Logger logger = LogManager.getLogger(MODID);

    public Nuggets(IEventBus modEventBus, ModContainer modContainer)
    {
    }
}
