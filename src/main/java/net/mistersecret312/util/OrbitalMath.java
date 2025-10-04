package net.mistersecret312.util;

import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.rocket.Stage;

public class OrbitalMath
{
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
}
