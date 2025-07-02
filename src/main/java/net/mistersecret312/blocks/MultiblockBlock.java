package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.mistersecret312.block_entities.MultiBlockEntity;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class MultiblockBlock extends BaseEntityBlock
{
    public static final BooleanProperty MASTER = BooleanProperty.create("master");

    public MultiblockBlock(Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(MASTER, false));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston)
    {
        if(level.getBlockEntity(pos) instanceof MultiBlockEntity && level.getBlockEntity(neighborPos) instanceof MultiBlockEntity)
        {
            MultiBlockEntity self = (MultiBlockEntity) level.getBlockEntity(pos);
            MultiBlockEntity neighbor = (MultiBlockEntity) level.getBlockEntity(neighborPos);

            if(self.isMaster() && !neighbor.isMaster())
            {
                neighbor.masterVector = pos.subtract(neighborPos);
                self.slaveVectors.add(neighborPos.subtract(pos));
            }
            if(!self.isMaster() && !neighbor.isMaster())
            {
                if(self.getMasterRelativePosition() == BlockPos.ZERO)
                {
                    level.setBlock(pos, state.setValue(MASTER, true), 1);
                    neighbor.masterVector = pos.subtract(neighborPos);
                    self.slaveVectors.add(neighborPos.subtract(pos));
                }
                else
                {
                    BlockPos neighborstuff = self.masterVector.offset(pos.subtract(neighborPos));
                    neighbor.masterVector = neighborstuff;
                    BlockPos masterPos = neighborPos.offset(neighbor.masterVector);
                    MultiBlockEntity master = (MultiBlockEntity) level.getBlockEntity(masterPos);
                    if(master != null)
                    {
                        BlockPos stuffPos = neighborPos.subtract(master.getBlockPos());
                        master.slaveVectors.add(stuffPos);
                    }
                }
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(MASTER, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(MASTER);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return null;
    }
}
