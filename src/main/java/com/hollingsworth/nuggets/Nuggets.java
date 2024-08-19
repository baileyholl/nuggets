package com.hollingsworth.nuggets;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nuggets.MODID)
public class Nuggets
{
    public static final String MODID = "nuggets";
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Nuggets(IEventBus modEventBus, ModContainer modContainer)
    {
    }
}
