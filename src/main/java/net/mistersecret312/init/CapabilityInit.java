package net.mistersecret312.init;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.capabilities.BlueprintDataCapability;

@Mod.EventBusSubscriber(modid = RocketryScienceMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityInit
{
    public static final Capability<BlueprintDataCapability> BLUEPRINTS_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(BlueprintDataCapability.class);
    }

}
