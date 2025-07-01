package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.SolidFuelTankBlock;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class SolidFuelTankBlockEntity extends MultiBlockEntity
{

    public int fuel = 0;
    public SolidFuelTankBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.MULTIBLOCK_TEST.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if(this.isMaster())
            tag.putInt("fuel_stored", this.fuel);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if(this.isMaster() && tag.contains("fuel_stored"))
            this.fuel = tag.getInt("fuel_stored");
    }

    @Nullable
    public SolidFuelTankBlockEntity getMaster()
    {
        if(this.getMasterRelativePosition() != null)
            return (SolidFuelTankBlockEntity) this.getLevel().getBlockEntity(this.getBlockPos().offset(masterVector));
        else return null;
    }

    public void increaseStored(int fuel)
    {
        int capacity = getFuelCapacity();
        this.fuel = Math.min(this.fuel+fuel, capacity);
    }

    public int getFuelStored()
    {
        if(this.isMaster())
            return fuel;
        else
        {
            SolidFuelTankBlockEntity master = this.getMaster();
            if(master != null)
                return master.fuel;
        }

        return 0;
    }

    public int getFuelCapacity()
    {
        int capacity = 0;
        if(this.getBlockState().getBlock() instanceof SolidFuelTankBlock tank)
            capacity += tank.getFuelCapacity();

        for(BlockPos slavePos : getSlaveRelativePositions())
        {
            if(level.getBlockState(this.getBlockPos().offset(slavePos)).getBlock() instanceof SolidFuelTankBlock tank)
                capacity += tank.getFuelCapacity();
        }
        if(this.fuel > capacity)
            this.fuel = capacity;

        return capacity;
    }
}
