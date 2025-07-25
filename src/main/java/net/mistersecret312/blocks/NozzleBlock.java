package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NozzleBlock extends Block
{
    public static VoxelShape SHAPE_NORTH = Shapes.join(Block.box(3, 3, 0, 13, 13, 6), Block.box(2, 2, 6, 14, 14, 16), BooleanOp.OR);
    public static VoxelShape SHAPE_SOUTH = Shapes.join(Block.box(3, 3, 10, 13, 13, 16), Block.box(2, 2, 0, 14, 14, 10), BooleanOp.OR);
    public static VoxelShape SHAPE_EAST = Shapes.join(Block.box(10, 3, 3, 16, 13, 13), Block.box(0, 2, 2, 10, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_WEST = Shapes.join(Block.box(0, 3, 3, 6, 13, 13), Block.box(6, 2, 2, 16, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_UP = Shapes.join(Block.box(3, 10, 3, 13, 16, 13), Block.box(2, 0, 2, 14, 10, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_DOWN = Shapes.join(Block.box(3, 0, 3, 13, 6, 13), Block.box(2, 6, 2, 14, 16, 14), BooleanOp.OR);
    
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty HOT = IntegerProperty.create("hot", 0, 3);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty HAS_PIPE = BooleanProperty.create("has_pipe");
    public boolean isVacuum;
    public boolean isLiquidPropellant;

    public NozzleBlock(Properties pProperties, boolean isVacuum, boolean isLiquidPropellant)
    {
        super(pProperties);
        this.isVacuum = isVacuum;
        this.isLiquidPropellant = isLiquidPropellant;

        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HOT, 0).setValue(ACTIVE, false).setValue(HAS_PIPE, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        if(state.getValue(HOT) > 0)
        {
            entity.hurt(entity.damageSources().hotFloor(), 1);
        }
    }


    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (isOnEngine(pLevel, pPos, pState)) {
            pLevel.setBlock(pPos, pState.setValue(HAS_PIPE, true), 11);
        } else {
            pLevel.setBlock(pPos, pState.setValue(HAS_PIPE, false), 11);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    public boolean isOnEngine(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction blockDirection = pState.getValue(FACING);
        Block parentBlock = pLevel.getBlockState(pPos.relative(blockDirection)).getBlock();
        return parentBlock instanceof CombustionChamberBlock && pLevel.getBlockState(pPos.relative(blockDirection)).getValue(FACING) == blockDirection;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        LevelAccessor accessor = context.getLevel();
        Player player = context.getPlayer();
        Direction facing = context.getClickedFace().getOpposite();
        BlockState blockState = accessor.getBlockState(context.getClickedPos().relative(facing));
        return this.defaultBlockState().setValue(FACING, facing).setValue(HOT, 0).setValue(ACTIVE, false)
                .setValue(HAS_PIPE, blockState.getBlock() instanceof CombustionChamberBlock && blockState.getValue(FACING) == facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HOT, ACTIVE, HAS_PIPE);
        super.createBlockStateDefinition(builder);
    }

    public boolean isVacuum()
    {
        return isVacuum;
    }

    public boolean isLiquidPropellant()
    {
        return isLiquidPropellant;
    }
}
