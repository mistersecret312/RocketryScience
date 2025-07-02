package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.capabilities.BlueprintDataCapability;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.*;
import net.mistersecret312.mishaps.Mishap;
import net.mistersecret312.mishaps.MishapType;
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import net.mistersecret312.network.packets.RocketEngineUpdatePacket;
import net.mistersecret312.sound.RocketEngineSoundWrapper;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static net.mistersecret312.blocks.CombustionChamberBlock.FACING;

public class RocketEngineBlockEntity extends BlueprintBlockEntity
{
    public static final int COMBUSTION_CHAMBER_CAPACITY = 1000;

    public int blueprintID = 0;
    public boolean isRunning = false;
    public boolean isBuilt = false;
    public int throttle = 0;
    public double integrity = 0;
    public double maxIntegrity = 0;
    public double reliability = 0;

    public int animTick = 0;
    public int soundTick = 0;
    public int frame = 0;

    @Nullable
    public RocketEngineSoundWrapper runningSound = null;

    public List<Mishap<RocketEngineBlockEntity, ?>> mishaps = new ArrayList<>();

    public double runtime = 0;

    public RocketEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        updateBlueprintData();
        if(this.integrity <= 0)
            this.integrity = this.maxIntegrity;
    }

    public void updateBlueprintData()
    {
        RocketEngineBlueprint blueprint = this.getBlueprint();
        if(blueprint != null)
        {
            this.maxIntegrity = blueprint.maxIntegrity;
            if(this.integrity > this.maxIntegrity)
                this.integrity = this.maxIntegrity;
        }
    }

    public boolean hasPropellantMixture()
    {
        return true;
    }

    @Nullable
    public RocketEngineBlueprint getBlueprint()
    {
        LazyOptional<BlueprintDataCapability> lazyCapability = this.level.getCapability(CapabilityInit.BLUEPRINTS_DATA);
        if(lazyCapability.isPresent())
        {
            Optional<BlueprintDataCapability> optionalCapability = lazyCapability.resolve();
            if(optionalCapability.isPresent())
                return optionalCapability.get().rocketEngineBlueprints.get(this.getBlueprintID());
        }
        return null;
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

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        ListTag listTag = new ListTag();
        this.mishaps.forEach(mishap -> {
            listTag.add(mishap.writeToNBT());
            mishap.removeFromPhysical(this);
        });
        tag.put("mishaps", listTag);

        tag.putBoolean("is_running", this.isRunning);
        tag.putBoolean("is_built", this.isBuilt);
        tag.putInt("throttle", this.throttle);
        tag.putDouble("integrity", this.integrity);
        tag.putDouble("max_integrity", this.maxIntegrity);
        tag.putDouble("reliability", this.reliability);
        tag.putDouble("runtime", this.runtime);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.isRunning = tag.getBoolean("is_running");
        this.isBuilt = tag.getBoolean("is_built");
        this.throttle = tag.getInt("throttle");
        this.integrity = tag.getDouble("integrity");
        this.maxIntegrity = tag.getDouble("max_integrity");
        this.reliability = tag.getDouble("reliability");
        this.runtime = tag.getDouble("runtime");

        List<Mishap<RocketEngineBlockEntity, ?>> mishaps = new ArrayList<>();
        ListTag mishapsTag = tag.getList("mishaps", CompoundTag.TAG_COMPOUND);
        mishapsTag.forEach(listTag -> {
            CompoundTag mishapTag = ((CompoundTag) listTag);
            Mishap<RocketEngineBlockEntity, ?> mishap = Mishap.loadBlockEntityStatic(mishapTag, this);
            if(mishap != null)
            {
                mishap.loadFromNBT(mishapTag);
                mishap.applyToPhysical(this);
                mishaps.add(mishap);
            }
        });
        this.mishaps = mishaps;
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

    @Override
    public void setBlueprintID(int blueprintID)
    {
        this.blueprintID = blueprintID;
        setChanged();
    }

    public void setReliability(double reliability)
    {
        this.reliability = reliability;
        setChanged();
    }

    public void setBuilt(boolean built)
    {
        isBuilt = built;
        NetworkInit.sendToTracking(this, new RocketEngineUpdatePacket(this.getBlockPos(), isBuilt, isRunning, throttle));
        setChanged();
    }

    public void setIntegrity(double integrity)
    {
        this.integrity = integrity;
        setChanged();
    }

    public void setMaxIntegrity(double maxIntegrity)
    {
        this.maxIntegrity = maxIntegrity;
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
