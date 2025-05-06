package com.hollingsworth.nuggets.common.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MultiExtractedReference extends MultiSlotReference<ExtractedStack>{
    protected ItemStack extracted;

    public MultiExtractedReference(ItemStack extracted, List<ExtractedStack> slots){
        super(slots);
        this.extracted = extracted;
    }

    public ItemStack getExtracted(){
        return extracted;
    }

    public boolean isEmpty(){
        return extracted.isEmpty();
    }
}
