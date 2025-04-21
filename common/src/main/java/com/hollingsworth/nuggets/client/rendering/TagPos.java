package com.hollingsworth.nuggets.client.rendering;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class TagPos {
    public CompoundTag tag;
    public BlockPos pos;

    public TagPos(CompoundTag tag, BlockPos pos) {
        this.tag = tag;
        this.pos = pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagPos) {
            return ((TagPos) obj).tag.equals(this.tag) && ((TagPos) obj).pos.equals(this.pos);
        }
        return false;
    }
}