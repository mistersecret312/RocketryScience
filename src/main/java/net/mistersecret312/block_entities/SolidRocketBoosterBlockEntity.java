package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.blocks.SolidRocketBoosterNozzleBlock;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import org.jetbrains.annotations.Nullable;

public class SolidRocketBoosterBlockEntity extends RocketEngineBlockEntity
{
    public int fuelTicker = 0;

    public SolidRocketBoosterBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.SRB.get(), pos, state);
        this.mass = 25000;
        this.efficiency = 250;
        this.thrust = 10000;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolidRocketBoosterBlockEntity rocketEngine)
    {
        if(level.isClientSide())
            return;

        BlockPos nozzlePos = pos;
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(SolidRocketBoosterNozzleBlock.FACING)))
        {
            rocketEngine.setBuilt(true);
            if (rocketEngine.isRunning() && rocketEngine.getFuelTank() != null)
            {
                if (!rocketEngine.hasPropellantMixture() || !level.hasNeighborSignal(pos))
                    rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);
                else
                {
                    if(rocketEngine.soundTick == 0 && rocketEngine.isRunning)
                    {
                        NetworkInit.sendToTracking(rocketEngine, new RocketEngineSoundPacket(rocketEngine.worldPosition, false));
                        rocketEngine.soundTick = 50;
                    }
                    rocketEngine.soundTick--;
                    if(rocketEngine.fuelTicker > 2)
                    {
                        rocketEngine.getFuelTank().increaseStored(-1);
                        rocketEngine.fuelTicker = 0;
                    }
                    rocketEngine.fuelTicker++;
                    rocketEngine.setThrottle(15);
                    rocketEngine.setRuntime(rocketEngine.runtime+1);
                    if (nozzleState.getValue(NozzleBlock.HOT) < 3 && level.getGameTime() % 200 == 0)
                    {
                        int targetHotness = Math.min(3, nozzleState.getValue(NozzleBlock.HOT) + 1);
                        targetHotness = 0;
                        BlockState targetNozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
                        level.setBlock(nozzlePos, targetNozzleState, 2);
                    }
                }
            }

            if (!rocketEngine.isRunning())
            {
                if (level.hasNeighborSignal(pos) && rocketEngine.hasPropellantMixture())
                {
                    rocketEngine.setRunning(true);
                    level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.ACTIVE, true), 2);
                }
                else rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);

                if (nozzleState.getValue(NozzleBlock.HOT) > 0 && level.getGameTime() % 400 == 0)
                {
                    int targetHotness = Math.max(0, nozzleState.getValue(NozzleBlock.HOT) - 1);
                    targetHotness = 0;
                    BlockState targetNozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
                    level.setBlock(nozzlePos, targetNozzleState, 2);
                }
            }
        } else rocketEngine.setBuilt(false);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
    }

    @Override
    public boolean hasPropellantMixture()
    {
        if(this.getFuelTank() != null)
            return this.getFuelTank().getFuelStored() > 0;

        return false;
    }

    @Nullable
    public SolidFuelTankBlockEntity getFuelTank()
    {
        BlockEntity blockEntity = this.level.getBlockEntity(this.getBlockPos().offset(this.getBlockState().getValue(SolidRocketBoosterNozzleBlock.FACING).getNormal()));
        if(blockEntity != null && blockEntity instanceof SolidFuelTankBlockEntity fuelTank)
        {
            if(fuelTank.isMaster())
                return fuelTank;
            else if(fuelTank.getMasterRelativePosition() != BlockPos.ZERO)
            {
                return fuelTank.getMaster();
            }
        }

        return null;
    }

    @Override
    public @Nullable BlockState getNozzle()
    {
        return this.getBlockState();
    }

    @Override
    public @Nullable BlockPos getNozzlePos()
    {
        return this.getBlockPos();
    }
}
