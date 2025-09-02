package net.mistersecret312.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.init.FluidTypeInit;

@Mod.EventBusSubscriber(modid = RocketryScienceMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
    @SubscribeEvent
    public static void entityTick(LivingEvent.LivingTickEvent event)
    {
        LivingEntity entity = event.getEntity();
        if(entity.isInFluidType(FluidTypeInit.LIQUID_HYDROGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.LIQUID_OXYGEN_TYPE.get())
        || entity.isInFluidType(FluidTypeInit.LIQUID_NITROGEN_TYPE.get()))
        {
            entity.setTicksFrozen(400);
            entity.hurt(new DamageSource(Holder.direct(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RocketryScienceMod.MODID, "cryogenic"))))), 2f);
        }
    }
}
