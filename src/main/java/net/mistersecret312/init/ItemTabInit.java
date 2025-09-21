package net.mistersecret312.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.items.CombustionChamberItem;
import net.mistersecret312.items.TurboPumpItem;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;

public class ItemTabInit
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RocketryScienceMod.MODID);

    public static final RegistryObject<CreativeModeTab> ROCKET_ENGINE_PARTS = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(() -> BlockInit.STEEL_COMBUSTION_CHAMBER.get().asItem().getDefaultInstance())
                    .title(Component.translatable("tabs.rocketry_science.main"))
                    .displayItems((parameters, output) ->
                    {
                        output.accept(BlockInit.STEEL_COMBUSTION_CHAMBER.get());
                        output.accept(BlockInit.STEEL_NOZZLE_ATMOPSHERE.get());
                        output.accept(BlockInit.STEEL_NOZZLE_VACUUM.get());
                        output.accept(BlockInit.STEEL_NOZZLE_SOLID.get());
                        output.accept(BlockInit.STEEL_ROCKET_ENGINE_STUB.get());

                        output.accept(BlockInit.COPPER_SOLID_FUEL_TANK.get());
                        output.accept(ItemInit.FUEL_TANK.get());

                        output.accept(ItemInit.LIQUID_HYDROGEN_BUCKET.get());
                        output.accept(ItemInit.LIQUID_OXYGEN_BUCKET.get());
                        output.accept(ItemInit.LIQUID_NITROGEN_BUCKET.get());

                        output.accept(BlockInit.ROCKET_PAD.get());
                        output.accept(BlockInit.EXHAUST_GRATE.get());
                        output.accept(BlockInit.LAUNCH_TOWER.get());

                        output.accept(BlockInit.HAZARD_STRIP.get());
                        output.accept(ItemInit.SEPARATOR.get());

                        output.accept(CombustionChamberItem.create(ItemInit.STEEL_COMBUSTION_CHAMBER.get(),
                                RocketFuel.HYDROLOX, RocketMaterial.STAINLESS_STEEL));
                        output.accept(TurboPumpItem.create(ItemInit.STEEL_TURBOPUMP.get(),
                                RocketMaterial.STAINLESS_STEEL));
                    })
                    .build());

    public static void register(IEventBus bus)
    {
        TABS.register(bus);
    }
}
