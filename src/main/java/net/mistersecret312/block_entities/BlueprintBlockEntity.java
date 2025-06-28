package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlueprintBlockEntity extends BlockEntity
{
    public int blueprintID = 0;

    public BlueprintBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }

    public int getBlueprintID()
    {
        return blueprintID;
    }

    public void setBlueprintID(int blueprintID)
    {
        this.blueprintID = blueprintID;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("blueprint", this.blueprintID);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.blueprintID = tag.getInt("blueprint");
    }
}
