package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class CombustionChamberBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<ChamberType> TYPE = EnumProperty.create("type", ChamberType.class);

    public CombustionChamberBlock(Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(TYPE, ChamberType.PIPELESS));
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor)
    {
        if(level.getBlockState(neighbor).getValue(NozzleBlock.FACING).equals(state.getValue(FACING)) && level.getBlockState(neighbor).getBlock() instanceof NozzleBlock nozzle)
        {
            if(nozzle.isVacuum())
                state.setValue(TYPE, ChamberType.VACUUM);
            else if(nozzle.isLiquidPropellant())
                state.setValue(TYPE, ChamberType.ATMOSPHERE);
            else state.setValue(TYPE, ChamberType.PIPELESS);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection()).setValue(TYPE, ChamberType.PIPELESS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, TYPE);
        super.createBlockStateDefinition(builder);
    }

    public enum ChamberType implements StringRepresentable
    {
        VACUUM("vacuum"),
        ATMOSPHERE("atmosphere"),
        PIPELESS("pipeless");

        String name;
        ChamberType(String name)
        {
            this.name = name;
        }

        @Override
        public String getSerializedName()
        {
            return this.name;
        }
    }
}
