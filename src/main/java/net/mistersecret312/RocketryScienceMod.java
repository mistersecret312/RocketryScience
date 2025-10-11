package net.mistersecret312;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
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
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.client.entity.RocketRenderer;
import net.mistersecret312.client.model.PlumeModel;
import net.mistersecret312.client.renderer.FuelTankRenderer;
import net.mistersecret312.client.renderer.PlumeRenderer;
import net.mistersecret312.client.renderer.SeparatorRenderer;
import net.mistersecret312.client.renderer.SolidPlumeRenderer;
import net.mistersecret312.client.screen.CombustionChamberScreen;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.events.CommonEvents;
import net.mistersecret312.init.*;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.rocket.RocketEngineData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.swing.text.html.parser.Entity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RocketryScienceMod.MODID)
public class RocketryScienceMod
{
    public static final String MODID = "rocketry_science";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Fluid> OXYGEN = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation("forge", "oxygen"));
    public static final TagKey<Fluid> HYDROGEN = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation("forge", "hydrogen"));

    public RocketryScienceMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ItemInit.register(modEventBus);
        BlockInit.register(modEventBus);
        ItemTabInit.register(modEventBus);
        BlockEntityInit.register(modEventBus);
        FluidInit.register(modEventBus);
        FluidTypeInit.register(modEventBus);
        SoundInit.register(modEventBus);
        EntityInit.register(modEventBus);
        MenuInit.register(modEventBus);
        RocketBlockDataInit.register(modEventBus);
        EntityDataSerializersInit.register(modEventBus);

        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
                                {
                                    event.dataPackRegistry(CelestialBody.REGISTRY_KEY, CelestialBody.CODEC,
                                                           CelestialBody.CODEC);
                                });

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigInit.CLIENT_CONFIG, "rocketry_science-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            NetworkInit.registerPackets();
        });

        FluidTypeInit.registerFluidInteractions();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        MinecraftServer server = event.getServer();
        CommonEvents.init(server);

        Registry<CelestialBody> registry = server.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
        Set<Map.Entry<ResourceKey<CelestialBody>, CelestialBody>> set = registry.entrySet();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        public static PlumeModel plumeModel;

        @SubscribeEvent
        public static void bakeModels(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(PlumeModel.LAYER_LOCATION, PlumeModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event)
        {


            event.registerBlockEntityRenderer(BlockEntityInit.ROCKET_ENGINE.get(),
                    context ->
                    {
                        plumeModel = new PlumeModel(context.bakeLayer(PlumeModel.LAYER_LOCATION));
                        return new PlumeRenderer(plumeModel);
                    });
            event.registerBlockEntityRenderer(BlockEntityInit.SRB.get(),
                    context -> new SolidPlumeRenderer(plumeModel));
            event.registerBlockEntityRenderer(BlockEntityInit.LIQUID_FUEL_TANK.get(),
                    context -> new FuelTankRenderer());
            event.registerBlockEntityRenderer(BlockEntityInit.SEPARATOR.get(),
                    context -> new SeparatorRenderer());
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(EntityInit.ROCKET.get(), RocketRenderer::new);
            MenuScreens.register(MenuInit.COMBUSTION_CHAMBER.get(), CombustionChamberScreen::new);

            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_CRYOGENIC_HYDROGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_CRYOGENIC_HYDROGEN.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_CRYOGENIC_OXYGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_CRYOGENIC_OXYGEN.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(FluidInit.SOURCE_CRYOGENIC_NITROGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.FLOWING_CRYOGENIC_NITROGEN.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BlockInit.STEEL_COMBUSTION_CHAMBER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.STEEL_NOZZLE_ATMOPSHERE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.STEEL_NOZZLE_VACUUM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.STEEL_NOZZLE_SOLID.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.FUEL_TANK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.LAUNCH_TOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.SEPARATOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.STEEL_ROCKET_ENGINE_STUB.get(), RenderType.cutout());
        }
    }
}
