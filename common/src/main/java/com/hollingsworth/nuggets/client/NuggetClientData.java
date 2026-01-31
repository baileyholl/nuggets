package com.hollingsworth.nuggets.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;

public class NuggetClientData {

    public static int ticksInGame = 0;
    public static float partialTicks = 0.0f;
    public static MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(256));
}
