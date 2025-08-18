package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.SolidFuelTankBlock;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SolidFuelTankBlockEntity extends MultiBlockEntity
{
    public int fuel = 0;
    public SolidFuelTankBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.SOLID_FUEL_TANK.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("fuel_stored", this.fuel);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.fuel = tag.getInt("fuel_stored");
    }

    @Override
    public boolean findingPartsCheck(BlockPos pos, List<MultiBlockEntity> blockEntity)
    {
        return true;
    }

    @Nullable
    public SolidFuelTankBlockEntity getMaster()
    {
        if(this.getMasterRelativePosition() != BlockPos.ZERO)
            return (SolidFuelTankBlockEntity) this.getLevel().getBlockEntity(this.getBlockPos().offset(masterVector));
        else return null;
    }

    public void increaseStored(int fuel)
    {
        int capacity = getFuelCapacity();
        if(this.isMaster())
            this.fuel = Math.max(0, Math.min(this.fuel+fuel, capacity));
        else
        {
            SolidFuelTankBlockEntity master = this.getMaster();
            if(master != null)
                master.fuel = Math.max(0, Math.min(this.fuel+fuel, capacity));
        }
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
            if(slavePos.equals(BlockPos.ZERO))
                continue;

            if(level.getBlockState(this.getBlockPos().offset(slavePos)).getBlock() instanceof SolidFuelTankBlock tank)
                capacity += tank.getFuelCapacity();
        }
        if(this.fuel > capacity)
            this.fuel = capacity;

        return capacity;
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
