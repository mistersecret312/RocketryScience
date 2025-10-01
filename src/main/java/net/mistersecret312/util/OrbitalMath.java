package net.mistersecret312.util;

import net.mistersecret312.datapack.CelestialBody;

public class OrbitalMath
{
    public static double getOrbitDeltaV(CelestialBody body, double height)
    {
        double altitude = body.getRadius()+height;
        return Math.sqrt(body.getGravitationalParameter()/altitude);
    }

    public static double getTransferDeltaV(CelestialBody body, double initialHeight, double targetHeight)
    {
        initialHeight += body.getRadius();
        targetHeight += body.getRadius();

        double part1 = Math.sqrt(body.getGravitationalParameter()/initialHeight)*(Math.sqrt((2*targetHeight)/initialHeight+targetHeight)-1);
        double part2 = Math.sqrt(body.getGravitationalParameter()/targetHeight)*(1-Math.sqrt((2*targetHeight)/initialHeight+targetHeight));

        return part1+part2;
    }
}
