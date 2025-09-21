package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.block_entities.RocketPadBlockEntity;
import org.jetbrains.annotations.Nullable;

public class RocketPadBlock extends MultiblockBlock
{
    public RocketPadBlock(Properties pProperties)
    {
        super(pProperties);
    }



    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new RocketPadBlockEntity(pos, state);
    }
}
