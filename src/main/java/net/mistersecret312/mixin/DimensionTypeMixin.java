package net.mistersecret312.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.OrbitalMath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin
{

    @ModifyConstant(method = "timeOfDay(J)F", constant = @Constant(doubleValue = 24000.0D))
    public double timeOfDay(double constant)
    {
        DimensionType type = (DimensionType) (Object) this;

        Level level = Minecraft.getInstance().level;
        if(level == null)
            return constant;

        RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
        if(registryAccess == null)
            return constant;

        ResourceLocation typeLocation = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).getKey(type);
        CelestialBody body = OrbitalMath.getCelestialBody(typeLocation);
        if(body == null)
            return constant;

        return body.getDayLength()*60*20;
    }
}
