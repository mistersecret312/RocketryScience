package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IConnectiveBlockEntity
{
    BlockPos getController();
    <T extends BlockEntity & IConnectiveBlockEntity> T getControllerBE();
    boolean isController();
    void setController(BlockPos pos);
    void removeController(boolean keepContents);

    void preventConnectivityUpdate();
    void notifyMultiUpdated();

    Direction.Axis getMainConnectionAxis();

    default Direction.Axis getMainAxisOf(BlockEntity be) { // this feels redundant, but it gives us a default to use when defining ::getMainConnectionAxis
        BlockState state = be.getBlockState();

        Direction.Axis axis;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        }
        else if (state.hasProperty(BlockStateProperties.FACING)) {
            axis = state.getValue(BlockStateProperties.FACING).getAxis();
        }
        else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            axis = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
        }
        else axis = Direction.Axis.Y;

        return axis;
    }

    int getMaxLength(Direction.Axis longAxis, int width);
    int getMaxWidth();

    int getHeight();
    void setHeight(int height);
    int getWidth();
    void setWidth(int width);

    public interface Fluid extends IConnectiveBlockEntity {
        // done here rather than through the Capability to allow greater flexibility
        default boolean hasTank() { return false; }

        default int getTankSize(int tank) {	return 0; }

        default void setTankSize(int tank, int blocks) {}

        default IFluidTank getTank(int tank) { return null; }

        default FluidStack getFluid(int tank) {	return FluidStack.EMPTY; }
    }
}
