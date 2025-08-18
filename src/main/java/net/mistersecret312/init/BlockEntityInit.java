package net.mistersecret312.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.*;

public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RocketryScienceMod.MODID);

    public static final RegistryObject<BlockEntityType<LiquidRocketEngineBlockEntity>> ROCKET_ENGINE = BLOCK_ENTITIES.register("rocket_engine",
            () -> BlockEntityType.Builder.of(LiquidRocketEngineBlockEntity::new, BlockInit.STEEL_COMBUSTION_CHAMBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolidRocketBoosterBlockEntity>> SRB = BLOCK_ENTITIES.register("solid_rocket_booster",
            () -> BlockEntityType.Builder.of(SolidRocketBoosterBlockEntity::new, BlockInit.STEEL_NOZZLE_SOLID.get()).build(null));

    public static final RegistryObject<BlockEntityType<SolidFuelTankBlockEntity>> SOLID_FUEL_TANK = BLOCK_ENTITIES.register("solid_fuel_tank",
            () -> BlockEntityType.Builder.of(SolidFuelTankBlockEntity::new, BlockInit.COPPER_SOLID_FUEL_TANK.get()).build(null));
    public static final RegistryObject<BlockEntityType<FuelTankBlockEntity>> LIQUID_FUEL_TANK = BLOCK_ENTITIES.register("fuel_tank",
            () -> BlockEntityType.Builder.of(FuelTankBlockEntity::new, BlockInit.FUEL_TANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD = BLOCK_ENTITIES.register("launch_pad",
            () -> BlockEntityType.Builder.of(LaunchPadBlockEntity::new, BlockInit.LAUNCH_PAD.get()).build(null));
    public static final RegistryObject<BlockEntityType<LaunchTowerBlockEntity>> LAUNCH_TOWER = BLOCK_ENTITIES.register("launch_tower",
            () -> BlockEntityType.Builder.of(LaunchTowerBlockEntity::new, BlockInit.LAUNCH_TOWER.get()).build(null));


    public static void register(IEventBus bus)
    {
        BLOCK_ENTITIES.register(bus);
    }
}
