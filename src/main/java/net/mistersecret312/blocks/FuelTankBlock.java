package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends MultiblockBlock
{

    public int capacityPerFluid;
    public FuelTankBlock(Properties pProperties, int capacityPerFluid)
    {
        super(pProperties);
        this.capacityPerFluid = capacityPerFluid;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        if(level.getBlockEntity(pos) instanceof FuelTankBlockEntity fuelTank)
        {
            if(player.getItemInHand(hand).getItem() instanceof BucketItem bucket)
            {
                FluidStack bucketStack = new FluidStack(bucket.getFluid(), 1000);
                RocketFuelTank tank = fuelTank.getFuelTank();
                if(tank != null && tank.isFluidValid(bucketStack))
                {
                    tank.fill(bucketStack, IFluidHandler.FluidAction.EXECUTE);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return BlockEntityInit.LIQUID_FUEL_TANK.get().create(pos, state);
    }
}
