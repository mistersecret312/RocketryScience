package net.mistersecret312.events;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.init.FluidTypeInit;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.OrbitalMath;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = RocketryScienceMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
    public static ResourceKey<DimensionType> LUNA = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna"));

    public static void init(MinecraftServer server)
    {

    }

    @SubscribeEvent
    public static void entityTick(LivingEvent.LivingTickEvent event)
    {
        LivingEntity entity = event.getEntity();
        if(entity.isInFluidType(FluidTypeInit.CRYOGENIC_HYDROGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.CRYOGENIC_OXYGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.CRYOGENIC_NITROGEN_TYPE.get()))
        {
            entity.setTicksFrozen(400);
            entity.hurt(new DamageSource(Holder.direct(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RocketryScienceMod.MODID, "cryogenic"))))), 2f);
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.END)
        {
            for(Orbit orbit : Orbits.get(event.getServer()).orbits)
            {
                //orbit.tick(event.getServer().overworld(), 0.05);
            }
        }
    }

    @SubscribeEvent
    public static void dimensionTick(TickEvent.LevelTickEvent event)
    {
        Level level = event.level;
        if(level.isClientSide())
            return;

        CelestialBody body = OrbitalMath.getCelestialBody(level);

        if(level instanceof ServerLevel serverLevel)
        {
            double timeOfDay = serverLevel.getTimeOfDay(0f);
            int darken = serverLevel.getSkyDarken();
            if(darken > 0)
            {

            }
            //long newTime = calculateTime(serverLevel, body.getDayLength(), serverLevel.getServer());
            //serverLevel.setDayTime(newTime);
        }

    }
}
