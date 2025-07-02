package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    public BlockPos masterVector = BlockPos.ZERO;
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
                if(pos == BlockPos.ZERO)
                    continue;

                MultiBlockEntity slave = (MultiBlockEntity) level.getBlockEntity(this.getBlockPos().offset(pos));
                if(slave != null)
                {
                    slave.masterVector = BlockPos.ZERO;
                }
            }
        }

        if(!this.isMaster() && this.getMasterRelativePosition() != null)
        {
            MultiBlockEntity master = (MultiBlockEntity) level.getBlockEntity(this.getBlockPos().offset(masterVector));
            if(master != null)
            {
                BlockPos pos = this.getBlockPos().subtract(master.getBlockPos());
                master.slaveVectors.remove(pos);
            }
        }


        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if(!this.isMaster() && this.getMasterRelativePosition() != BlockPos.ZERO)
        {
            CompoundTag masterPos = new CompoundTag();
            masterPos.putInt("x", this.masterVector.getX());
            masterPos.putInt("y", this.masterVector.getY());
            masterPos.putInt("z", this.masterVector.getZ());

            tag.put("master_pos", masterPos);
        }
        if(this.isMaster())
        {
            ListTag listPos = new ListTag();
            for (BlockPos pos : this.getSlaveRelativePositions())
            {
                CompoundTag posData = new CompoundTag();
                posData.putInt("x", pos.getX());
                posData.putInt("y", pos.getY());
                posData.putInt("z", pos.getZ());
                listPos.add(posData);
            }
            tag.put("slaves", listPos);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if (!this.isMaster())
        {
            CompoundTag masterTag = tag.getCompound("master_pos");
            int x = masterTag.getInt("x");
            int y = masterTag.getInt("y");
            int z = masterTag.getInt("z");

            this.masterVector = new BlockPos(x, y, z);
        }

        if (this.isMaster())
        {
            tag.getList("slaves", Tag.TAG_COMPOUND).forEach(compound -> {
                int x = ((CompoundTag) compound).getInt("x");
                int y = ((CompoundTag) compound).getInt("y");
                int z = ((CompoundTag) compound).getInt("z");

                BlockPos pos = new BlockPos(x, y, z);
                this.slaveVectors.add(pos);
            });
        }
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
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
