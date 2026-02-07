package com.hollingsworth.nuggets.client.area_capture;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class ToolType<T extends BaseSchematicScreen> {
    public Component name;
    public ResourceLocation icon;
    public T placeSchematicScreen;

    public ToolType(Component name, ResourceLocation icon, T placeSchematicScreen){
        this.name = name;
        this.icon = icon;
        this.placeSchematicScreen = placeSchematicScreen;
    }

    public void onClick(){
    }


    public Component getDisplayName(){
        return name;
    }

    abstract List<Component> getDescription();

    public ResourceLocation getIcon(){
        return icon;
    }

    public boolean handleMouseWheel(double delta){
        return false;
    }
}
