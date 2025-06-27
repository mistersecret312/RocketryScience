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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RocketEngineBlockEntity extends BlockEntity
{
    public static final int COMBUSTION_CHAMBER_CAPACITY = 1000;

    public int blueprintID = 0;
    public boolean isRunning = false;

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
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();

        holder.invalidate();
    }

    public void updateFuelTank()
    {
        this.level.getCapability(CapabilityInit.BLUEPRINTS_DATA).ifPresent(cap ->
        {
            RocketEngineBlueprint blueprint = cap.rocketEngineBlueprints.get(this.blueprintID);
            if(!this.fuelTank.getPropellantTypes().equals(blueprint.rocketFuel.getPropellants()))
                this.fuelTank = new RocketFuelTank(blueprint.rocketFuel.getPropellants(), COMBUSTION_CHAMBER_CAPACITY);
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

    public static void tick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity rocketEngine)
    {
        BlockPos nozzlePos = pos.relative(state.getValue(CombustionChamberBlock.FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(!level.isClientSide() && nozzleState.getBlock() instanceof NozzleBlock)
        {
            if (rocketEngine.isRunning() && !rocketEngine.hasPropellantMixture())
            {
                rocketEngine.setRunning(false);
                level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.ACTIVE, false), 2);
            }

            if (rocketEngine.isRunning() && rocketEngine.hasPropellantMixture())
            {
                rocketEngine.fuelTank.drain(2, IFluidHandler.FluidAction.EXECUTE);

                if (nozzleState.getValue(NozzleBlock.HOT) < 3 && level.getGameTime() % 200 == 0)
                {
                    level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.HOT, nozzleState.getValue(NozzleBlock.HOT) + 1), 2);
                }
            }
            if(!rocketEngine.isRunning())
            {
                if (nozzleState.getValue(NozzleBlock.HOT) > 0 && level.getGameTime() % 400 == 0)
                {
                    level.setBlock(nozzlePos, nozzleState.setValue(NozzleBlock.HOT, nozzleState.getValue(NozzleBlock.HOT) - 1), 2);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("blueprint_id", this.blueprintID);
        tag.putBoolean("is_running", this.isRunning);
        fuelTank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.blueprintID = tag.getInt("blueprint_id");
        this.isRunning = tag.getBoolean("is_running");
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
