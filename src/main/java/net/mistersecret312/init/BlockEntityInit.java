package net.mistersecret312.init;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.RocketryScienceMod;

public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RocketryScienceMod.MODID);

    public static void register(IEventBus bus)
    {
        BLOCK_ENTITIES.register(bus);
    }
}
