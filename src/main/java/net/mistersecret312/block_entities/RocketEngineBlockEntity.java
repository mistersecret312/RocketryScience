package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.capabilities.BlueprintDataCapability;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.CapabilityInit;
import net.mistersecret312.init.MishapInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.mishaps.Mishap;
import net.mistersecret312.mishaps.MishapType;
import net.mistersecret312.network.packets.RocketEngineUpdatePacket;
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
    public int frame = 0;

    public List<Mishap<RocketEngineBlockEntity, ?>> mishaps = new ArrayList<>();

    public double runtime = 0;

    public RocketFuelTank fuelTank = createTank();
    private LazyOptional<IFluidHandler> holder = LazyOptional.empty();

    public RocketEngineBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.ROCKET_ENGINE.get(), pPos, pBlockState);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        this.holder = LazyOptional.of(() -> fuelTank);
        updateBlueprintData();
        if(this.integrity <= 0)
            this.integrity = this.maxIntegrity;
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();

        holder.invalidate();
    }

    public void updateBlueprintData()
    {
        this.level.getCapability(CapabilityInit.BLUEPRINTS_DATA).ifPresent(cap ->
        {
            RocketEngineBlueprint blueprint = cap.rocketEngineBlueprints.get(this.blueprintID);
            if(!this.fuelTank.getPropellantTypes().equals(blueprint.rocketFuel.getPropellants()))
                this.fuelTank = new RocketFuelTank(blueprint.rocketFuel.getPropellants(), COMBUSTION_CHAMBER_CAPACITY);

            this.maxIntegrity = blueprint.maxIntegrity;
            if(this.integrity > this.maxIntegrity)
                this.integrity = this.maxIntegrity;
        });
    }

    public boolean hasPropellantMixture()
    {
        LazyOptional<BlueprintDataCapability> lazyCapability = this.level.getCapability(CapabilityInit.BLUEPRINTS_DATA);
        if(lazyCapability.isPresent())
        {
            Optional<BlueprintDataCapability> optionalCapability = lazyCapability.resolve();
            if(optionalCapability.isPresent())
            {
                BlueprintDataCapability cap = optionalCapability.get();

                RocketEngineBlueprint blueprint = cap.rocketEngineBlueprints.get(this.blueprintID);
                List<Boolean> hasFuel = new ArrayList<>();
                hasFuel.add(this.fuelTank.getPropellants().stream().allMatch(stack -> stack.getAmount() > 0));
                blueprint.rocketFuel.getPropellants().forEach(type -> {
                    boolean fuelContained = this.fuelTank.getPropellantTypes().contains(type);
                    hasFuel.add(fuelContained);
                });

                boolean hasMixture = hasFuel.stream().allMatch(bool -> bool);
                return hasMixture;
            }
        }
        return false;
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
    public NozzleBlock getNozzle()
    {
        BlockPos nozzlePos = this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite());
        BlockState nozzleState = this.level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock nozzle)
        {
            if(nozzleState.getValue(NozzleBlock.FACING).equals(this.getBlockState().getValue(FACING)))
                return nozzle;
        }
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity rocketEngine)
    {
        if(level.isClientSide())
            return;
        if(rocketEngine.getBlueprint() == null)
            return;

        rocketEngine.getBlueprint().calculateReliability();
        double reliabilityEffects = 0;
        for(Mishap<RocketEngineBlockEntity,?> mishap : rocketEngine.mishaps)
            reliabilityEffects += mishap.getType().physicalEffect;
        rocketEngine.setReliability(trimDouble(rocketEngine.getBlueprint().reliability*Math.max(0.2d, rocketEngine.integrity/rocketEngine.maxIntegrity))+reliabilityEffects);

        BlockPos nozzlePos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(FACING)))
        {
            rocketEngine.setBuilt(true);
            rocketEngine.mishaps.forEach(mishap -> mishap.tickToPhysical(rocketEngine));

            if (rocketEngine.isRunning())
            {
                if (!rocketEngine.hasPropellantMixture())
                    rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);
                else
                {
                    RandomSource random = level.getRandom();
                    if(level.getGameTime() % 20 == 0 && random.nextDouble() > rocketEngine.reliability)
                    {
                        RocketEngineBlueprint blueprint = rocketEngine.getBlueprint();
                        MishapType<?, RocketEngineBlockEntity, RocketEngineBlueprint> mishapType = ((MishapType<?, RocketEngineBlockEntity, RocketEngineBlueprint>) MishapInit.getRandomType(MishapType.MishapTarget.ROCKET_ENGINE));

                        if(mishapType != null && blueprint != null)
                        {
                            Mishap<RocketEngineBlockEntity, ?> mishapBlockEntity = mishapType.create(rocketEngine, null);
                            Mishap<?, RocketEngineBlueprint> mishapBlueprint = mishapType.create(null, blueprint);
                            //rocketEngine.mishaps.add(mishapBlockEntity);
                            //blueprint.mishaps.add(mishapBlueprint);
                            rocketEngine.setChanged();
                        }
                    }

                    //rocketEngine.fuelTank.drain(Math.max(1, 8 * (rocketEngine.throttle / 15)), IFluidHandler.FluidAction.EXECUTE);
                    //rocketEngine.setIntegrity(trimDouble(rocketEngine.integrity - Math.max(0.01, 0.1 * ((double) rocketEngine.throttle / 15))));
                    rocketEngine.setThrottle(level.getBestNeighborSignal(pos));
                    rocketEngine.setRuntime(rocketEngine.runtime+1);
                    if (nozzleState.getValue(NozzleBlock.HOT) < 3 && level.getGameTime() % 200 == 0)
                    {
                        int targetHotness = Math.min(3, nozzleState.getValue(NozzleBlock.HOT) + 1);
                        BlockState targetNozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
                        level.setBlock(nozzlePos, targetNozzleState, 2);
                    }
                    if (!level.hasNeighborSignal(pos))
                        rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);
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
                    BlockState targetNozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
                    level.setBlock(nozzlePos, targetNozzleState, 2);
                }
            }
        } else rocketEngine.setBuilt(false);
    }

    public void deactiveEngine(RocketEngineBlockEntity rocketEngine, BlockPos nozzlePos, BlockState nozzleState)
    {
        rocketEngine.setRunning(false);
        rocketEngine.setThrottle(0);
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
        fuelTank.writeToNBT(tag);
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
        this.fuelTank.readFromNBT(tag);

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
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ForgeCapabilities.FLUID_HANDLER && this.isBuilt)
            return holder.cast();

        return super.getCapability(capability, facing);
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

    public static double trimDouble(double value)
    {
        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(2);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        return Double.parseDouble(fraction.format(value));
    }

    public RocketFuelTank createTank()
    {
        return new RocketFuelTank(RocketFuel.HYDROLOX.getPropellants(), COMBUSTION_CHAMBER_CAPACITY)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }
}
