package net.mistersecret312.init;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.data_serializers.RocketDataSerializer;
import net.mistersecret312.util.rocket.Rocket;

public class EntityDataSerializersInit
{
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, RocketryScienceMod.MODID);

    public static final RegistryObject<EntityDataSerializer<Rocket>> ROCKET = SERIALIZERS.register("rocket", RocketDataSerializer::new);

    public static void register(IEventBus bus)
    {
        SERIALIZERS.register(bus);
    }
}
