package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.capabilities.BlueprintDataCapability;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.CapabilityInit;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RocketEngineBlockEntity extends BlockEntity
{
    public int blueprintID = 0;

    protected FluidTank tank = new FluidTank(5*FluidType.BUCKET_VOLUME, this::testFluid);
    private final LazyOptional<IFluidHandler> holder = LazyOptional.empty();

    public RocketEngineBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.ROCKET_ENGINE.get(), pPos, pBlockState);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();

    }

    public static void tick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity rocketEngine)
    {

    }

    public boolean testFluid(FluidStack stack)
    {
        if(level.getCapability(CapabilityInit.BLUEPRINTS_DATA).isPresent())
        {
            Optional<BlueprintDataCapability> capabilityOptional = level.getCapability(CapabilityInit.BLUEPRINTS_DATA).resolve();
            if(capabilityOptional.isPresent())
            {
                RocketEngineBlueprint blueprint = capabilityOptional.get().rocketEngineBlueprints.get(this.blueprintID);

                RocketFuel rocketFuel = blueprint.rocketFuel;
                for(FluidType type : rocketFuel.getPropellants())
                    return stack.getFluid().getFluidType().equals(type);

            }
        }
        return false;
    }

    public int propellantQuantity()
    {
        if(level.getCapability(CapabilityInit.BLUEPRINTS_DATA).isPresent())
        {
            Optional<BlueprintDataCapability> capabilityOptional = level.getCapability(CapabilityInit.BLUEPRINTS_DATA).resolve();
            if(capabilityOptional.isPresent())
            {
                RocketEngineBlueprint blueprint = capabilityOptional.get().rocketEngineBlueprints.get(this.blueprintID);

                RocketFuel rocketFuel = blueprint.rocketFuel;
                return rocketFuel.getPropellants().size();
            }
        }
        return 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("blueprint_id", this.blueprintID);
        tank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.blueprintID = tag.getInt("blueprint_id");
        tank.readFromNBT(tag);
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
}
