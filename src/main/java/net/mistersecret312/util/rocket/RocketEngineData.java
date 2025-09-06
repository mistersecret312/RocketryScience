package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;

public class RocketEngineData extends BlockData
{
    public RocketEngineData(Stage stage, BlockState state, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
    }

    @Override
    public boolean doesTick(Level level)
    {
        return this.getStage().blocks.get(this.pos.offset(this.state.getValue(CombustionChamberBlock.FACING)
                .getOpposite().getNormal())).state.getBlock() instanceof NozzleBlock;
    }
}
