package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.block_entities.LaunchTowerBlockEntity;
import net.mistersecret312.util.VerticalConnection;
import org.jetbrains.annotations.Nullable;

import static net.mistersecret312.blocks.SolidFuelTankBlock.CONNECTION;

public class LaunchTowerBlock extends MultiblockBlock
{
    public LaunchTowerBlock(Properties pProperties)
    {
        super(pProperties);
        registerDefaultState(this.defaultBlockState().setValue(CONNECTION, VerticalConnection.NONE));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
                                 BlockHitResult pHit)
    {
        if(!pLevel.isClientSide() && pLevel.getBlockEntity(pPos) instanceof LaunchTowerBlockEntity tower)
        {
            //System.out.println("Height: " + tower.getHeight());
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (level.getBlockState(pos.below()).getBlock() instanceof LaunchTowerBlock && level.getBlockState(pos.above()).getBlock() instanceof LaunchTowerBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.MIDDLE), 2);
        else if (level.getBlockState(pos.below()).getBlock() instanceof LaunchTowerBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.BOTTOM), 2);
        else if (level.getBlockState(pos.above()).getBlock() instanceof LaunchTowerBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.UP), 2);
        else level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.NONE), 2);

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.add(CONNECTION);
        super.createBlockStateDefinition(pBuilder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LaunchTowerBlockEntity(pos, state);
    }
}
