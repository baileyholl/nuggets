package com.hollingsworth.nuggets.platform;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public Iterable<RenderType> getRenderTypes(BakedModel bakedModel, BlockState blockState, RandomSource rand) {
        return bakedModel.getRenderTypes(blockState, rand, ModelData.EMPTY);
    }
}
