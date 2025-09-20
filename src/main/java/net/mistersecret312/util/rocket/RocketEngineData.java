package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;

import java.util.function.BiFunction;

public class RocketEngineData extends BlockData
{
    public RocketEngineData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
    }

    public RocketEngineData()
    {

    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getRocket().getRocketEntity().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag extraData;
            if(blockEntity instanceof RocketEngineBlockEntity rocketEngine)
            {
                extraData = blockEntity.saveWithId();
                return new RocketEngineData(stage, stage.palette.indexOf(state), pos, extraData);
            }
            if(state.getBlock() instanceof NozzleBlock)
            {
                RocketEngineBlockEntity rocketEngineBlockEntity = (RocketEngineBlockEntity) level.getBlockEntity(pos.relative(state.getValue(NozzleBlock.FACING)));
                if(rocketEngineBlockEntity != null && rocketEngineBlockEntity.isBuilt)
                    return BlockData.VOID;

            }
            return null;
        };
    }

    @Override
    public boolean doesTick(Level level)
    {
        return getNozzle() != null;
    }

    public BlockState getNozzle()
    {
        BlockState nozzleState = this.getStage().blocks.get(this.pos.offset(this.getBlockState().getValue(CombustionChamberBlock.FACING)
                .getOpposite().getNormal())).getBlockState();

        if(nozzleState != null && nozzleState.getBlock() instanceof NozzleBlock)
            return nozzleState;
        else return null;
    }
}
