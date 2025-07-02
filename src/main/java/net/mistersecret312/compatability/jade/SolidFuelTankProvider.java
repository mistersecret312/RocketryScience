package net.mistersecret312.compatability.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.SolidFuelTankBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SolidFuelTankProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    public static final SolidFuelTankProvider INSTANCE = new SolidFuelTankProvider();
    public static final ResourceLocation ID = new ResourceLocation(RocketryScienceMod.MODID, "solid_fuel_tank_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        if(accessor.getServerData().contains("capacity"))
        {
            int capacity = accessor.getServerData().getInt("capacity");
            int fuel = accessor.getServerData().getInt("fuel");

            tooltip.add(Component.translatable("data.rocketry_science.fuel", fuel, capacity));
        }
    }

    @Override
    public ResourceLocation getUid()
    {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor)
    {
        SolidFuelTankBlockEntity blockEntity = (SolidFuelTankBlockEntity) blockAccessor.getBlockEntity();
        if(blockEntity != null)
        {
            if (!blockEntity.isMaster() && blockEntity.getMasterRelativePosition() != BlockPos.ZERO)
            {
                blockEntity = blockEntity.getMaster();
            }
            if(blockEntity.isMaster())
            {
                tag.putInt("capacity", blockEntity.getFuelCapacity());
                tag.putInt("fuel", blockEntity.getFuelStored());
            }
        }
    }
}
