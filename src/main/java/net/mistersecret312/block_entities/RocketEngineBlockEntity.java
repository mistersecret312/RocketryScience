package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.mistersecret312.blocks.CombustionChamberBlock.FACING;

public class RocketEngineBlockEntity extends BlockEntity
{
    public static final int COMBUSTION_CHAMBER_CAPACITY = 1000;

    public int blueprintID = 0;
    public boolean isRunning = false;
    public int throttle = 0;
    public double integrity = 0;
    public double maxIntegrity = 0;
    public double reliability = 0;

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

    public static void tick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity rocketEngine)
    {
        if(level.isClientSide())
            return;
        if(rocketEngine.getBlueprint() == null)
            return;

        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(2);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        rocketEngine.reliability = Double.parseDouble(fraction.format(rocketEngine.getBlueprint().reliability*Math.max(0.2d, rocketEngine.integrity/rocketEngine.maxIntegrity)));

        BlockPos nozzlePos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(FACING)))
        {
            if (rocketEngine.isRunning())
            {
                if (!rocketEngine.hasPropellantMixture())
                    rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);
                else
                {
                    rocketEngine.fuelTank.drain(Math.max(1, 8 * (rocketEngine.throttle / 15)), IFluidHandler.FluidAction.EXECUTE);
                    rocketEngine.integrity = Double.parseDouble(fraction.format(rocketEngine.integrity - Math.max(0.01, 0.1 * ((double) rocketEngine.throttle / 15))));
                    rocketEngine.throttle = level.getBestNeighborSignal(pos);
                    rocketEngine.runtime++;
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
        }
    }

    public void deactiveEngine(RocketEngineBlockEntity rocketEngine, BlockPos nozzlePos, BlockState nozzleState)
    {
        rocketEngine.setRunning(false);
        rocketEngine.throttle = 0;
        rocketEngine.runtime = 0;
        level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.ACTIVE, false), 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("blueprint_id", this.blueprintID);
        tag.putBoolean("is_running", this.isRunning);
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
        this.blueprintID = tag.getInt("blueprint_id");
        this.isRunning = tag.getBoolean("is_running");
        this.throttle = tag.getInt("throttle");
        this.integrity = tag.getDouble("integrity");
        this.maxIntegrity = tag.getDouble("max_integrity");
        this.reliability = tag.getDouble("reliability");
        this.runtime = tag.getDouble("runtime");
        this.fuelTank.readFromNBT(tag);
    }

    public int getBlueprintID()
    {
        return blueprintID;
    }

    public void setBlueprintID(int blueprintID)
    {
        this.blueprintID = blueprintID;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public void setRunning(boolean running)
    {
        isRunning = running;
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ForgeCapabilities.FLUID_HANDLER)
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
