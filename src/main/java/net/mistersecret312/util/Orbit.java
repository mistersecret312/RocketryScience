package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.rocket.Rocket;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import org.joml.Vector2f;

import java.util.LinkedHashSet;

public class Orbit
{
    private static final String SPACE_OBJECT = "space_object";
    private static final String EPOCH = "epoch";
    private static final String ALTITUDE = "altitude";
    private static final String PERIOD = "period";
    private static final String PARENT = "parent";

    private static final String IS_ARTIFICIAL = "artificial";

    public double epoch;
    public double orbitalAltitude;
    public double orbitalPeriod;

    public CelestialBody parent;
    public SpaceObject spaceObject;

    public Orbit(CelestialBody parent, double altitude, double epoch) {
        this.parent = parent;

        this.epoch = epoch;
        this.orbitalAltitude = parent.getRadius()+altitude;

        this.orbitalPeriod = 10*Math.pow(altitude, 1.5);
    }

    public void tick(Level level)
    {

    }

    public CompoundTag save(Level level)
    {
        CompoundTag tag = new CompoundTag();

        tag.putDouble(ALTITUDE, orbitalAltitude);
        tag.putDouble(EPOCH, epoch);
        tag.putDouble(PERIOD, orbitalPeriod);

        ResourceLocation rl = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).getKey(parent);
        if(rl == null)
            rl = new ResourceLocation(RocketryScienceMod.MODID, "error");
        tag.putString(PARENT, rl.toString());
        tag.putBoolean(IS_ARTIFICIAL, spaceObject instanceof SpaceCraft);
        tag.put(SPACE_OBJECT, spaceObject.save(level));

        return tag;
    }

    public static Orbit load(Level level, CompoundTag tag)
    {
        CelestialBody parent = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY)
                                    .get(ResourceLocation.parse(tag.getString(PARENT)));

        double altitude = tag.getDouble(ALTITUDE);
        double epoch = tag.getDouble(EPOCH);

        Orbit orbit = new Orbit(parent, altitude, epoch);
        boolean artificial = tag.getBoolean(IS_ARTIFICIAL);
        SpaceObject object;
        if(artificial)
        {
            object = new SpaceCraft(new LinkedHashSet<>(), level);
            object.load(level, tag.getCompound(SPACE_OBJECT));
        }
        else
        {
            CompoundTag compoundTag = tag.getCompound(SPACE_OBJECT);
            ResourceLocation key = ResourceLocation.parse(compoundTag.getString("key"));
            object = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).get(key);
        }

        orbit.spaceObject = object;
        object.setOrbit(orbit);

        return orbit;
    }



    // Orbital period in seconds
    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    // Current radius (distance from parent)
    public double getOrbitalAltitude() {
        return orbitalAltitude;
    }

    public double getAngle(long time)
    {
        double period = orbitalPeriod*20*20*60;

        double velocity = (360D/period);
        double angle = velocity*time + epoch;
        return angle % 360D;
    }

    public CelestialBody getParent()
    {
        return parent;
    }
}
