package net.mistersecret312.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.mistersecret312.init.BlockInit;
import org.jetbrains.annotations.Nullable;

public class NozzleBlock extends Block
{
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

    public boolean isVacuum()
    {
        return isVacuum;
    }

    public boolean isLiquidPropellant()
    {
        return isLiquidPropellant;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        LevelAccessor accessor = context.getLevel();
        BlockState blockState = accessor.getBlockState(context.getClickedPos().relative(context.getNearestLookingDirection()));
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection()).setValue(HOT, 0).setValue(ACTIVE, false)
                .setValue(HAS_PIPE, blockState.getBlock() instanceof CombustionChamberBlock && blockState.getValue(FACING) == context.getNearestLookingDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HOT, ACTIVE, HAS_PIPE);
        super.createBlockStateDefinition(builder);
    }
}
