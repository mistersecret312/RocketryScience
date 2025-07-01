package net.mistersecret312.util;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fluids.FluidStack;
import net.mistersecret312.RocketryScienceMod;

import java.util.List;
import java.util.function.Predicate;

public enum RocketFuel implements StringRepresentable
{
    HYDROLOX("hydrolox", List.of(stack -> stack.getFluid().is(RocketryScienceMod.HYDROGEN), stack -> stack.getFluid().is(RocketryScienceMod.OXYGEN)),
            380d, 2000);

    String name;
    List<Predicate<FluidStack>> fluids;
    double efficiency;
    double thrust_kN;
    RocketFuel(String name, List<Predicate<FluidStack>> fluids, double efficiency, double thrust_kN)
    {
        this.name = name;
        this.fluids = fluids;
        this.efficiency = efficiency;
        this.thrust_kN = thrust_kN;
    }

    public List<Predicate<FluidStack>> getPropellants()
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

    public String getName()
    {
        return name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
