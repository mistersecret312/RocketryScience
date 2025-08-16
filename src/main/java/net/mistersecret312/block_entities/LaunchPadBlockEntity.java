package net.mistersecret312.block_entities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.blocks.LaunchPadBlock;
import net.mistersecret312.blocks.MultiblockBlock;
import net.mistersecret312.init.BlockEntityInit;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class LaunchPadBlockEntity extends MultiBlockEntity
{
    public LaunchPadBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.LAUNCH_PAD.get(), pPos, pBlockState);
    }

    public int getXSize()
    {
        int size = 1;
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return size;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getX()));
        size += parts.get(parts.size()-1).getBlockPos().getX()-parts.get(0).getBlockPos().getX();

        return Math.min(5, size);
    }

    public int getZSize()
    {
        int size = 1;
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return size;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getZ()));
        size += parts.get(parts.size()-1).getBlockPos().getZ()-parts.get(0).getBlockPos().getZ();

        return Math.min(5, size);
    }

    @Override
    public boolean findingPartsCheck(BlockPos pos, List<MultiBlockEntity> blockEntity)
    {
        int distanceX = pos.getX()-this.getBlockPos().getX();
        int distanceZ = pos.getZ()-this.getBlockPos().getZ();
        System.out.println(distanceX);
        return pos.getY() == this.getBlockPos().getY();
    }

    public boolean isComplete()
    {
        List<MultiBlockEntity> parts = MultiblockBlock.findAllParts(this.getLevel(), this.getBlockPos());
        if(parts.isEmpty())
            return false;

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getX()));
        int minX = parts.get(0).getBlockPos().getX();
        int maxX = parts.get(parts.size()-1).getBlockPos().getX();

        parts.sort(Comparator.comparing(part -> part.getBlockPos().getZ()));
        int minZ = parts.get(0).getBlockPos().getZ();
        int maxZ = parts.get(parts.size()-1).getBlockPos().getZ();

        return level.getBlockStates(new AABB(minX, this.getBlockPos().getY(), minZ, maxX, this.getBlockPos().getY(), maxZ)).allMatch(state -> state.getBlock() instanceof LaunchPadBlock);
    }
}
