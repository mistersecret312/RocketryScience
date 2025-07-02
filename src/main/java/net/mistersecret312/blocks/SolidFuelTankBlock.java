package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.mistersecret312.block_entities.SolidFuelTankBlockEntity;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.util.VerticalConnection;
import org.jetbrains.annotations.Nullable;

public class SolidFuelTankBlock extends MultiblockBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<VerticalConnection> CONNECTION = EnumProperty.create("connection", VerticalConnection.class);

    public int fuelCapacity;
    public SolidFuelTankBlock(Properties pProperties, int capacity)
    {
        super(pProperties);
        this.fuelCapacity = capacity;

        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(CONNECTION, VerticalConnection.NONE));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        if(!level.isClientSide() && player.getItemInHand(hand).is(Tags.Items.GUNPOWDER))
        {
            SolidFuelTankBlockEntity blockEntity = (SolidFuelTankBlockEntity) level.getBlockEntity(pos);
            if(blockEntity != null)
            {
                if(blockEntity.isMaster() && blockEntity.getFuelCapacity() > blockEntity.getFuelStored())
                {
                    blockEntity.increaseStored(20);
                    return InteractionResult.SUCCESS;
                }
                else
                {
                    SolidFuelTankBlockEntity master = blockEntity.getMaster();
                    if(master != null && master.getFuelCapacity() > master.getFuelStored())
                    {
                        master.increaseStored(20);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if(level.getBlockState(pos.below()).getBlock() instanceof SolidFuelTankBlock &&
                level.getBlockState(pos.above()).getBlock() instanceof SolidFuelTankBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.MIDDLE), 2);
        else if(level.getBlockState(pos.below()).getBlock() instanceof SolidFuelTankBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.BOTTOM), 2);
        else if(level.getBlockState(pos.above()).getBlock() instanceof SolidFuelTankBlock)
            level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.UP), 2);
        else level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.NONE), 2);

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()).setValue(CONNECTION, VerticalConnection.NONE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, CONNECTION);
        super.createBlockStateDefinition(builder);
    }

    public int getFuelCapacity()
    {
        return fuelCapacity;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return BlockEntityInit.SOLID_FUEL_TANK.get().create(pos, state);
    }
}
