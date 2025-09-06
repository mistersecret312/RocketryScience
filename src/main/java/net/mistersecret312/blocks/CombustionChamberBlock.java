package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import net.mistersecret312.block_entities.LiquidRocketEngineBlockEntity;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.menus.CombustionChamberMenu;
import org.jetbrains.annotations.Nullable;

public class CombustionChamberBlock extends BaseEntityBlock
{
    public static VoxelShape SHAPE_NORTH = Shapes.join(Block.box(0, 0, 0, 16, 16, 4), Block.box(2, 2, 4, 14, 14, 16), BooleanOp.OR);
    public static VoxelShape SHAPE_SOUTH = Shapes.join(Block.box(0, 0, 12, 16, 16, 16), Block.box(2, 2, 0, 14, 14, 12), BooleanOp.OR);
    public static VoxelShape SHAPE_EAST = Shapes.join(Block.box(12, 0, 0, 16, 16, 16), Block.box(0, 2, 2, 12, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_WEST = Shapes.join(Block.box(0, 0, 0, 4, 16, 16), Block.box(4, 2, 2, 16, 14, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_UP = Shapes.join(Block.box(0, 12, 0, 16, 16, 16), Block.box(2, 0, 2, 14, 12, 14), BooleanOp.OR);
    public static VoxelShape SHAPE_DOWN = Shapes.join(Block.box(0, 0, 0, 16, 4, 16), Block.box(2, 4, 2, 14, 16, 14), BooleanOp.OR);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CombustionChamberBlock(Properties pProperties)
    {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        if(level.getBlockEntity(pos) instanceof LiquidRocketEngineBlockEntity rocketEngine)
        {
            if(player.getItemInHand(hand).getItem() instanceof BucketItem bucket)
            {
                FluidStack bucketStack = new FluidStack(bucket.getFluid(), 1000);
                if(rocketEngine.fuelTank.isFluidValid(bucketStack))
                {
                    rocketEngine.fuelTank.fill(bucketStack, IFluidHandler.FluidAction.EXECUTE);
                    return InteractionResult.SUCCESS;
                }

            }
        }

        if(!level.isClientSide())
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if(blockEntity instanceof LiquidRocketEngineBlockEntity)
            {
                MenuProvider containerProvider = new MenuProvider()
                {
                    @Override
                    public Component getDisplayName()
                    {
                        return Component.translatable("screen.rocketry_science.combustion_chamber");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
                    {
                        return new CombustionChamberMenu(windowId, playerInventory, blockEntity);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, blockEntity.getBlockPos());
                return InteractionResult.SUCCESS;
            }
            else
            {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityInit.ROCKET_ENGINE.get(), LiquidRocketEngineBlockEntity::tick);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityInit.ROCKET_ENGINE.get().create(pos, state);
    }
}
