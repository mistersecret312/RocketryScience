package net.mistersecret312.fluids;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RocketFuelTank implements IFluidHandler
{
    private List<Predicate<FluidStack>> filter;
    private int tanks;
    private int capacity;

    private List<FluidTank> propellants;

    public RocketFuelTank(List<Predicate<FluidStack>> propellants, int capacity)
    {
        this.tanks = propellants.size();
        this.capacity = capacity;

        this.filter = propellants;
        this.propellants = new ArrayList<>();
        for (int tank = 0; tank < tanks; tank++)
            this.propellants.add(new FluidTank(capacity, propellants.get(tank)));

    }

    public RocketFuelTank(RocketFuelTank otherTank, int capacity)
    {
        this.tanks = otherTank.tanks;
        this.capacity = capacity;

        this.filter = otherTank.filter;
        this.propellants = otherTank.propellants;
    }

    public List<Predicate<FluidStack>> getFilter()
    {
        return filter;
    }

    public List<FluidTank> getPropellants()
    {
        return propellants;
    }

    @Override
    public int getTanks()
    {
        return tanks;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank)
    {
        return this.propellants.get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank)
    {
        return this.capacity;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
        for(FluidTank propellant : propellants)
        {
            propellant.setCapacity(capacity);
        }
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack)
    {
        return this.filter.get(tank).test(stack) && this.propellants.get(tank).getFluidAmount() < capacity;
    }

    public boolean isFluidValid(@NotNull FluidStack stack)
    {
        for (int tank = 0; tank < tanks; tank++)
        {
            if(this.isFluidValid(tank, stack))
                return true;
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        for(int tank = 0; tank < this.tanks; tank++)
        {
            FluidStack tankFluid = this.getFluidInTank(tank);

            if(resource.isEmpty() || !this.isFluidValid(tank, resource))
                continue;

            if(action.simulate())
            {
                if(tankFluid.isEmpty())
                    return Math.min(capacity, resource.getAmount());
                if(!tankFluid.isFluidEqual(resource))
                    continue;
                return Math.min(capacity-tankFluid.getAmount(), resource.getAmount());
            }
            if(tankFluid.isEmpty())
            {
                tankFluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
                this.propellants.get(tank).setFluid(tankFluid);
                onContentsChanged();
                return tankFluid.getAmount();
            }
            if(!tankFluid.isFluidEqual(resource))
                continue;
            int filled = capacity - tankFluid.getAmount();
            if(resource.getAmount() < filled)
            {
                tankFluid.grow(resource.getAmount());
                filled = resource.getAmount();
            }
            else tankFluid.setAmount(capacity);
            if(filled > 0)
                onContentsChanged();

            return filled;
        }

        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
    {
        for (int tank = 0; tank < tanks; tank++)
        {
            FluidStack tankFluid = this.getFluidInTank(tank);
            if(resource.isEmpty() || !resource.isFluidEqual(tankFluid))
                continue;

            if(tankFluid.getAmount() < resource.getAmount())
            {
                FluidStack output = tankFluid.copy();
                tankFluid.setAmount(0);
                onContentsChanged();
                return output;
            }
            else
            {
                int toRemove = tankFluid.getAmount() == resource.getAmount() ? tankFluid.getAmount() : resource.getAmount();
                FluidStack output = new FluidStack(tankFluid, toRemove);
                tankFluid.shrink(toRemove);
                onContentsChanged();
                return output;
            }

        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action)
    {
        for (int tank = 0; tank < tanks; tank++)
        {
            FluidStack tankFluid = this.getFluidInTank(tank);
            if(tankFluid.isEmpty())
                continue;

            tankFluid.shrink(maxDrain);
            this.propellants.get(tank).setFluid(tankFluid);
        }

        return FluidStack.EMPTY;
    }

    public RocketFuelTank readFromNBT(CompoundTag tag) {

        ListTag listTag = tag.getList("propellants", Tag.TAG_COMPOUND);
        for(int tank = 0; tank < listTag.size(); tank++)
        {
            CompoundTag fluidStackTag = listTag.getCompound(tank);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fluidStackTag);
            this.propellants.get(tank).setFluid(fluidStack);
        }

        return this;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {

        ListTag listTag = new ListTag();
        for (int tank = 0; tank < tanks; tank++)
        {
            FluidStack tankFluid = this.getFluidInTank(tank);
            CompoundTag fluidStack = new CompoundTag();
            listTag.add(tankFluid.writeToNBT(fluidStack));
        }
        tag.put("propellants", listTag);

         return tag;
    }

    public int getSpace(int tank)
    {
        return capacity-propellants.get(tank).getFluidAmount();
    }

    protected void onContentsChanged()
    {

    }
}
