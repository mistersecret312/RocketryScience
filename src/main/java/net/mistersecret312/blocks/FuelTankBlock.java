package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.util.ConnectivityHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FuelTankBlock extends BaseEntityBlock
{
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public int capacityPerFluid;
    public FuelTankBlock(Properties pProperties, int capacityPerFluid)
    {
        super(pProperties);
        this.capacityPerFluid = capacityPerFluid;
        registerDefaultState(this.defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        Optional.ofNullable(world.getBlockEntity(pos)).ifPresent(blockEntity -> {
            if(blockEntity instanceof FuelTankBlockEntity fuelTank)
                fuelTank.updateConnectivity();
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        FuelTankBlockEntity fuelTank = ConnectivityHandler.partAt(BlockEntityInit.LIQUID_FUEL_TANK.get(), level, pos).getControllerBE();

        if (player.getItemInHand(hand).getItem() instanceof BucketItem bucket)
        {
            FluidStack bucketStack = new FluidStack(bucket.getFluid(), 1000);
            IFluidTank tank = fuelTank.getTank(0);
            if (tank != null && tank.isFluidValid(bucketStack))
            {
                tank.fill(bucketStack, IFluidHandler.FluidAction.EXECUTE);
                fuelTank.setChanged();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof FuelTankBlockEntity tankBE))
                return;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TOP, BOTTOM);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return BlockEntityInit.LIQUID_FUEL_TANK.get().create(pos, state);
    }
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityInit.LIQUID_FUEL_TANK.get(), FuelTankBlockEntity::tick);
    }
}
