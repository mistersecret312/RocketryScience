package net.mistersecret312.util;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fluids.FluidType;
import net.mistersecret312.init.FluidTypeInit;

import java.util.List;

public enum RocketFuel implements StringRepresentable
{
    HYDROLOX("hydrolox", List.of(FluidTypeInit.LIQUID_HYDROGEN_TYPE.get(), FluidTypeInit.LIQUID_OXYGEN_TYPE.get()),
            380d, 2000);

    String name;
    List<FluidType> fluids;
    double efficiency;
    double thrust_kN;
    RocketFuel(String name, List<FluidType> fluids, double efficiency, double thrust_kN)
    {
        this.name = name;
        this.fluids = fluids;
        this.efficiency = efficiency;
        this.thrust_kN = thrust_kN;
    }

    public List<FluidType> getPropellants()
    {
        return fluids;
    }

    public double getEfficiency()
    {
        return efficiency;
    }

    public double getThrustKiloNewtons()
    {
        return thrust_kN;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
