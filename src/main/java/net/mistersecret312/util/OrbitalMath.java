package net.mistersecret312.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.rocket.Stage;
import net.mistersecret312.util.trajectories.EllipticalPath;
import net.mistersecret312.util.trajectories.HyperbolicPath;
import net.mistersecret312.util.trajectories.OrbitalPath;
import org.joml.Vector2d;

import java.text.NumberFormat;
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
        double massWithoutDeltaV = stage.getTotalMass()*Math.pow(2.718, -(deltaV/(stage.getAverageIsp()*stage.getVessel().getOrbit().getParent().getGravityMS2())));

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

    public static OrbitalPath calculatePath(Vector2d sun, Vector2d departure, Vector2d arrival) {
        // Step 1: Get Key Measurements (r_A, r_B, theta_B)
        double rA = departure.distance(sun);
        double rB = arrival.distance(sun);

        Vector2d departureVec = new Vector2d(departure.sub(sun));
        Vector2d arrivalVec = new Vector2d(arrival.sub(sun));
        double thetaB = Math.atan2(arrivalVec.x, arrivalVec.y) - Math.atan2(departureVec.x, departureVec.y);

        // Step 2: Calculate Eccentricity
        double eccentricity = (rB - rA) / (rA - rB * Math.cos(thetaB));

        // Step 3: Determine path type and return the correct object
        if (eccentricity < 1) {
            // Path is an Ellipse
            double semiMajorAxis = rA / (1 - eccentricity);

            // Calculate the vector pointing from the sun to the periapsis (departure point)
            Vector2d periapsisDir = departureVec.mul(1.0 / rA); // Normalize

            // Calculate the second focus
            double focalDistance = 2 * semiMajorAxis * eccentricity;
            Vector2d secondFocus = sun.add(periapsisDir.mul(-focalDistance));

            return new EllipticalPath(sun, secondFocus, semiMajorAxis, departure, arrival);
        } else {
            // Path is a Hyperbola (or Parabola if e == 1)
            // The logic is very similar for creating the object
            double semiMajorAxis = rA / (1 - eccentricity); // Will be negative

            Vector2d periapsisDir = departureVec.mul(1.0 / rA);

            // For a hyperbola, foci are outside the curve
            double focalDistance = 2 * Math.abs(semiMajorAxis * eccentricity);
            Vector2d secondFocus = sun.add(periapsisDir.mul(-focalDistance));

            return new HyperbolicPath(sun, secondFocus, semiMajorAxis, departure, arrival);
        }
    }

    public static double trimDouble(double value)
    {
        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(2);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        return Double.parseDouble(fraction.format(value));
    }
}
