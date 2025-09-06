package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockData
{
    public BlockPos pos;
    public BlockState state;
    public CompoundTag extraData;

    private Stage stage;

    public BlockData(Stage stage, BlockState state, BlockPos pos, CompoundTag tag)
    {
        this.state = state;
        this.pos = pos;
        this.extraData = tag;

        this.stage = stage;
    }

    public void tick(Level level)
    {

    }

    public boolean doesTick(Level level)
    {
        return false;
    }

    public void placeInLevel(Level level, BlockPos pos)
    {
        level.setBlock(pos, this.state, Block.UPDATE_ALL);
        if(extraData == null || extraData.isEmpty())
            return;

        BlockEntity.loadStatic(pos, this.state, this.extraData);
    }

    public Stage getStage()
    {
        return stage;
    }
}
