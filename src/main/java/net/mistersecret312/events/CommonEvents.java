package net.mistersecret312.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.init.FluidTypeInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.network.packets.ClientOrbitsUpdatePacket;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.OrbitalMath;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = RocketryScienceMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
    public static ResourceKey<DimensionType> LUNA = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna"));

    @SubscribeEvent
    public static void entityTick(LivingEvent.LivingTickEvent event)
    {
        //Cryogenic fluids
        LivingEntity entity = event.getEntity();
        if(entity.isInFluidType(FluidTypeInit.CRYOGENIC_HYDROGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.CRYOGENIC_OXYGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.CRYOGENIC_NITROGEN_TYPE.get()))
        {
            entity.setTicksFrozen(400);
            entity.hurt(new DamageSource(Holder.direct(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RocketryScienceMod.MODID, "cryogenic"))))), 2f);
        }

        //Gravity
		OrbitalMath.gravityAffect(entity);
    }

    @SubscribeEvent
    public static void fallEvent(LivingFallEvent event)
    {
        CelestialBody body = OrbitalMath.getCelestialBody(event.getEntity().level());
        if(body != null)
        {
            event.setDamageMultiplier((float) (event.getDamageMultiplier()*body.getGravity()));
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.END)
        {
            for(Orbit orbit : Orbits.get(event.getServer()).getOrbits())
            {
                if(orbit.shouldRemove)
                    continue;

                orbit.tick(event.getServer().overworld());
            }
            NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientOrbitsUpdatePacket(Orbits.get(event.getServer()).getOrbits(), new ArrayList<>()));
        }
    }
}
