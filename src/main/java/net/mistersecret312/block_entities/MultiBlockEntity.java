package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.MultiblockBlock;

import java.util.*;

public class MultiBlockEntity extends BlockEntity
{
    public BlockPos masterVector = BlockPos.ZERO;
    public UUID uuid = UUID.randomUUID();
    public LinkedHashSet<BlockPos> slaveVectors = new LinkedHashSet<>();

    public MultiBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        CompoundTag masterPos = new CompoundTag();
        masterPos.putInt("x", this.masterVector.getX());
        masterPos.putInt("y", this.masterVector.getY());
        masterPos.putInt("z", this.masterVector.getZ());

        tag.put("master_pos", masterPos);
        tag.putUUID("uuid", uuid);

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

        CompoundTag masterTag = tag.getCompound("master_pos");
        int x = masterTag.getInt("x");
        int y = masterTag.getInt("y");
        int z = masterTag.getInt("z");

        this.masterVector = new BlockPos(x, y, z);
        this.uuid = tag.getUUID("uuid");

        if (this.masterVector != BlockPos.ZERO)
        {
            LinkedHashSet<BlockPos> slaves = new LinkedHashSet<>();
            tag.getList("slaves", Tag.TAG_COMPOUND).forEach(compound ->
            {
                int slaveX = ((CompoundTag) compound).getInt("x");
                int slaveY = ((CompoundTag) compound).getInt("y");
                int slaveZ = ((CompoundTag) compound).getInt("z");

                BlockPos pos = new BlockPos(slaveX, slaveY, slaveZ);
                slaves.add(pos);
            });

            this.slaveVectors = slaves;
        }
    }

    public boolean findingPartsCheck(BlockPos pos, List<MultiBlockEntity> blockEntity)
    {
        return true;
    }

    public void updateMaster()
    {

    }

    public boolean isMaster()
    {
        return !this.slaveVectors.isEmpty();
    }

    public BlockPos getMasterRelativePosition()
    {
        return masterVector;
    }

    public LinkedHashSet<BlockPos> getSlaveRelativePositions()
    {
        return slaveVectors;
    }
}
