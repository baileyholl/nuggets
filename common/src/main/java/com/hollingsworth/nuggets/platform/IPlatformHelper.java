package com.hollingsworth.nuggets.platform;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface IPlatformHelper {

    Iterable<RenderType> getRenderTypes(BakedModel bakedModel, BlockState blockState, RandomSource rand);
}
