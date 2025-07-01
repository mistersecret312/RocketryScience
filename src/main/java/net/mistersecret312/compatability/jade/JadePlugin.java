package net.mistersecret312.compatability.jade;

import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.block_entities.SolidFuelTankBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.SolidFuelTankBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin
{
    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(RocketEngineProvider.INSTANCE, RocketEngineBlockEntity.class);
        registration.registerBlockDataProvider(SolidFuelTankProvider.INSTANCE, SolidFuelTankBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerBlockComponent(RocketEngineProvider.INSTANCE, CombustionChamberBlock.class);
        registration.registerBlockComponent(SolidFuelTankProvider.INSTANCE, SolidFuelTankBlock.class);
    }
}
