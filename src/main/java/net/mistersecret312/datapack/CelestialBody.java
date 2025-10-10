package net.mistersecret312.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.SpaceObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CelestialBody implements SpaceObject
{	public static final ResourceLocation CELESTIAL_BODY_LOCATION = new ResourceLocation(RocketryScienceMod.MODID, "celestial_body");
    public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(body -> body.name),
            Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(CelestialBody::getDimension),
            ResourceKey.codec(REGISTRY_KEY).optionalFieldOf("parent").forGetter(CelestialBody::getParent),
            Codec.DOUBLE.optionalFieldOf("orbit_altitude", 0d).forGetter(body -> body.orbitAltitude),
            Codec.DOUBLE.fieldOf("sphere_of_influence_radius").forGetter(CelestialBody::getRadiusSphereOfInfluence),
            Codec.DOUBLE.fieldOf("gravity").forGetter(CelestialBody::getGravity),
            Codec.DOUBLE.fieldOf("radius").forGetter(CelestialBody::getRadius)
    ).apply(instance, CelestialBody::new));

    public String name;
    public ResourceKey<Level> dimension;
    public ResourceKey<CelestialBody> parent;
    public double orbitAltitude;
    public double gravity;
    public double radius;
    public double radiusSOI;
    public List<ResourceKey<CelestialBody>> children = new ArrayList<>();

    public CelestialBody(String name, Optional<ResourceKey<Level>> dimension, Optional<ResourceKey<CelestialBody>> parent, double orbitAltitude, double radiusSOI, double gravity, double radius)
    {
        this.name = name;
        this.dimension = dimension.orElse(null);
        this.parent = parent.orElse(null);
        this.radiusSOI = radiusSOI;
        this.orbitAltitude = orbitAltitude;
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

    public double getRadiusSphereOfInfluence()
    {
        return radiusSOI;
    }

    public Optional<ResourceKey<CelestialBody>> getParent()
    {
        return Optional.ofNullable(parent);
    }

    public Optional<ResourceKey<Level>> getDimension()
    {
        return Optional.ofNullable(dimension);
    }

    public double getGravitationalParameter()
    {
        return getGravityMS2()*getRadius()*getRadius();
    }

    @Override
    public Component getName()
    {
        return Component.translatable(name);
    }

    @Override
    public Orbit getOrbit()
    {
        return null;
    }

    @Override
    public CompoundTag save(Level level)
    {
        CompoundTag tag = new CompoundTag();
        ResourceLocation key = level.registryAccess().registryOrThrow(REGISTRY_KEY).getKey(this);
        tag.putString("key", key.toString());
        return tag;
    }

    @Override
    public void load(Level level, CompoundTag compoundTag)
    {

    }

    public List<ResourceKey<CelestialBody>> getChildren() 
    {
        return children;
    }
}
