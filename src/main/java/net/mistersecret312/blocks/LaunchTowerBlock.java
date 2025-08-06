package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.block_entities.LaunchTowerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LaunchTowerBlock extends MultiblockBlock
{

    public LaunchTowerBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
                                 BlockHitResult pHit)
    {
        if(!pLevel.isClientSide() && pLevel.getBlockEntity(pPos) instanceof LaunchTowerBlockEntity tower)
        {
            System.out.println("Height: " + tower.getHeight());
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LaunchTowerBlockEntity(pos, state);
    }
}
