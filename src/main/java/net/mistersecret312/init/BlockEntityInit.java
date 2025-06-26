package net.mistersecret312.init;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;

public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RocketryScienceMod.MODID);

    public static final RegistryObject<BlockEntityType<RocketEngineBlockEntity>> ROCKET_ENGINE = BLOCK_ENTITIES.register("rocket_engine",
            () -> BlockEntityType.Builder.of(RocketEngineBlockEntity::new, BlockInit.STEEL_COMBUSTION_CHAMBER.get()).build(null));

    public static void register(IEventBus bus)
    {
        BLOCK_ENTITIES.register(bus);
    }
}
