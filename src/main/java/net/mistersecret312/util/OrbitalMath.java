package net.mistersecret312.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.rocket.Stage;

import java.util.HashMap;
import java.util.Map;

public class OrbitalMath
{
    private static HashMap<ServerLevel, Double> dimensionTime = new HashMap<>();

    public static double getOrbitDeltaV(CelestialBody body, double height)
    {
        double altitude = body.getRadius()+height;
        return Math.sqrt(body.getGravitationalParameter()/altitude);
    }

    public static double getLaunchDeltaV(CelestialBody body, double targetOrbitHeight)
    {
        double startHeight = 120000;

        double part1 = getOrbitDeltaV(body, startHeight);
        double part2 = getTransferDeltaV(body, startHeight, targetOrbitHeight);

        return part1+part2;
    }

    public static double getTransferDeltaV(CelestialBody body, double initialHeight, double targetHeight)
    {
        initialHeight += body.getRadius();
        targetHeight += body.getRadius();

        double part1 = Math.sqrt(body.getGravitationalParameter()/initialHeight)*(Math.sqrt((2*targetHeight)/(initialHeight+targetHeight))-1);
        double part2 = Math.sqrt(body.getGravitationalParameter()/targetHeight)*(1-Math.sqrt((2*initialHeight)/(initialHeight+targetHeight)));

        return part1+part2;
    }

    public static int deltaVToFuelMass(Stage stage, double deltaV)
    {
        double stageMass = stage.getTotalMass();
        double massWithoutDeltaV = stage.getTotalMass()*Math.pow(2.718, -(deltaV/(stage.getAverageIsp()*stage.getRocket().getCelestialBody().getGravityMS2())));

        return (int) (stageMass-massWithoutDeltaV);
    }

    public static CelestialBody getCelestialBody(Level level)
    {
        Registry<CelestialBody> registry = level.getServer().registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
        for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
        {
            if(entry.getValue().getDimension().isPresent() &&
            entry.getValue().getDimension().get().equals(level.dimension()))
                return entry.getValue();
        }

        return null;
    }

    public static CelestialBody getCelestialBody(ResourceLocation dimensionType)
    {
        Level level = Minecraft.getInstance().level;
        if(level == null)
            return null;

        Registry<CelestialBody> registry = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
        for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
        {
            if(entry.getValue().getDimension().isPresent()
                    && entry.getValue().getDimension().get().location().equals(dimensionType))
                return entry.getValue();
        }

        return null;
    }

    public static long calculateTime(CelestialBody body, ServerLevel level, long currentTime) {

        int dayLength = body.getDayLength();

        // If day length is 0, time is frozen
        if (dayLength == 0) {
            return currentTime;
        }

        // Vanilla day length behavior
        if (dayLength == 20) {
            return currentTime + 1L;
        }

        // Custom day length progression
        double speedFactor = (20.0 * 60.0) / (dayLength * 60.0); // ticks per tick
        double accumulated = dimensionTime.getOrDefault(level, 0.0);
        accumulated += speedFactor;

        long ticksToAdd = (long) accumulated;
        accumulated -= ticksToAdd;

        dimensionTime.put(level, accumulated);
        return currentTime + ticksToAdd;
    }
}
