package src.main.java.com.hollingsworth.nuggets.common.codec;

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
}
