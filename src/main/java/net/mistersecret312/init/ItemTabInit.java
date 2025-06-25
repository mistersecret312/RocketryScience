package net.mistersecret312.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;

public class ItemTabInit
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RocketryScienceMod.MODID);

    public static final RegistryObject<CreativeModeTab> ROCKET_ENGINE_PARTS = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(Items.FIREWORK_ROCKET::getDefaultInstance)
                    .title(Component.translatable("tabs.rocketry_science.main"))
                    .displayItems((parameters, output) ->
                    {
                        output.accept(ItemInit.LIQUID_HYDROGEN_BUCKET.get());
                        output.accept(ItemInit.LIQUID_OXYGEN_BUCKET.get());
                        output.accept(ItemInit.LIQUID_NITROGEN_BUCKET.get());
                    })
                    .build());

    public static void register(IEventBus bus)
    {
        TABS.register(bus);
    }
}
