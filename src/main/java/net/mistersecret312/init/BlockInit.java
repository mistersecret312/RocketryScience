package net.mistersecret312.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.blocks.*;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static net.mistersecret312.blocks.NozzleBlock.ACTIVE;

public class BlockInit
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RocketryScienceMod.MODID);

    public static final RegistryObject<LiquidBlock> LIQUID_HYDROGEN = BLOCKS.register("liquid_hydrogen",
            () -> new LiquidBlock(FluidInit.SOURCE_LIQUID_HYDROGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));
    public static final RegistryObject<LiquidBlock> LIQUID_OXYGEN = BLOCKS.register("liquid_oxygen",
            () -> new LiquidBlock(FluidInit.SOURCE_LIQUID_OXYGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));
    public static final RegistryObject<LiquidBlock> LIQUID_NITROGEN = BLOCKS.register("liquid_nitrogen",
            () -> new LiquidBlock(FluidInit.SOURCE_LIQUID_NITROGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<CombustionChamberBlock> STEEL_COMBUSTION_CHAMBER = registerBlock("steel_combustion_chamber",
            () -> new CombustionChamberBlock(BlockBehaviour.Properties.of().noOcclusion().strength(15).explosionResistance(15).sound(SoundType.COPPER)));
    public static final RegistryObject<NozzleBlock> STEEL_NOZZLE_ATMOPSHERE = registerBlock("steel_nozzle_atmosphere",
            () -> new NozzleBlock(BlockBehaviour.Properties.of().noOcclusion().explosionResistance(10).explosionResistance(10).sound(SoundType.COPPER).lightLevel(
                     nozzleBlockEmission(15)),
                    false, true));
    public static final RegistryObject<NozzleBlock> STEEL_NOZZLE_VACUUM = registerBlock("steel_nozzle_vacuum",
            () -> new NozzleBlock(BlockBehaviour.Properties.of().noOcclusion().explosionResistance(10).explosionResistance(10).sound(SoundType.COPPER),
                    true, true));
    public static final RegistryObject<SolidRocketBoosterNozzleBlock> STEEL_NOZZLE_SOLID = registerBlock("steel_nozzle_solid",
            () -> new SolidRocketBoosterNozzleBlock(BlockBehaviour.Properties.of().noOcclusion().explosionResistance(10).explosionResistance(10).sound(SoundType.COPPER).lightLevel(
                    nozzleBlockEmission(15))));

    public static final RegistryObject<SolidFuelTankBlock> COPPER_SOLID_FUEL_TANK = registerBlock("copper_solid_fuel_tank",
            () -> new SolidFuelTankBlock(BlockBehaviour.Properties.of().explosionResistance(5).strength(5), 100));
    public static final RegistryObject<FuelTankBlock> LOW_PRESSURE_FUEL_TANK = registerBlock("low_pressure_fuel_tank",
            () -> new FuelTankBlock(BlockBehaviour.Properties.of().explosionResistance(5).strength(5), 2000));

    public static final RegistryObject<LaunchPadBlock> LAUNCH_PAD = registerBlock("launch_pad",
            () -> new LaunchPadBlock(BlockBehaviour.Properties.of()));
    public static final RegistryObject<LaunchTowerBlock> LAUNCH_TOWER = registerBlock("launch_tower",
            () -> new LaunchTowerBlock(BlockBehaviour.Properties.of()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block)
    {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }

    private static ToIntFunction<BlockState> nozzleBlockEmission(int pLightValue) {
        return (state) -> state.getValue(ACTIVE) ? pLightValue : 0;
    }
}
