package net.mistersecret312.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class RocketFuelTank implements IFluidHandler, IFluidTank
{

    @Override
    public @NotNull FluidStack getFluid()
    {
        return null;
    }

    @Override
    public int getFluidAmount()
    {
        return 0;
    }

    @Override
    public int getCapacity()
    {
        return 0;
    }

    @Override
    public boolean isFluidValid(FluidStack stack)
    {
        return false;
    }

    @Override
    public int getTanks()
    {
        return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank)
    {
        return null;
    }

    @Override
    public int getTankCapacity(int tank)
    {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack)
    {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
    {
        return null;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action)
    {
        return null;
    }
}
