package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.mistersecret312.blocks.FuelTankBlock;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.items.FuelTankBlockItem;
import net.mistersecret312.util.ConnectivityHandler;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class FuelTankBlockEntity extends BlockEntity implements IConnectiveBlockEntity.Fluid
{
    public RocketFuel fuel = RocketFuel.HYDROLOX;
    public RocketFuelTank propellantTank = createTank();
    public LazyOptional<IFluidHandler> cap = LazyOptional.empty();

    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    protected int width;
    protected int height;

    public FuelTankBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.LIQUID_FUEL_TANK.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FuelTankBlockEntity fuelTank)
    {
        if(fuelTank.lastKnownPos == null)
            fuelTank.lastKnownPos = fuelTank.getBlockPos();
        else if(!fuelTank.lastKnownPos.equals(fuelTank.worldPosition) && fuelTank.worldPosition != null)
        {
            fuelTank.onPositionChanged();
            return;
        }
        if(fuelTank.updateCapability)
        {
            fuelTank.updateCapability = false;
            fuelTank.refreshCapability();
        }
        if(fuelTank.updateConnectivity)
            fuelTank.updateConnectivity();
    }

    @Override
    public void onLoad()
    {
        this.cap = LazyOptional.of(() -> propellantTank);
        super.onLoad();
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        cap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        if (updateConnectivity)
            tag.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            tag.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            tag.put("TankContent", propellantTank.writeToNBT(new CompoundTag()));
            tag.putInt("Size", width);
            tag.putInt("Height", height);
        }

        tag.putString("fuel_type", this.fuel.getName());
        tag.putInt("capacity", this.propellantTank.getCapacity());

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;

        controller = null;
        lastKnownPos = null;

        updateConnectivity = tag.contains("Uninitialized");

        if (tag.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(tag.getCompound("LastKnownPos"));
        if (tag.contains("Controller"))
            controller = NbtUtils.readBlockPos(tag.getCompound("Controller"));

        if (isController()) {
            width = tag.getInt("Size");
            height = tag.getInt("Height");
            propellantTank.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            propellantTank.readFromNBT(tag.getCompound("TankContent"));
        }

        updateCapability = true;
        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                propellantTank.setCapacity(getCapacityMultiplier() * getTotalTankSize());
        }


        this.fuel = RocketFuel.valueOf(tag.getString("fuel_type").toUpperCase());
    }

    public int getTotalTankSize()
    {
        return width*height*width;
    }

    public int getCapacityMultiplier()
    {
        if(this.getBlockState().getBlock() instanceof FuelTankBlock tank)
            return tank.capacityPerFluid;
        else return 0;
    }

    public RocketFuelTank createTank()
    {
        int capacity = getControllerBE().getTotalTankSize()*getCapacityMultiplier();
        if(capacity == 0)
            capacity = getCapacityMultiplier();
        return new RocketFuelTank(this.fuel.getPropellants(), capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }

    public RocketFuelTank createTank(RocketFuelTank fuelTank, int capacity)
    {
        return new RocketFuelTank(fuelTank, capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        load(pkt.getTag());
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @SuppressWarnings("unchecked")
    @Override
    public FuelTankBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof FuelTankBlockEntity)
            return (FuelTankBlockEntity) blockEntity;
        return null;
    }

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        if (!keepFluids)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;

        BlockState state = getBlockState();
        if (state.getBlock() instanceof FuelTankBlock) {
            state = state.setValue(FuelTankBlock.BOTTOM, true);
            state = state.setValue(FuelTankBlock.TOP, true);
            getLevel().setBlock(worldPosition, state, 22);
        }

        refreshCapability();
        setChanged();
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide)
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
    }

    public void applyFluidTankSize(int blocks)
    {
        propellantTank = createTank(propellantTank, blocks*getCapacityMultiplier());
    }

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = cap;
        cap = LazyOptional.of(this::handlerForCapability);
        oldCap.invalidate();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ?  propellantTank : getControllerBE().handlerForCapability();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public void preventConnectivityUpdate()
    {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof FuelTankBlock) { // safety
            state = state.setValue(FuelTankBlock.BOTTOM, getController().getY() == getBlockPos().getY());
            state = state.setValue(FuelTankBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(getBlockPos(), state, 6);
        }
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis()
    {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();
        return getMaxWidth();
    }

    public int getMaxHeight()
    {
        return 16;
    }

    @Override
    public int getMaxWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public void setHeight(int height)
    {
        this.height = height;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public void setWidth(int width)
    {
        this.width = width;
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    public RocketFuelTank getPropellantTank()
    {
        return propellantTank;
    }

    @Override
    public IFluidTank getTank(int tank)
    {
        return propellantTank;
    }

    @Override
    public boolean hasTank()
    {
        return true;
    }

    @Override
    public int getTankSize(int tank)
    {
        return getCapacityMultiplier();
    }

    @Override
    public FluidStack getFluid(int tank)
    {
        return propellantTank.getFluidInTank(tank);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return this.cap.cast();
        return super.getCapability(cap, side);
    }
}
