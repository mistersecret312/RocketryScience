package net.mistersecret312;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.init.*;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RocketryScienceMod.MODID)
public class RocketryScienceMod
{

    public static final String MODID = "rocketry_science";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RocketryScienceMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ItemInit.register(modEventBus);
        BlockInit.register(modEventBus);
        ItemTabInit.register(modEventBus);
        BlockEntityInit.register(modEventBus);
        FluidInit.register(modEventBus);
        FluidTypeInit.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        FluidTypeInit.registerFluidInteractions();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_LIQUID_HYDROGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_LIQUID_HYDROGEN.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_LIQUID_OXYGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_LIQUID_OXYGEN.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_LIQUID_NITROGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_LIQUID_NITROGEN.get(), RenderType.translucent());

        }
    }
}
