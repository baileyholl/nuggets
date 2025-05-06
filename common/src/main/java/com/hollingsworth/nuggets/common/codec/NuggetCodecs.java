package com.hollingsworth.nuggets.common.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class NuggetCodecs {

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC_STREAM = StreamCodec.of(
            (pBuffer, pValue) -> {
                pBuffer.writeDouble(pValue.x);
                pBuffer.writeDouble(pValue.y);
                pBuffer.writeDouble(pValue.z);
            },
            pBuffer -> new Vec3(pBuffer.readDouble(), pBuffer.readDouble(), pBuffer.readDouble())
    );

    public static <T> Tag encode(HolderLookup.Provider provider, Codec<T> codec, T value){
        return codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), value).getOrThrow();
    }

    public static <T> Tag encode(Codec<T> codec, T value){
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    public static <T> T decode(Codec<T> codec, Tag tag){
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    public static <T> T decode(HolderLookup.Provider provider, Codec<T> codec, Tag tag){
        return codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }
}
