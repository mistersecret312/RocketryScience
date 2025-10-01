package net.mistersecret312.block_entities;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.items.CombustionChamberItem;
import net.mistersecret312.items.TurboPumpItem;
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.mistersecret312.blocks.CombustionChamberBlock.FACING;

public class LiquidRocketEngineBlockEntity extends RocketEngineBlockEntity
{
    public RocketFuelTank fuelTank = createTank();
    public LazyOptional<IFluidHandler> fluidHolder = LazyOptional.empty();

    public ItemStackHandler handler = createHandler();

    public RocketFuel rocketFuel;

    public LiquidRocketEngineBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.ROCKET_ENGINE.get(), pos, state);
    }

    @Override
    public void onLoad()
    {
        this.fluidHolder = LazyOptional.of(() -> fuelTank);
        super.onLoad();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if(handler.getStackInSlot(i).isEmpty())
            {
                this.efficiency = 0;
                this.thrust = 0;
                this.mass = 0;
                this.rocketFuel = null;
            }
        }
        double materialEfficiencyMult = 0;
        double materialThrustMult = 0;
        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack stack = handler.getStackInSlot(i);
            if(stack.getItem() instanceof CombustionChamberItem chamber)
            {
                RocketFuel fuelType = chamber.getFuelType(stack);
                RocketMaterial material = chamber.getMaterial(stack);

                if(fuelType == null || material == null)
                    return;

                this.rocketFuel = fuelType;
                this.setMass(2500*material.getMassCoefficient());
                this.setThrust(fuelType.getThrustKiloNewtons());
                this.setEfficiency(fuelType.getEfficiency());
                materialEfficiencyMult += material.getEfficiencyCoefficient();
                materialThrustMult += material.getThrustCoefficient();
            }
            else if(stack.getItem() instanceof TurboPumpItem pump)
            {
                RocketMaterial material = pump.getMaterial(stack);

                if(material == null)
                    return;

                this.setMass(this.getMass()+(1000*material.getMassCoefficient()));
                materialThrustMult += material.getThrustCoefficient();
                materialEfficiencyMult += material.getEfficiencyCoefficient();
            }
        }
        this.setThrust(this.getThrust()*(materialThrustMult/3));
        this.setEfficiency(this.getEfficiency()*(materialEfficiencyMult/3));
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();

        fluidHolder.invalidate();
    }

    public boolean isVacuum()
    {
        if(getNozzle() != null && getNozzle().getBlock() instanceof NozzleBlock nozzle)
            return nozzle.isVacuum();

        return false;
    }

    @Override
    public boolean hasPropellantMixture()
    {
        RocketFuel fuelType = rocketFuel;

        List<Boolean> hasFuel = new ArrayList<>();
        hasFuel.add(this.fuelTank.getPropellants().stream().allMatch(fluidStack -> fluidStack.getFluidAmount() > 0));
        for (int i = 0; i < this.fuelTank.getTanks(); i++)
            hasFuel.add(fuelType.getPropellants().get(i).test(this.fuelTank.getFluidInTank(i)));

        return hasFuel.stream().allMatch(bool -> bool);
    }

    @Override
    public @Nullable BlockState getNozzle()
    {
        BlockPos nozzlePos = this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite());
        BlockState nozzleState = this.level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock)
        {
            if(nozzleState.getValue(NozzleBlock.FACING).equals(this.getBlockState().getValue(FACING)))
                return nozzleState;
        }
        return super.getNozzle();
    }

    @Override
    public @Nullable BlockPos getNozzlePos()
    {
        BlockPos nozzlePos = this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite());
        BlockState nozzleState = this.level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock)
        {
            if(nozzleState.getValue(NozzleBlock.FACING).equals(this.getBlockState().getValue(FACING)))
                return nozzlePos;
        }
        return super.getNozzlePos();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LiquidRocketEngineBlockEntity rocketEngine)
    {
        if(level.isClientSide())
            return;
        if(rocketEngine.rocketFuel == null || rocketEngine.thrust == 0 || rocketEngine.efficiency == 0)
            return;
        BlockEntity blockEntity = level.getBlockEntity(pos.offset(state.getValue(FACING).getNormal()));
        if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, state.getValue(FACING).getOpposite()).isPresent())
        {
            blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, state.getValue(FACING).getOpposite()).ifPresent(handler -> {
                for (int tank = 0; tank < handler.getTanks(); tank++)
                {
                    FluidStack handlerStack = handler.getFluidInTank(tank);
                    if(rocketEngine.fuelTank.isFluidValid(handlerStack) && rocketEngine.fuelTank.getSpace(tank) > 0)
                    {
                        FluidStack drain = new FluidStack(handlerStack, 8);
                        handler.drain(drain, IFluidHandler.FluidAction.EXECUTE);
                        rocketEngine.fuelTank.fill(drain, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            });
        }

        BlockPos nozzlePos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(FACING)))
        {
            rocketEngine.setBuilt(true);

            if (rocketEngine.isRunning())
            {
                if (!rocketEngine.hasPropellantMixture())
                    rocketEngine.deactiveEngine(rocketEngine, nozzlePos, nozzleState);
                else
                {
                    if(rocketEngine.soundTick == 0 && rocketEngine.isRunning)
                    {
                        NetworkInit.sendToTracking(rocketEngine, new RocketEngineSoundPacket(rocketEngine.worldPosition, false));
                        rocketEngine.soundTick = 50;
                    }
                    rocketEngine.soundTick--;
                    rocketEngine.fuelTank.drain(Math.max(1, 8*(rocketEngine.throttle/15)), IFluidHandler.FluidAction.EXECUTE);
                    rocketEngine.setThrottle(15);
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

    public int getFuelStored()
    {
        int stored = 0;
        for (int tank = 0; tank < this.fuelTank.getTanks(); tank++)
            stored += this.fuelTank.getFluidInTank(tank).getAmount();


        return stored;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ForgeCapabilities.FLUID_HANDLER)
            return fluidHolder.cast();

        return super.getCapability(capability, facing);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        this.fuelTank.writeToNBT(tag);
        tag.put("chamber", this.handler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.fuelTank.readFromNBT(tag);
        this.handler.deserializeNBT(tag.getCompound("chamber"));
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

    public ItemStackHandler createHandler()
    {
        return new ItemStackHandler(3)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                super.onContentsChanged(slot);
                setChanged();
            }
        };
    }
}
