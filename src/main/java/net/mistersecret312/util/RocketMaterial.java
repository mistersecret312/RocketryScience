package net.mistersecret312.util;

import net.minecraft.util.StringRepresentable;

public enum RocketMaterial implements StringRepresentable
{
    COPPER("copper",  0.5, 0.75, 0.95, 0.9),
    STAINLESS_STEEL("stainless_steel", 1.0, 1.0, 1.0, 1.0),
    COSMIC_ALLOY("cosmic_alloy", 0.8, 1.5, 1.5, 1.5);

    String name;
    double massCoefficient;
    double thrustCoefficient;
    double efficiencyCoefficientVacuum;
    double efficiencyCoefficientAtmosphere;

    RocketMaterial(String name, double massCoefficient,
                   double thrustCoefficient, double efficiencyCoefficientVacuum, double efficiencyCoefficientAtmosphere)
    {
        this.name = name;
        this.massCoefficient = massCoefficient;
        this.thrustCoefficient = thrustCoefficient;
        this.efficiencyCoefficientVacuum = efficiencyCoefficientVacuum;
        this.efficiencyCoefficientAtmosphere = efficiencyCoefficientAtmosphere;
    }


    public double getMassCoefficient()
    {
        return massCoefficient;
    }

    public double getThrustCoefficient()
    {
        return thrustCoefficient;
    }

    public double getEfficiencyCoefficientVacuum()
    {
        return efficiencyCoefficientVacuum;
    }

    public double getEfficiencyCoefficientAtmosphere()
    {
        return efficiencyCoefficientAtmosphere;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
