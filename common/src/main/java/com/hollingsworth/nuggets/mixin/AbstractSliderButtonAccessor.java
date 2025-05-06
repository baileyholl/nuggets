package com.hollingsworth.nuggets.mixin;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonAccessor {
    @Invoker
    ResourceLocation callGetSprite();

    @Invoker
    ResourceLocation callGetHandleSprite();
}
