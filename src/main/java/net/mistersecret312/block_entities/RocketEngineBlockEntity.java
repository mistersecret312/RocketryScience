package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.init.*;
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import net.mistersecret312.network.packets.RocketEngineUpdatePacket;
import net.mistersecret312.sound.RocketEngineSoundWrapper;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

import static net.mistersecret312.blocks.CombustionChamberBlock.FACING;

public class RocketEngineBlockEntity extends BlockEntity
{
    public static final int COMBUSTION_CHAMBER_CAPACITY = 1000;

    public boolean isRunning = false;
    public boolean isBuilt = false;
    public int throttle = 0;

    public double mass;
    public double thrust;
    public double efficiency;

    public int animTick = 0;
    public int soundTick = 0;
    public int frame = 0;

    @Nullable
    public RocketEngineSoundWrapper runningSound = null;
    public double runtime = 0;

    public RocketEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
    }

    public boolean hasPropellantMixture()
    {
        return true;
    }

    @Nullable
    public BlockState getNozzle()
    {
        return null;
    }

    @Nullable
    public BlockPos getNozzlePos()
    {
        return null;
    }

    public void deactiveEngine(RocketEngineBlockEntity rocketEngine, BlockPos nozzlePos, BlockState nozzleState)
    {
        rocketEngine.setRunning(false);
        rocketEngine.setThrottle(0);
        rocketEngine.soundTick = 0;
        NetworkInit.sendToTracking(rocketEngine, new RocketEngineSoundPacket(rocketEngine.worldPosition, true));
        level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.ACTIVE, false), 2);
    }

    public void setMass(double mass)
    {
        this.mass = trimDouble(mass);
    }

    public void setEfficiency(double efficiency)
    {
        this.efficiency = trimDouble(efficiency);
    }

    public void setThrust(double thrust)
    {
        this.thrust = trimDouble(thrust);
    }

    public double getMass()
    {
        return mass;
    }

    public double getEfficiency()
    {
        return efficiency;
    }

    public double getThrust()
    {
        return thrust;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putBoolean("is_running", this.isRunning);
        tag.putBoolean("is_built", this.isBuilt);
        tag.putInt("throttle", this.throttle);
        tag.putDouble("mass", this.mass);
        tag.putDouble("thrust", this.thrust);
        tag.putDouble("efficiency", this.efficiency);
        tag.putDouble("runtime", this.runtime);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.isRunning = tag.getBoolean("is_running");
        this.isBuilt = tag.getBoolean("is_built");
        this.throttle = tag.getInt("throttle");
        this.mass = tag.getDouble("mass");
        this.thrust = tag.getDouble("thrust");
        this.efficiency = tag.getDouble("efficiency");
        this.runtime = tag.getDouble("runtime");
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public void setRunning(boolean running)
    {
        isRunning = running;
        NetworkInit.sendToTracking(this, new RocketEngineUpdatePacket(this.getBlockPos(), isBuilt, isRunning, throttle));
        setChanged();
    }

    public void setBuilt(boolean built)
    {
        isBuilt = built;
        NetworkInit.sendToTracking(this, new RocketEngineUpdatePacket(this.getBlockPos(), isBuilt, isRunning, throttle));
        setChanged();
    }

    public void setRuntime(double runtime)
    {
        this.runtime = runtime;
        setChanged();
    }

    public void setThrottle(int throttle)
    {
        this.throttle = throttle;
        NetworkInit.sendToTracking(this, new RocketEngineUpdatePacket(this.getBlockPos(), isBuilt, isRunning, throttle));
        setChanged();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        if(!this.isRunning())
            return super.getRenderBoundingBox();

        int length = 2+Math.max(1, Math.min(4, this.throttle-2));
        Direction direction = this.getBlockState().getValue(FACING).getOpposite();
        AABB box = super.getRenderBoundingBox().expandTowards(direction.getStepX()*length, direction.getStepY()*length, direction.getStepZ()*length);
        return box;
    }

    public static double trimDouble(double value)
    {
        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(2);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        return Double.parseDouble(fraction.format(value));
    }

    public void stopRunSound()
    {
        this.runningSound.stopSound();
    }

    public void startRunSound()
    {
        if(!this.runningSound.isPlaying())
        {
            this.runningSound.stopSound();
            this.runningSound.playSound();
        }
    }


}
