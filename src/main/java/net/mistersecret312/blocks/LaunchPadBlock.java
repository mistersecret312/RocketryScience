package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.block_entities.LaunchPadBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LaunchPadBlock extends MultiblockBlock
{

    public LaunchPadBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
                                 BlockHitResult pHit)
    {
        LaunchPadBlockEntity pad = (LaunchPadBlockEntity) pLevel.getBlockEntity(pPos);
        if(pad != null && !pLevel.isClientSide())
        {
            //System.out.println("X size: " + pad.getXSize());
            //System.out.println("Z size: " + pad.getZSize());
            //System.out.println("Complete: " + pad.isComplete());
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LaunchPadBlockEntity(pos, state);
    }
}
