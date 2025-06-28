package net.mistersecret312.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.BlueprintBlockEntity;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blueprint.Blueprint;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.capabilities.BlueprintDataCapability;
import net.mistersecret312.mishaps.FuelLeakMishap;
import net.mistersecret312.mishaps.Mishap;
import net.mistersecret312.mishaps.MishapType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class MishapInit
{
    public static final DeferredRegister<MishapType<?,?,?>> MISHAPS = DeferredRegister.create(new ResourceLocation(RocketryScienceMod.MODID, "mishap"), RocketryScienceMod.MODID);
    public static final Supplier<IForgeRegistry<MishapType<?,?,?>>> REGISTRY = MISHAPS.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<MishapType<?, RocketEngineBlockEntity, RocketEngineBlueprint>> FUEL_LEAK = MISHAPS.register("fuel_leak",
            () -> new MishapType<>(MishapType.MishapTarget.ROCKET_ENGINE, MishapType.MishapCategory.MINOR,
                    (type, blockEntity) -> new FuelLeakMishap(blockEntity, null, 1),
                    (type, blueprint) -> new FuelLeakMishap(null, blueprint, 1),
        0.1F, 0.02, -0.04));

    public static void register(IEventBus bus)
    {
        MISHAPS.register(bus);
    }

    @Nullable
    public static MishapType<?, ?, ?> getRandomType(MishapType.MishapTarget targetType)
    {
        Random random = new Random();
        List<MishapType<?, ?, ?>> mishapList = MishapInit.REGISTRY.get().getValues().stream()
                .filter(mishapType -> mishapType.getTarget().equals(targetType))
                .filter(mishapType -> mishapType.getChance() > random.nextFloat()).toList();

        return mishapList.isEmpty() ? null : mishapList.get(random.nextInt(mishapList.size()));
    }
}
