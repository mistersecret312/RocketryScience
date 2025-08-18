package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
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
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static net.mistersecret312.blocks.CombustionChamberBlock.FACING;

public class LiquidRocketEngineBlockEntity extends RocketEngineBlockEntity
{

    public RocketFuelTank fuelTank = createTank();
    public LazyOptional<IFluidHandler> holder = LazyOptional.empty();

    public LiquidRocketEngineBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.ROCKET_ENGINE.get(), pos, state);
    }

    @Override
    public void onLoad()
    {
        this.holder = LazyOptional.of(() -> fuelTank);
        super.onLoad();
    }

    @Override
    public void updateBlueprintData()
    {
        super.updateBlueprintData();
        RocketEngineBlueprint blueprint = this.getBlueprint();
        if(blueprint != null)
            if(!this.fuelTank.getFilter().equals(blueprint.rocketFuel.getPropellants()))
                this.fuelTank = new RocketFuelTank(blueprint.rocketFuel.getPropellants(), COMBUSTION_CHAMBER_CAPACITY);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();

        holder.invalidate();
    }

    @Override
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
                hasFuel.add(this.fuelTank.getPropellants().stream().allMatch(stack -> stack.getFluidAmount() > 0));
                for(int i = 0; i < this.fuelTank.getTanks(); i++)
                    hasFuel.add(blueprint.rocketFuel.getPropellants().get(i).test(this.fuelTank.getFluidInTank(i)));

                boolean hasMixture = hasFuel.stream().allMatch(bool -> bool);
                return hasMixture;
            }
        }
        return super.hasPropellantMixture();
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
        if(rocketEngine.getBlueprint() == null)
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
        if(level.getBlockEntity(pos.offset(state.getValue(FACING).getNormal())) instanceof FuelTankBlockEntity fuelTank)
        {

        }

        rocketEngine.getBlueprint().calculateReliability();
        double reliabilityEffects = 0;
        for(Mishap<RocketEngineBlockEntity,?> mishap : rocketEngine.mishaps)
            reliabilityEffects += mishap.getType().physicalEffect;
        rocketEngine.setReliability(trimDouble(rocketEngine.getBlueprint().reliability*Math.max(0.2d, rocketEngine.integrity/rocketEngine.maxIntegrity))+reliabilityEffects);

        BlockPos nozzlePos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(FACING)))
        {
            if(rocketEngine.integrity == 0)
            {
                level.explode(null, pos.getX(), pos.getY(), pos.getZ(),4, false, Level.ExplosionInteraction.NONE);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                level.destroyBlock(nozzlePos, true);
            }

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

                    if(rocketEngine.soundTick == 0 && rocketEngine.isRunning)
                    {
                        NetworkInit.sendToTracking(rocketEngine, new RocketEngineSoundPacket(rocketEngine.worldPosition, false));
                        rocketEngine.soundTick = 50;
                    }
                    rocketEngine.soundTick--;
                    rocketEngine.fuelTank.drain(Math.max(1, 8*(rocketEngine.throttle/15)), IFluidHandler.FluidAction.EXECUTE);
                    rocketEngine.setIntegrity(trimDouble(rocketEngine.integrity - Math.max(0.01, 0.1 * ((double) rocketEngine.throttle / 15))));
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
            return holder.cast();

        return super.getCapability(capability, facing);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        this.fuelTank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.fuelTank.readFromNBT(tag);
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
