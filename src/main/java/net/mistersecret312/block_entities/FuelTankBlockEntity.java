package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.blocks.SolidFuelTankBlock;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlockEntity extends MultiBlockEntity
{
    public RocketFuel fuel = RocketFuel.HYDROLOX;
    public int capacity = 50000;

    public RocketFuelTank fuelTank = createTank();
    public LazyOptional<IFluidHandler> holder = LazyOptional.empty();

    public FuelTankBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.LIQUID_FUEL_TANK.get(), pPos, pBlockState);
    }

    @Override
    public void onLoad()
    {
        this.holder = LazyOptional.of(() -> fuelTank);
        super.onLoad();
        updateMaster();
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        holder.invalidate();
    }

    @Nullable
    public RocketFuelTank getFuelTank()
    {
        if(this.isMaster())
        {
            return fuelTank;
        }
        else if(this.getMasterRelativePosition() != BlockPos.ZERO)
        {
            FuelTankBlockEntity master = getMaster();
            if(master != null)
                return master.fuelTank;
        }

        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {

        if (capability == ForgeCapabilities.FLUID_HANDLER)
        {
            if(this.isMaster())
                return holder.cast();
            else if(this.getMasterRelativePosition() != BlockPos.ZERO)
            {
                FuelTankBlockEntity master = this.getMaster();
                if(master != null)
                    return master.holder.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putString("fuel_type", this.fuel.getName());
        tag.putInt("capacity", this.capacity);
        if(this.isMaster())
            this.fuelTank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.fuel = RocketFuel.valueOf(tag.getString("fuel_type").toUpperCase());
        this.capacity = tag.getInt("capacity");
        if(tag.contains("propellants"))
            this.fuelTank.readFromNBT(tag);
    }

    @Override
    public void updateMaster()
    {
        int cap = getFuelTankCapacity();
        this.fuelTank = createTank(this.fuelTank, cap);
        this.holder = LazyOptional.of(() -> this.fuelTank);
    }


    public int getFuelTankCapacity()
    {
        int capacity = this.capacity;

        for(BlockPos slavePos : getSlaveRelativePositions())
        {
            if(slavePos == BlockPos.ZERO)
                continue;

            if(level.getBlockEntity(this.getBlockPos().offset(slavePos)) instanceof FuelTankBlockEntity tank)
                capacity += tank.capacity;
        }

        return capacity;
    }

    @Nullable
    public FuelTankBlockEntity getMaster()
    {
        if(this.getMasterRelativePosition() != BlockPos.ZERO)
            return (FuelTankBlockEntity) this.getLevel().getBlockEntity(this.getBlockPos().offset(masterVector));
        else return null;
    }

    public RocketFuelTank createTank()
    {
        return new RocketFuelTank(this.fuel.getPropellants(), getFuelTankCapacity())
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }

    public RocketFuelTank createTank(RocketFuelTank fuelTank, int capacity)
    {
        return new RocketFuelTank(fuelTank, capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        load(pkt.getTag());
    }
}
