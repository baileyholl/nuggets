package com.hollingsworth.nuggets.platform;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public Iterable<RenderType> getRenderTypes(BakedModel bakedModel, BlockState blockState, RandomSource rand) {
        return List.of(ItemBlockRenderTypes.getChunkRenderType(blockState));
    }
}
