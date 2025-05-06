package com.hollingsworth.nuggets.common.inventory;


import com.hollingsworth.nuggets.Nuggets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class FilterSet {

    /**
     * Returns the highest preference for a given item.
     * Invalid overrules all other preferences, as the user does NOT want that item to be inserted.
     */
    public abstract SortPref getHighestPreference(ItemStack stack);

    public static FilterSet forPosition(Level level, BlockPos pos){
        List<Function<ItemStack, SortPref>> filters = new ArrayList<>();
        IItemHandler inv = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if(inv == null){
            return new ListSet(filters);
        }
        IFiltersetProvider filtersetProvider = level.getCapability(Nuggets.FILTERSET_CAPABILITY, pos, null);
        if(filtersetProvider != null){
            return filtersetProvider.getFilterSet();
        }

        for (ItemFrame i : level.getEntitiesOfClass(ItemFrame.class, new AABB(pos).inflate(1))) {
            BlockPos attachedTo = i.blockPosition().relative(i.getDirection().getOpposite());
            if(!attachedTo.equals(pos)){
                continue;
            }
            ItemStack stackInFrame = i.getItem();
            if (i.getItem().isEmpty() || stackInFrame.isEmpty()) {
                continue;
            }

            if (stackInFrame.getItem() instanceof IFilterItem scrollItem) {
                filters.add(stackToStore -> scrollItem.getSortPref(stackToStore, stackInFrame, inv));
            } else {
                filters.add(stackToStore -> stackToStore.getItem() == stackInFrame.getItem() ? SortPref.HIGHEST : SortPref.INVALID);
            }
        }

        return new ListSet(filters);
    }

    public static class ListSet extends FilterSet{
        public List<Function<ItemStack, SortPref>> filters;

        public ListSet(){
            this.filters = new ArrayList<>();
        }

        public ListSet(List<Function<ItemStack, SortPref>> filters){
            super();
            this.filters = filters;
        }

        public boolean addFilterScroll(ItemStack scrollStack, IItemHandler itemHandler){
            if(scrollStack.getItem() instanceof IFilterItem itemScroll){
                return filters.add(stackIn -> itemScroll.getSortPref(stackIn, scrollStack, itemHandler));
            }
            return false;
        }

        /**
         * Returns the highest preference from a list of predicates, unless it is invalid.
         * Invalid overrules all other preferences, as the user does NOT want that item to be inserted.
         */
        public SortPref getHighestPreference(ItemStack stack){
            SortPref pref = SortPref.LOW;
            for(Function<ItemStack, SortPref> filter : filters){
                SortPref newPref = filter.apply(stack);
                if(newPref == SortPref.INVALID){
                    return SortPref.INVALID;
                }else if(newPref.ordinal() > pref.ordinal()){
                    pref = newPref;
                }
            }
            return pref;
        }
    }

    public static class Composite extends FilterSet{
        public List<FilterSet> filterSets;

        public Composite(List<FilterSet> filterSets){
            this.filterSets = filterSets;
        }

        public Composite(){
            this.filterSets = new ArrayList<>();
        }

        public Composite withFilter(FilterSet filterSet){
            this.filterSets.add(filterSet);
            return this;
        }

        /**
         * Returns the highest preference from a list of predicates, unless it is invalid.
         * Invalid overrules all other preferences, as the user does NOT want that item to be inserted.
         */
        public SortPref getHighestPreference(ItemStack stack){
            SortPref pref = SortPref.LOW;
            for(FilterSet filterSet : filterSets){
                SortPref newPref = filterSet.getHighestPreference(stack);
                if(newPref == SortPref.INVALID){
                    return SortPref.INVALID;
                }else if(newPref.ordinal() > pref.ordinal()){
                    pref = newPref;
                }
            }
            return pref;
        }
    }
}
