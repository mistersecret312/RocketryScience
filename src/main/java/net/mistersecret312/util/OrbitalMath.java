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
import net.mistersecret312.util.trajectories.ParabolicPath;
import org.joml.Vector2d;
import org.joml.Vector3d;

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

    public static OrbitalPath calculatePath(Vector2d focus1, Vector2d departure, Vector2d arrival, double efficiency, double parameter) {
        double rA = focus1.distance(departure);
        double rB = focus1.distance(arrival);

        // --- Case 1: Parabolic Path (Efficiency = 50) ---
        if (Math.abs(efficiency - 50.0) < 1e-9) {
            try {
                return new ParabolicPath(focus1, departure, arrival);
            } catch (Exception e) {
                return null; // Parabolic solution failed
            }
        }

        // --- Case 2: Elliptical or Hyperbolic ---

        // Calculate 'a' (semi-major axis) from efficiency
        double a_hohmann = (rA + rB) / 2.0;
        double E_base_magnitude = parameter / (2.0 * a_hohmann);
        double normalizedScore = (efficiency - 50.0) / 50.0;
        double orbitEnergy = -normalizedScore * E_base_magnitude;
        double a_final = -parameter / (2.0 * orbitEnergy); // Will be positive for ellipse, negative for hyperbola

        // Check for impossible ellipse (2a < distance(A,B))
        if (a_final > 0 && 2.0 * a_final < departure.distance(arrival)) {
            return null; // Impossible path
        }

        // Find the second focus (F2)
        Vector2d focus2;
        double radiusFromA, radiusFromB;

        if (orbitEnergy < 0) { // Ellipse (a_final is positive)
            radiusFromA = 2.0 * a_final - rA;
            radiusFromB = 2.0 * a_final - rB;
        } else { // Hyperbola (a_final is negative)
            radiusFromA = rA - 2.0 * a_final; // a_final is negative, so this adds
            radiusFromB = rB - 2.0 * a_final;
        }

        Vector2d[] intersections = findCircleIntersections(departure, radiusFromA, arrival, radiusFromB);

        if (intersections == null) {
            return null; // No intersection, path is impossible
        }

        // --- Pick the correct focus for the "short arc" ---
        // We determine which side of the A-B line F1 is on.
        // The correct F2 will be on the *opposite* side.

        Vector2d vecAB = new Vector2d(arrival).sub(departure);
        Vector2d vecAF1 = new Vector2d(focus1).sub(departure);
        double f1_side = vecAB.x * vecAF1.y - vecAB.y * vecAF1.x;

        Vector2d vecAI1 = new Vector2d(intersections[0]).sub(departure);
        double i1_side = vecAB.x * vecAI1.y - vecAB.y * vecAI1.x;

        if (f1_side * i1_side < 0)
        {
            focus2 = intersections[0];
        }
        else
        {
            focus2 = intersections[1];
        }

        // --- Return the correct path type ---
        if (orbitEnergy < 0) {
            return new EllipticalPath(focus1, focus2, a_final, departure, arrival);
        } else {
            return new HyperbolicPath(focus1, focus2, a_final, departure, arrival);
        }
    }

    private static Vector2d[] findCircleIntersections(Vector2d c1, double r1, Vector2d c2, double r2) {
        double d = c1.distance(c2);
        if (d > r1 + r2 || d < Math.abs(r1 - r2) || d == 0) {
            return null; // No solution
        }

        double a = (r1 * r1 - r2 * r2 + d * d) / (2 * d);
        double h = Math.sqrt(Math.max(0, r1 * r1 - a * a));

        double dx = c2.x - c1.x;
        double dy = c2.y - c1.y;

        Vector2d p2 = new Vector2d(
                c1.x + a * (dx) / d,
                c1.y + a * (dy) / d
        );

        Vector2d i1 = new Vector2d(
                p2.x + h * (dy) / d,
                p2.y - h * (dx) / d
        );
        Vector2d i2 = new Vector2d(
                p2.x - h * (dy) / d,
                p2.y + h * (dx) / d
        );

        return new Vector2d[]{i1, i2};
    }

    /**
     * Linearly interpolates between two angles, taking the shortest path around the circle.
     */
    public static double lerpAngle(double a, double b, double t, boolean direction) {
        double delta = b - a;

        // Normalize the delta to the shortest path (-PI to +PI)
        double shortestDelta = (delta + Math.PI * 3) % (Math.PI * 2) - Math.PI;

        double finalDelta;
        if (direction) {
            // We want a positive delta
            if (shortestDelta < 0) {
                finalDelta = shortestDelta + 2 * Math.PI; // Go the long way CCW
            } else {
                finalDelta = shortestDelta; // Shortest was already CCW
            }
        } else {
            // We want a negative delta
            if (shortestDelta > 0) {
                finalDelta = shortestDelta - 2 * Math.PI; // Go the long way CW
            } else {
                finalDelta = shortestDelta; // Shortest was already CW
            }
        }

        // Add the fraction of the final delta to the start angle
        // No final modulo is needed here, as atan2 in the path classes will handle it
        return a + finalDelta * t;
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
