package net.mistersecret312.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
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
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CelestialBody implements SpaceObject
{	public static final ResourceLocation CELESTIAL_BODY_LOCATION = new ResourceLocation(RocketryScienceMod.MODID, "celestial_body");
    public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(body -> body.name),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(CelestialBody::getTexture),
            Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(CelestialBody::getDimension),
            ResourceKey.codec(REGISTRY_KEY).optionalFieldOf("parent").forGetter(CelestialBody::getParent),
            Codec.INT.optionalFieldOf("day_length", 20).forGetter(CelestialBody::getDayLength),
            Codec.DOUBLE.optionalFieldOf("epoch", 0D).forGetter(CelestialBody::getEpoch),
            Codec.DOUBLE.optionalFieldOf("altitude", 0D).forGetter(CelestialBody::getAltitude),
            Codec.DOUBLE.fieldOf("gravity").forGetter(CelestialBody::getGravity),
            Codec.DOUBLE.fieldOf("radius").forGetter(CelestialBody::getRadius)
    ).apply(instance, CelestialBody::new));

    public String name;
    public ResourceLocation texture;
    public ResourceKey<Level> dimension;
    public ResourceKey<CelestialBody> parent;
    public int dayLength;
    public double epoch;
    public double altitude;
    public double gravity;
    public double radius;

    public Orbit orbit;
    public List<ResourceKey<CelestialBody>> children = new ArrayList<>();

    public CelestialBody(String name, ResourceLocation texture, Optional<ResourceKey<Level>> dimension,
                         Optional<ResourceKey<CelestialBody>> parent, int dayLength, double epoch, double altitude, double gravity, double radius)
    {
        this.name = name;
        this.texture = texture;
        this.dimension = dimension.orElse(null);
        this.parent = parent.orElse(null);
        this.epoch = epoch;
        this.altitude = altitude;
        this.dayLength = dayLength;
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

    public int getDayLength()
    {
        return dayLength;
    }

    public double getAltitude()
    {
        return altitude;
    }

    public double getEpoch()
    {
        return epoch;
    }

    public Optional<ResourceKey<CelestialBody>> getParent()
    {
        return Optional.ofNullable(parent);
    }

    public Optional<ResourceKey<Level>> getDimension()
    {
        return Optional.ofNullable(dimension);
    }

    public ResourceLocation getTexture()
    {
        return texture;
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
        return orbit;
    }

    @Override
    public void setOrbit(Orbit orbit)
    {
        this.orbit = orbit;
    }

    public Vector2d getCoordinates(double altitude, double angle)
    {
        double radians = Math.toRadians(angle);

        double x = altitude * Math.cos(radians);
        double y = altitude * Math.sin(radians);

        return new Vector2d(x, y);
    }

    public void render(GuiGraphics graphics)
    {
        ResourceLocation texture = getTexture();
        graphics.blit(texture, 100, 100, 0, 0, 16, 16);
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
