package net.mistersecret312.init;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.fluids.LiquidMaterialFluid;
import org.joml.Vector3f;

public class FluidTypeInit
{
    public static final ResourceLocation LIQUID_STILL = new ResourceLocation("block/water_still");
    public static final ResourceLocation LIQUID_FLOW = new ResourceLocation("block/water_flow");
    public static final ResourceLocation LIQUID_OVERLAY = new ResourceLocation("block/water_overlay");
    public static final ResourceLocation LIQUID_UNDERWATER = new ResourceLocation("textures/misc/underwater.png");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, RocketryScienceMod.MODID);

    public static final RegistryObject<FluidType> LIQUID_HYDROGEN_TYPE = registerFluidType("liquid_hydrogen",
            new LiquidMaterialFluid(LIQUID_STILL, LIQUID_FLOW, LIQUID_OVERLAY, LIQUID_UNDERWATER, 0xFF99CCFF,
                    new Vector3f(153f / 255f, 204f / 255f, 1.0f),
                    FluidType.Properties.create().lightLevel(0).viscosity(8).density(15).canExtinguish(false)));

    public static final RegistryObject<FluidType> LIQUID_OXYGEN_TYPE = registerFluidType("liquid_oxygen",
            new LiquidMaterialFluid(LIQUID_STILL, LIQUID_FLOW, LIQUID_OVERLAY, LIQUID_UNDERWATER,0xFFFF6666,
                    new Vector3f(1.0f, 102f / 255f, 102f / 255f),

                    FluidType.Properties.create().lightLevel(0).viscosity(8).density(15).canExtinguish(false)));
    public static final RegistryObject<FluidType> LIQUID_NITROGEN_TYPE = registerFluidType("liquid_nitrogen",
            new LiquidMaterialFluid(LIQUID_STILL, LIQUID_FLOW, LIQUID_OVERLAY, LIQUID_UNDERWATER,0xFF66CC99,
                    new Vector3f(102 / 255f, 204f / 255f, 153f / 255f),
                    FluidType.Properties.create().lightLevel(0).viscosity(8).density(15).canExtinguish(false)));


    private static RegistryObject<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }

    public static void registerFluidInteractions()
    {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_HYDROGEN_TYPE.get(),
                        fluidstate -> Blocks.OBSIDIAN.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(ForgeMod.WATER_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_HYDROGEN_TYPE.get(),
                        fluidstate ->
                        {
                            if(fluidstate.isSource())
                                return Blocks.BLUE_ICE.defaultBlockState();
                            else return Blocks.PACKED_ICE.defaultBlockState();
                        }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_OXYGEN_TYPE.get(),
                        fluidstate -> Blocks.OBSIDIAN.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(ForgeMod.WATER_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_OXYGEN_TYPE.get(),
                        fluidstate ->
                        {
                            if(fluidstate.isSource())
                                return Blocks.BLUE_ICE.defaultBlockState();
                            else return Blocks.PACKED_ICE.defaultBlockState();
                        }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_NITROGEN_TYPE.get(),
                        fluidstate -> Blocks.OBSIDIAN.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(ForgeMod.WATER_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(LIQUID_NITROGEN_TYPE.get(),
                        fluidstate ->
                        {
                            if(fluidstate.isSource())
                                return Blocks.BLUE_ICE.defaultBlockState();
                            else return Blocks.PACKED_ICE.defaultBlockState();
                        }));

    }
}
