package net.mistersecret312.init;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.util.rocket.BlockData;
import net.mistersecret312.util.rocket.FuelTankData;
import net.mistersecret312.util.rocket.RocketEngineData;
import net.mistersecret312.util.rocket.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RocketBlockDataInit
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(RocketryScienceMod.MODID, "rocket_block_data");

    public static final DeferredRegister<BlockData> ROCKET_DATAS = DeferredRegister.create(REGISTRY_NAME, RocketryScienceMod.MODID);
    public static final Supplier<IForgeRegistry<BlockData>> ROCKET_DATA = ROCKET_DATAS.makeRegistry(RegistryBuilder::new);

    public static final List<BiFunction<Stage, BlockPos, BlockData>> DATA_FACTORY = new ArrayList<>();

    public static final RegistryObject<BlockData> BASE = registerBlockData(BlockData::new, "base");
    public static final RegistryObject<BlockData> FUEL_TANK = registerBlockData(FuelTankData::new, "fuel_tank");
    public static final RegistryObject<BlockData> ROCKET_ENGINE = registerBlockData(RocketEngineData::new, "rocket_engine");

    public static RegistryObject<BlockData> registerBlockData(Supplier<BlockData> data, String id)
    {
        DATA_FACTORY.add(data.get().create());
        return ROCKET_DATAS.register(id, data);
    }

    public static void register(IEventBus bus)
    {
        ROCKET_DATAS.register(bus);
    }

}
