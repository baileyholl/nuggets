package com.hollingsworth.nuggets.common.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MultiInsertReference extends MultiSlotReference<SlotReference>{
    private ItemStack remainder;

    public MultiInsertReference(ItemStack remainder, List<SlotReference> slots){
        super(slots);
        this.remainder = remainder;
    }

    public ItemStack getRemainder(){
        return remainder;
    }
}
