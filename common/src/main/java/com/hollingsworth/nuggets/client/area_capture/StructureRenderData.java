package com.hollingsworth.nuggets.client.area_capture;

import com.hollingsworth.nuggets.client.rendering.StatePos;
import com.hollingsworth.nuggets.mixin.StructureTemplateAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;

public class StructureRenderData {
    public ArrayList<StatePos> statePosCache;
    public BlockPos anchorPos;
    public BlockPos lastRenderPos;
    public double distanceFromCameraCast = 25;
    public StructureTemplate structureTemplate;
    public Rotation rotation;
    public Mirror mirror;
    public boolean flipped = false;
    public BoundingBox boundingBox;
    public StructurePlaceSettings structurePlaceSettings;

    public StructureRenderData(StructureTemplate template){
        var accessor = (StructureTemplateAccessor)structureTemplate;
        var palettes = accessor.getPalettes();
        if(palettes.isEmpty()){
            return;
        }
        var palette = palettes.get(0);
        statePosCache = new ArrayList<>();
        this.structureTemplate = template;
        for(StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()){
            statePosCache.add(new StatePos(blockInfo.state(), blockInfo.pos()));
        }
        structurePlaceSettings = new StructurePlaceSettings();
        boundingBox = structureTemplate.getBoundingBox(structurePlaceSettings, new BlockPos(0, 0, 0));
    }

    public void rotate(Rotation rotateBy){
        rotation = rotation.getRotated(rotateBy);
        statePosCache = StatePos.rotate(statePosCache, new ArrayList<>(), rotateBy);
        boundingBox = structureTemplate.getBoundingBox(structurePlaceSettings.setRotation(rotation), new BlockPos(0, 0, 0));
    }

    public void mirror(boolean mirror){
        this.mirror = mirror ? Mirror.FRONT_BACK : Mirror.NONE;

        boundingBox = structureTemplate.getBoundingBox(structurePlaceSettings.setMirror(this.mirror), new BlockPos(0, 0, 0));
    }

    public void flip(){
        flipped = !flipped;
        this.mirror(flipped);
    }

}
