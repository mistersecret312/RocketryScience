package net.mistersecret312.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, RocketryScienceMod.MODID);

    public static final RegistryObject<SoundEvent> ROCKET_ENGINE_RUN_LIQUID = registerSoundEvent("rocket_engine_run_liquid");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, sound)));
    }

    public static void register(IEventBus eventBus)
    {
        SOUNDS.register(eventBus);
    }
}
