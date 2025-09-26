package net.mistersecret312.init;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.util.rocket.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RocketBlockDataInit
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(RocketryScienceMod.MODID, "rocket_block_data");

    public static final DeferredRegister<BlockDataType<?>> ROCKET_DATAS = DeferredRegister.create(REGISTRY_NAME, RocketryScienceMod.MODID);
    public static final Supplier<IForgeRegistry<BlockDataType<?>>> ROCKET_DATA = ROCKET_DATAS.makeRegistry(RegistryBuilder::new);

    public static final List<BiFunction<Stage, BlockPos, BlockData>> DATA_FACTORY = new ArrayList<>();
    public static final HashMap<BlockDataType<?>, ResourceLocation> CLASSES = new HashMap<>();

    public static final RegistryObject<BlockDataType<FuelTankData>> FUEL_TANK = registerBlockData(FuelTankData::new, "fuel_tank");
    public static final RegistryObject<BlockDataType<RocketEngineData>> ROCKET_ENGINE = registerBlockData(RocketEngineData::new, "rocket_engine");
    public static final RegistryObject<BlockDataType<SeparatorData>> SEPARATOR = registerBlockData(SeparatorData::new, "separator");

    public static final RegistryObject<BlockDataType<BlockData>> BASE = registerBlockData(BlockData::new, "base");

    public static <T extends BlockData> RegistryObject<BlockDataType<T>> registerBlockData(Supplier<T> data, String id)
    {
        BlockDataType<T> type = new BlockDataType<>(data, id);
        RegistryObject<BlockDataType<T>> object = ROCKET_DATAS.register(id, () -> type);
        DATA_FACTORY.add(data.get().create());
        CLASSES.put(type, object.getId());
        return object;
    }

    public static void register(IEventBus bus)
    {
        ROCKET_DATAS.register(bus);
    }

}
