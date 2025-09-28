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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.mistersecret312.blocks.FuelTankBlock;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.items.FuelTankBlockItem;
import net.mistersecret312.network.packets.FuelTankFrostPacket;
import net.mistersecret312.network.packets.FuelTankSizePacket;
import net.mistersecret312.util.ConnectivityHandler;
import net.mistersecret312.util.RocketFuel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FuelTankBlockEntity extends BlockEntity implements IConnectiveBlockEntity.Fluid
{
    private static final int MAX_SIZE = 3;

    protected LazyOptional<IFluidHandler> fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected RocketFuelTank tankInventory;
    protected RocketFuel propellants;
    public BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    protected int luminosity;
    protected int width;
    protected int height;

    public float ratio;
    public FuelTankBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.LIQUID_FUEL_TANK.get(), pPos, pBlockState);
        propellants = RocketFuel.HYDROLOX;
        tankInventory = createInventory();
        fluidCapability = LazyOptional.of(() -> tankInventory);
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        updateCapability = false;
        height = 1;
        width = 1;
        refreshCapability();
    }

    private RocketFuelTank createInventory()
    {
        FuelTankBlockEntity fuelTank = this;
        return new RocketFuelTank(propellants.getPropellants(), getCapacityMultiplier())
        {
            @Override
            protected void onContentsChanged()
            {
                List<Float> ratios = new ArrayList<>();
                for (int tank = 0; tank < this.getTanks(); tank++)
                {
                    float ratio = 1-(float) getTankInventory().getSpace(tank)/getTankInventory().getTankCapacity(tank);
                    ratios.add(ratio/this.getTanks());
                }
                float totalRatio = 0f;
                for(Float ratio : ratios)
                    totalRatio += ratio;

                ratio = totalRatio;
                if(level != null && !level.isClientSide())
                    NetworkInit.sendToTracking(fuelTank, new FuelTankFrostPacket(getBlockPos(), ratio));
            }
        };
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FuelTankBlockEntity fuelTank)
    {
        if (fuelTank.lastKnownPos == null)
            fuelTank.lastKnownPos = fuelTank.getBlockPos();
        else if (!fuelTank.lastKnownPos.equals(fuelTank.worldPosition) && fuelTank.worldPosition != null) {
            fuelTank.onPositionChanged();
            return;
        }

        if (fuelTank.updateCapability) {
            fuelTank.updateCapability = false;
            fuelTank.refreshCapability();
        }
        if (fuelTank.updateConnectivity)
            fuelTank.updateConnectivity();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
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

    public void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;

        List<Float> ratios = new ArrayList<>();
        for (int tank = 0; tank < this.getTanks(); tank++)
        {
            float ratio = 1-(float) this.getTankInventory().getSpace(tank)/this.getTankInventory().getTankCapacity(tank);
            ratios.add(ratio/this.getTanks());
        }
        float totalRatio = 0f;
        for(Float ratio : ratios)
            totalRatio += ratio;

        this.ratio = totalRatio;
        NetworkInit.sendToTracking(this, new FuelTankFrostPacket(this.getBlockPos(), ratio));

        FluidType attributes = newFluidStack.getFluid()
                .getFluidType();
        int luminosity = (int) (attributes.getLightLevel(newFluidStack) / 1.2f);
        boolean reversed = attributes.isLighterThanAir();
        int maxY = 1;

        for (int yOffset = 0; yOffset < height; yOffset++) {
            boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
            int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    FuelTankBlockEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
                    if (tankAt == null)
                        continue;
                    level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
                            .getBlock());
                    if (tankAt.luminosity == actualLuminosity)
                        continue;
                    tankAt.setLuminosity(actualLuminosity);
                }
            }
        }

        if (!level.isClientSide) {
            setChanged();
        }
    }

    protected void setLuminosity(int luminosity) {
        if (level.isClientSide)
            return;
        if (this.luminosity == luminosity)
            return;
        this.luminosity = luminosity;
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

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
        for (int tank = 0; tank < tankInventory.getTanks(); tank++)
        {
            int overflow = tankInventory.getFluidInTank(tank).getAmount()-tankInventory.getTankCapacity(tank);
            if(overflow > 0)
                tankInventory.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
        }
        forceFluidLevelUpdate = true;
    }

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        //if (!keepFluids)
        //   applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;
        for (int tank = 0; tank < tankInventory.getTanks(); tank++)
            onFluidStackChanged(tankInventory.getFluidInTank(tank));

        BlockState state = getBlockState();
        if (FuelTankBlock.isTank(state)) {
            state = state.setValue(FuelTankBlock.BOTTOM, true);
            state = state.setValue(FuelTankBlock.TOP, true);
            getLevel().setBlock(worldPosition, state, 22);
        }

        NetworkInit.sendToTracking(this, new FuelTankSizePacket(this.getBlockPos(), 1, this.serializeNBT()));
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
        NetworkInit.sendToTracking(this, new FuelTankSizePacket(this.getBlockPos(), 1, this.serializeNBT()));
        refreshCapability();
        setChanged();
    }

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = fluidCapability;
        fluidCapability = LazyOptional.of(this::handlerForCapability);
        oldCap.invalidate();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ?  tankInventory : getControllerBE() != null ?
                getControllerBE().handlerForCapability() : new FluidTank(0);
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Nullable
    public FuelTankBlockEntity getOtherFluidTankBlockEntity(Direction direction) {
        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
        if (otherBE instanceof FuelTankBlockEntity)
            return (FuelTankBlockEntity) otherBE;
        return null;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;
        int prevLum = luminosity;

        updateConnectivity = compound.contains("Uninitialized");
        luminosity = compound.getInt("Luminosity");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            width = compound.getInt("Size");
            height = compound.getInt("Height");
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            tankInventory.readFromNBT(compound.getCompound("TankContent"));
            for (int tank = 0; tank < tankInventory.getTanks(); tank++)
            {
                if(tankInventory.getSpace(tank) < 0)
                    tankInventory.drain(-tankInventory.getSpace(tank), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        updateCapability = true;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
        }

        if (luminosity != prevLum && hasLevel())
            level.getChunkSource()
                    .getLightEngine()
                    .checkBlock(worldPosition);
        this.propellants = RocketFuel.valueOf(compound.getString("fuel_type").toUpperCase());
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
            compound.putInt("Size", width);
            compound.putInt("Height", height);
        }
        compound.putInt("Luminosity", luminosity);
        compound.putString("fuel_type", propellants.getName());
        super.saveAdditional(compound);
        forceFluidLevelUpdate = false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!fluidCapability.isPresent())
            refreshCapability();
        if(!this.isController())
            return this.getControllerBE().getCapability(cap);
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }


    public RocketFuelTank getTankInventory() {
        return tankInventory;
    }

    public int getTotalTankSize() {
        return width * width * height;
    }

    public static int getMaxSize() {
        return MAX_SIZE;
    }

    public static int getCapacityMultiplier() {
        return 2000;
    }

    public static int getMaxHeight() {
        return 16;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (FuelTankBlock.isTank(state)) { // safety
            state = state.setValue(FuelTankBlock.BOTTOM, getController().getY() == getBlockPos().getY());
            state = state.setValue(FuelTankBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(getBlockPos(), state, 6);
        }
        for (int tank = 0; tank < tankInventory.getTanks(); tank++)
            onFluidStackChanged(tankInventory.getFluidInTank(tank));
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        notifyMultiUpdated();
        NetworkInit.sendToTracking(this, new FuelTankSizePacket(this.getBlockPos(), width, this.serializeNBT()));
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public IFluidTank getTank(int tank) {
        return tankInventory.getPropellants().get(tank);
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluidInTank(tank)
                .copy();
    }

    @Override
    public int getTanks()
    {
        return tankInventory.getTanks();
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        if(isController())
            return super.getRenderBoundingBox().expandTowards(width, height, width);
        else return super.getRenderBoundingBox();
    }
}
