package com.hollingsworth.nuggets.common.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public interface IFilterItem {
    SortPref getSortPref(ItemStack stackToStore, ItemStack scrollStack, IItemHandler inventory);
}
