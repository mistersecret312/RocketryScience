package net.mistersecret312.init;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;

public class FluidInit
{
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, RocketryScienceMod.MODID);

    public static final RegistryObject<FlowingFluid> SOURCE_LIQUID_HYDROGEN = FLUIDS.register("liquid_hydrogen",
            () -> new ForgeFlowingFluid.Source(FluidInit.LIQUID_HYDROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_LIQUID_HYDROGEN = FLUIDS.register("flowing_liquid_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.LIQUID_HYDROGEN_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_LIQUID_OXYGEN = FLUIDS.register("liquid_oxygen",
            () -> new ForgeFlowingFluid.Source(FluidInit.LIQUID_OXYGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_LIQUID_OXYGEN = FLUIDS.register("flowing_liquid_oxygen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.LIQUID_OXYGEN_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_LIQUID_NITROGEN = FLUIDS.register("liquid_nitrogen",
            () -> new ForgeFlowingFluid.Source(FluidInit.LIQUID_NITROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_LIQUID_NITROGEN = FLUIDS.register("flowing_liquid_nitrogen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.LIQUID_NITROGEN_PROPERTIES));


    public static final ForgeFlowingFluid.Properties LIQUID_HYDROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.LIQUID_HYDROGEN_TYPE, SOURCE_LIQUID_HYDROGEN, FLOWING_LIQUID_HYDROGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.LIQUID_HYDROGEN)
            .bucket(ItemInit.LIQUID_HYDROGEN_BUCKET);

    public static final ForgeFlowingFluid.Properties LIQUID_OXYGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.LIQUID_OXYGEN_TYPE, SOURCE_LIQUID_OXYGEN, FLOWING_LIQUID_OXYGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.LIQUID_OXYGEN)
            .bucket(ItemInit.LIQUID_OXYGEN_BUCKET);

    public static final ForgeFlowingFluid.Properties LIQUID_NITROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.LIQUID_NITROGEN_TYPE, SOURCE_LIQUID_NITROGEN, FLOWING_LIQUID_NITROGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.LIQUID_NITROGEN)
            .bucket(ItemInit.LIQUID_NITROGEN_BUCKET);

    public static void register(IEventBus bus)
    {
        FLUIDS.register(bus);
    }
}
