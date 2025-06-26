package net.mistersecret312.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class NozzleBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty HOT = IntegerProperty.create("hot", 0, 3);
    public boolean isVacuum;
    public boolean isLiquidPropellant;

    public NozzleBlock(Properties pProperties, boolean isVacuum, boolean isLiquidPropellant)
    {
        super(pProperties);
        this.isVacuum = isVacuum;
        this.isLiquidPropellant = isLiquidPropellant;

        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HOT, 0));
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
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection()).setValue(HOT, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HOT);
        super.createBlockStateDefinition(builder);
    }
}
