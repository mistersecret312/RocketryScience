package net.mistersecret312.init;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.items.*;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RocketryScienceMod.MODID);

    public static final RegistryObject<BucketItem> CRYOGENIC_HYDROGEN_BUCKET = ITEMS.register("cryogenic_hydrogen_bucket",
            () -> new BucketItem(FluidInit.SOURCE_CRYOGENIC_HYDROGEN, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<BucketItem> CRYOGENIC_OXYGEN_BUCKET = ITEMS.register("cryogenic_oxygen_bucket",
            () -> new BucketItem(FluidInit.SOURCE_CRYOGENIC_OXYGEN, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<BucketItem> CRYOGENIC_NITROGEN_BUCKET = ITEMS.register("cryogenic_nitrogen_bucket",
            () -> new BucketItem(FluidInit.SOURCE_CRYOGENIC_NITROGEN, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final RegistryObject<TurboPumpItem> STEEL_TURBOPUMP = ITEMS.register("steel_turbopump",
            () -> new TurboPumpItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<CombustionChamberItem> STEEL_COMBUSTION_CHAMBER = ITEMS.register("steel_combustion_chamber_component",
            () -> new CombustionChamberItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<FuelTankBlockItem> FUEL_TANK = ITEMS.register("fuel_tank",
            () -> new FuelTankBlockItem(BlockInit.FUEL_TANK.get(), new Item.Properties().stacksTo(64)));
    public static final RegistryObject<SeparatorBlockItem> SEPARATOR = ITEMS.register("separator",
            () -> new SeparatorBlockItem(BlockInit.SEPARATOR.get(), new Item.Properties().stacksTo(64)));

    public static final RegistryObject<PadLinkingItem> PAD_LINKING = ITEMS.register("pad_linking",
            () -> new PadLinkingItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CHROMIUM_INGOT = ITEMS.register("chromium_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
