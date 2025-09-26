package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.blocks.RocketPadBlock;
import net.mistersecret312.blocks.MultiblockBlock;
import net.mistersecret312.data.RocketPads;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.BlockInit;

import java.util.Comparator;
import java.util.List;

public class RocketPadBlockEntity extends MultiBlockEntity
{
    public RocketPadBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.ROCKET_PAD.get(), pPos, pBlockState);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if(level == null || level.getServer() == null || level.isClientSide)
            return;

        if(this.isMaster())
            RocketPads.get(level.getServer()).addRocketPad(uuid, getBlockPos(), level.dimension());
    }

    public int getXSize()
    {
        int size = 1;
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return size;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getX()));
        size += parts.get(parts.size()-1).getBlockPos().getX()-parts.get(0).getBlockPos().getX();

        return size;
    }

    public int getZSize()
    {
        int size = 1;
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return size;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getZ()));
        size += parts.get(parts.size()-1).getBlockPos().getZ()-parts.get(0).getBlockPos().getZ();

        return size;
    }

    @Override
    public boolean findingPartsCheck(BlockPos pos, List<MultiBlockEntity> blockEntity)
    {
        return pos.getY() == this.getBlockPos().getY();
    }

    @Override
    public boolean shouldSkip(Level level, BlockPos pos, Block masterBlock, BlockEntity be)
    {
        if(be.getBlockState().is(masterBlock) || be.getBlockState().is(BlockInit.EXHAUST_GRATE.get())
           || be.getBlockState().is(BlockInit.ROCKET_PAD.get()))
            return false;

        return super.shouldSkip(level, pos, masterBlock, be);
    }

    @Override
    public void updateMaster()
    {
        if(level == null || level.isClientSide() || level.getServer() == null)
            return;

        RocketPads.get(level.getServer()).addRocketPad(uuid, getBlockPos(), level.dimension());
    }

    public boolean isComplete()
    {
        if(this.getTower() == null)
            return false;

        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return false;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getX()));
        int minX = parts.get(0).getBlockPos().getX();
        int maxX = parts.get(parts.size()-1).getBlockPos().getX();

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getZ()));
        int minZ = parts.get(0).getBlockPos().getZ();
        int maxZ = parts.get(parts.size()-1).getBlockPos().getZ();

        return level.getBlockStates(new AABB(minX, this.getBlockPos().getY(), minZ, maxX, this.getBlockPos().getY(), maxZ)).allMatch(state -> state.getBlock() instanceof RocketPadBlock);
    }

    public AABB getOnPadBox()
    {
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty() || this.getTower() == null)
            return new AABB(this.getBlockPos());

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getX()));
        int minX = parts.get(0).getBlockPos().getX();
        int maxX = parts.get(parts.size()-1).getBlockPos().getX();

        int minY = this.getBlockPos().getY()+1;
        int maxY = this.getBlockPos().getY()+getTower().getHeight();

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getZ()));
        int minZ = parts.get(0).getBlockPos().getZ();
        int maxZ = parts.get(parts.size()-1).getBlockPos().getZ();

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public LaunchTowerBlockEntity getTower()
    {
        Level level = this.getLevel();
        if(level == null)
            return null;

        for (MultiBlockEntity part : MultiblockBlock.findAllParts(level, this.getBlockPos()))
        {
            for(Direction direction : Direction.values())
            {
                BlockEntity blockEntity = level.getBlockEntity(part.getBlockPos().relative(direction));
                if(blockEntity instanceof LaunchTowerBlockEntity tower)
                    return tower;
            }
        }

        return null;
    }
}
