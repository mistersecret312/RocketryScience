package net.mistersecret312.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.RocketryScienceMod;


public class CelestialBody
{	public static final ResourceLocation CELESTIAL_BODY_LOCATION = new ResourceLocation(RocketryScienceMod.MODID, "celestial_body");
    public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CelestialBody::getDimension),
            Codec.DOUBLE.fieldOf("gravity").forGetter(CelestialBody::getGravity),
            Codec.DOUBLE.fieldOf("radius").forGetter(CelestialBody::getRadius)
    ).apply(instance, CelestialBody::new));

    public ResourceKey<Level> dimension;
    public double gravity;
    public double radius;

    public CelestialBody(ResourceKey<Level> dimension, double gravity, double radius)
    {
        this.dimension = dimension;
        this.gravity = gravity;
        this.radius = radius;
    }

    public double getGravity()
    {
        return gravity;
    }

    public double getGravityMS2()
    {
        return gravity*9.8;
    }

    public double getRadius()
    {
        return radius;
    }

    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    public double getGravitationalParameter()
    {
        return getGravityMS2()*getRadius()*getRadius();
    }
}
