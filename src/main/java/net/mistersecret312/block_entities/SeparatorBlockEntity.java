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
import net.mistersecret312.blocks.SeparatorBlock;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.network.packets.FuelTankSizePacket;
import net.mistersecret312.util.ConnectivityHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SeparatorBlockEntity extends BlockEntity implements IConnectiveBlockEntity
{
    private static final int MAX_SIZE = 3;

    public BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int width;
    protected int height;

    public SeparatorBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.SEPARATOR.get(), pPos, pBlockState);
        updateConnectivity = false;
        height = 1;
        width = 1;
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SeparatorBlockEntity separator)
    {
        if (separator.lastKnownPos == null)
            separator.lastKnownPos = separator.getBlockPos();
        else if (!separator.lastKnownPos.equals(separator.worldPosition) && separator.worldPosition != null) {
            separator.onPositionChanged();
            return;
        }
        if (separator.updateConnectivity)
            separator.updateConnectivity();
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

    @SuppressWarnings("unchecked")
    @Override
    public SeparatorBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof SeparatorBlockEntity)
            return (SeparatorBlockEntity) blockEntity;
        return null;
    }

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        controller = null;
        width = 1;
        height = 1;

        BlockState state = getBlockState();
        if (SeparatorBlock.isSeparator(state))
        {
            getLevel().setBlock(worldPosition, state, 22);
        }

        NetworkInit.sendToTracking(this, new FuelTankSizePacket(this.getBlockPos(), 1, this.serializeNBT()));
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
        setChanged();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Nullable
    public SeparatorBlockEntity getOtherFluidTankBlockEntity(Direction direction) {
        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
        if (otherBE instanceof SeparatorBlockEntity)
            return (SeparatorBlockEntity) otherBE;
        return null;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            width = compound.getInt("Size");
            height = compound.getInt("Height");
        }


        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
        }

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
            compound.putInt("Size", width);
            compound.putInt("Height", height);
        }
        super.saveAdditional(compound);
    }

    public static int getMaxHeight() {
        return 1;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (SeparatorBlock.isSeparator(state)) {
            level.setBlock(getBlockPos(), state, 6);
        }
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
    public AABB getRenderBoundingBox()
    {
        if(isController())
            return super.getRenderBoundingBox().expandTowards(width, height, width);
        else return super.getRenderBoundingBox();
    }
}
