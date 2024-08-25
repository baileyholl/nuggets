package src.main.java.com.hollingsworth.nuggets.common.entity;

import src.main.java.com.hollingsworth.nuggets.Nuggets;
import src.main.java.com.hollingsworth.nuggets.common.codec.NuggetCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> DS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Nuggets.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Vec3>> VEC3 = DS.register("vec3", () -> EntityDataSerializer.forValueType(NuggetCodecs.VEC_STREAM));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ResourceLocation>> RESOURCE_LOCATION = DS.register("resource_location", () -> EntityDataSerializer.forValueType(ResourceLocation.STREAM_CODEC));
}
