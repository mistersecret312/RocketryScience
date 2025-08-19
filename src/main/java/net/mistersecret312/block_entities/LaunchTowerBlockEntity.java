package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.LaunchPadBlock;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.CTM;

import java.util.List;

public class LaunchTowerBlockEntity extends MultiBlockEntity
{
    public LaunchTowerBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.LAUNCH_TOWER.get(), pPos, pBlockState);
    }

    public int getHeight()
    {
        int height = 0;

        if(!this.isMaster())
        {
            LaunchTowerBlockEntity master = getMaster();
            if(master != null && master != this)
                return master.getHeight();
            else return 0;
        }
        while(level.getBlockEntity(getBlockPos().relative(Direction.UP, height+1)) != null
                && level.getBlockEntity(getBlockPos().relative(Direction.UP, height+1)) instanceof LaunchTowerBlockEntity)
            height++;

        return height;
    }

    public LaunchTowerBlockEntity getMaster()
    {
        if(this.getMasterRelativePosition() != BlockPos.ZERO)
            return (LaunchTowerBlockEntity) this.getLevel().getBlockEntity(this.getBlockPos().offset(masterVector));
        else return null;
    }

    @Override
    public boolean findingPartsCheck(BlockPos pos, List<MultiBlockEntity> blockEntity)
    {
        return pos.getX() == this.getBlockPos().getX()
                && pos.getZ() == this.getBlockPos().getZ();
    }

    @Override
    public boolean isMaster()
    {
        for(Direction direction : Direction.values())
        {
            if(level.getBlockState(this.getBlockPos().relative(direction)).getBlock() instanceof LaunchPadBlock)
                return true;
        }

        return false;
    }
}
