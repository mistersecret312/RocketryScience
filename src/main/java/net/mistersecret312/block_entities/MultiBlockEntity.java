package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.MultiblockBlock;
import net.mistersecret312.init.BlockEntityInit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiBlockEntity extends BlockEntity
{
    public BlockPos masterVector = null;
    public List<BlockPos> slaveVectors = new ArrayList<>();

    public MultiBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void setRemoved()
    {
        if(this.isMaster())
        {
            for(BlockPos pos : slaveVectors)
            {
                MultiBlockEntity slave = (MultiBlockEntity) level.getBlockEntity(this.getBlockPos().offset(pos));
                if(slave != null)
                {
                    slave.masterVector = null;
                }
            }
        }
        else if(this.getMasterRelativePosition() != null)
        {
            MultiBlockEntity master = (MultiBlockEntity) level.getBlockEntity(this.getBlockPos().offset(masterVector));
            if(master != null)
                master.slaveVectors.remove(this.getBlockPos().subtract(master.getBlockPos()));
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if(!this.isMaster() && this.getMasterRelativePosition() != null)
            tag.putLong("master_pos", this.masterVector.asLong());
        if(this.isMaster())
            tag.putLongArray("slaves", this.slaveVectors.stream().map(BlockPos::asLong).toList());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if(!this.isMaster() && tag.contains("master_pos"))
            this.masterVector = BlockPos.of(tag.getLong("master_pos"));
        if(this.isMaster())
            this.slaveVectors = Arrays.stream(tag.getLongArray("slaves")).mapToObj(BlockPos::of).toList();
    }

    public boolean isMaster()
    {
        return this.getBlockState().getValue(MultiblockBlock.MASTER);
    }

    public BlockPos getMasterRelativePosition()
    {
        return masterVector;
    }

    public List<BlockPos> getSlaveRelativePositions()
    {
        return slaveVectors;
    }
}
