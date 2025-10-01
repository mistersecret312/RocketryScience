package net.mistersecret312.util;

import net.minecraft.util.StringRepresentable;

public enum RocketMaterial implements StringRepresentable
{
    STAINLESS_STEEL("stainless_steel", 1.0, 1.0, 1.0);
    String name;
    double massCoefficient;
    double thrustCoefficient;
    double efficiencyCoefficient;

    RocketMaterial(String name, double massCoefficient,
                   double thrustCoefficient, double efficiencyCoefficient)
    {
        this.name = name;
        this.massCoefficient = massCoefficient;
        this.thrustCoefficient = thrustCoefficient;
        this.efficiencyCoefficient = efficiencyCoefficient;
    }


    public double getMassCoefficient()
    {
        return massCoefficient;
    }

    public double getThrustCoefficient()
    {
        return thrustCoefficient;
    }

    public double getEfficiencyCoefficient()
    {
        return efficiencyCoefficient;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
