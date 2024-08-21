package com.hollingsworth.nuggets.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class AABBHelpers {
    public static double boxDistance(AABB box1, AABB box2) {
        // Calculate the minimum distance between the two axis aligned bounding boxes
        double x = Math.max(0, Math.max(box1.minX - box2.maxX, box2.minX - box1.maxX));
        double y = Math.max(0, Math.max(box1.minY - box2.maxY, box2.minY - box1.maxY));
        double z = Math.max(0, Math.max(box1.minZ - box2.maxZ, box2.minZ - box1.maxZ));
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Iterable<BlockPos> iterateAABB(@Nullable AABB pAabb){
        if(pAabb == null)
            return List.of();
        return BlockPos.betweenClosed(Mth.floor(pAabb.minX), Mth.floor(pAabb.minY), Mth.floor(pAabb.minZ), Mth.floor(pAabb.maxX), Mth.floor(pAabb.maxY), Mth.floor(pAabb.maxZ));
    }
}
