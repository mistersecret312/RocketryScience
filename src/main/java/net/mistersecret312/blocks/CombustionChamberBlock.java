package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CombustionChamberBlock extends Block
{
    public static VoxelShape SHAPE_NORTH = Shapes.join(Block.box(0, 0, 0, 16, 16, 4), Block.box(2, 2, 4, 14, 14, 16), BooleanOp.OR);
    public static VoxelShape SHAPE_SOUTH = Shapes.join(Block.box(0, 0, 12, 16, 16, 16), Block.box(2, 2, 0, 14, 14, 12), BooleanOp.OR);
    public static VoxelShape SHAPE_EAST = Shapes.join(Block.box(12, 0, 0, 16, 16, 16), Block.box(0, 2, 2, 12, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_WEST = Shapes.join(Block.box(0, 0, 0, 4, 16, 16), Block.box(4, 2, 2, 16, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_UP = Shapes.join(Block.box(0, 12, 0, 16, 16, 16), Block.box(2, 0, 2, 14, 12, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_DOWN = Shapes.join(Block.box(0, 0, 0, 16, 4, 16), Block.box(2, 4, 2, 14, 16, 14), BooleanOp.OR);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CombustionChamberBlock(Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }
}
