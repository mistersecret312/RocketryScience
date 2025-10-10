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

    public static final RegistryObject<FlowingFluid> SOURCE_CRYOGENIC_HYDROGEN = FLUIDS.register("cryogenic_hydrogen",
            () -> new ForgeFlowingFluid.Source(FluidInit.CRYOGENIC_HYDROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_CRYOGENIC_HYDROGEN = FLUIDS.register("flowing_cryogenic_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.CRYOGENIC_HYDROGEN_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_CRYOGENIC_OXYGEN = FLUIDS.register("cryogenic_oxygen",
            () -> new ForgeFlowingFluid.Source(FluidInit.CRYOGENIC_OXYGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_CRYOGENIC_OXYGEN = FLUIDS.register("flowing_cryogenic_oxygen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.CRYOGENIC_OXYGEN_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_CRYOGENIC_NITROGEN = FLUIDS.register("cryogenic_nitrogen",
            () -> new ForgeFlowingFluid.Source(FluidInit.CRYOGENIC_NITROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_CRYOGENIC_NITROGEN = FLUIDS.register("flowing_cryogenic_nitrogen",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.CRYOGENIC_NITROGEN_PROPERTIES));


    public static final ForgeFlowingFluid.Properties CRYOGENIC_HYDROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.CRYOGENIC_HYDROGEN_TYPE, SOURCE_CRYOGENIC_HYDROGEN, FLOWING_CRYOGENIC_HYDROGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.CRYOGENIC_HYDROGEN)
            .bucket(ItemInit.CRYOGENIC_HYDROGEN_BUCKET);

    public static final ForgeFlowingFluid.Properties CRYOGENIC_OXYGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.CRYOGENIC_OXYGEN_TYPE, SOURCE_CRYOGENIC_OXYGEN, FLOWING_CRYOGENIC_OXYGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.CRYOGENIC_OXYGEN)
            .bucket(ItemInit.CRYOGENIC_OXYGEN_BUCKET);

    public static final ForgeFlowingFluid.Properties CRYOGENIC_NITROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeInit.CRYOGENIC_NITROGEN_TYPE, SOURCE_CRYOGENIC_NITROGEN, FLOWING_CRYOGENIC_NITROGEN)
            .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.CRYOGENIC_NITROGEN)
            .bucket(ItemInit.CRYOGENIC_NITROGEN_BUCKET);

    public static void register(IEventBus bus)
    {
        FLUIDS.register(bus);
    }
}
