package com.hollingsworth.nuggets.client.rendering;


import com.hollingsworth.nuggets.common.util.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.Collectors;

public class StatePos {
    public static StreamCodec<RegistryFriendlyByteBuf, StatePos> STREAM = StreamCodec.ofMember((val, buf) ->{
        ByteBufCodecs.COMPOUND_TAG.encode(buf, val.tag);
        BlockPos.STREAM_CODEC.encode(buf, val.pos);
    }, (buf) -> new StatePos(ByteBufCodecs.COMPOUND_TAG.decode(buf), BlockPos.STREAM_CODEC.decode(buf)));

    public static StreamCodec<RegistryFriendlyByteBuf, List<StatePos>> STREAM_LIST = STREAM.apply(ByteBufCodecs.list());

    public BlockState state;
    public BlockPos pos;
    private CompoundTag tag;


    public StatePos(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public StatePos(CompoundTag tag, BlockPos pos){
        this.state = null;
        this.tag = tag;
        this.pos = pos;
    }

    public static List<BlockState> getBlockStateMap(List<StatePos> list) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (StatePos statePos : list) {
            if (!blockStateMap.contains(statePos.state))
                blockStateMap.add(statePos.state);
        }
        return blockStateMap;
    }

    public static List<StatePos> rotate(List<StatePos> list, List<TagPos> tagListMutable, Rotation rotation) {
        ArrayList<StatePos> rotatedList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return rotatedList;
        }
        boolean tags = !(tagListMutable == null || tagListMutable.isEmpty()); //If not empty or null, it has tags!

        Map<BlockPos, CompoundTag> tagMap = new HashMap<>();
        if (tags)
            tagMap = tagListMutable.stream().collect(Collectors.toMap(e -> e.pos, e -> e.tag));

        for (StatePos statePos : list) {
            BlockPos oldPos = statePos.pos;
            BlockState oldState = statePos.state;
            BlockState newState = oldState.rotate(rotation);
            BlockPos newPos = oldPos.rotate(rotation);

            if (tags && tagMap.get(statePos.pos) != null) {
                CompoundTag tempTag = tagMap.get(statePos.pos);
                tagMap.remove(statePos.pos);
                tagMap.put(newPos, tempTag);
            }

            rotatedList.add(new StatePos(newState, newPos));
        }

        if (tags) {
            tagListMutable.clear();
            for (Map.Entry<BlockPos, CompoundTag> entry : tagMap.entrySet())
                tagListMutable.add(new TagPos(entry.getValue(), entry.getKey()));
        }

        return rotatedList;
    }

    public static ListTag getBlockStateNBT(List<BlockState> blockStateMap) {
        ListTag listTag = new ListTag();
        for (BlockState blockState : blockStateMap) {
            listTag.add(NbtUtils.writeBlockState(blockState));
        }
        return listTag;
    }

    public static List<BlockState> getBlockStateMapFromNBT(ListTag listTag) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), listTag.getCompound(i));
            blockStateMap.add(blockState);
        }
        return blockStateMap;
    }

    public static Map<ItemStackKey, Integer> getItemList(ArrayList<StatePos> list) {
        Map<ItemStackKey, Integer> itemList = new Object2IntOpenHashMap<>();
        if (list == null || list.isEmpty())
            return itemList;
        for (StatePos statePos : list) {
            BlockPos blockPos = BlockPos.ZERO;
            Level level = Minecraft.getInstance().level;
            ItemStack cloneStack = statePos.state.getBlock().getCloneItemStack(level, blockPos, statePos.state);
            ItemStackKey itemStackKey = new ItemStackKey(cloneStack, true);
            if (!itemList.containsKey(itemStackKey)) //Todo Slabs, etc
                itemList.put(itemStackKey, 1);
            else
                itemList.put(itemStackKey, itemList.get(itemStackKey) + 1);
        }
        return itemList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePos) {
            return ((StatePos) obj).state.equals(this.state) && ((StatePos) obj).pos.equals(this.pos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, pos);
    }
}