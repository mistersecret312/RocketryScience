package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.block_entities.SeparatorBlockEntity;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.util.ConnectivityHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SeparatorBlock extends BaseEntityBlock
{
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");

    public SeparatorBlock(Properties pProperties)
    {
        super(pProperties);
        registerDefaultState(this.defaultBlockState().setValue(SHAPE, Shape.SINGLE).setValue(EXTENDED, false));
    }

    public static boolean isSeparator(BlockState state)
    {
        return state.getBlock() instanceof SeparatorBlock;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        Optional.ofNullable(world.getBlockEntity(pos)).ifPresent(blockEntity -> {
            if(blockEntity instanceof SeparatorBlockEntity separator)
                separator.updateConnectivity();
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof SeparatorBlockEntity tankBE))
                return;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(SHAPE, EXTENDED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return BlockEntityInit.SEPARATOR.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityInit.SEPARATOR.get(), SeparatorBlockEntity::tick);
    }


    public enum Shape implements StringRepresentable
    {
        SINGLE, DOUBLE, TRIPLE_EDGE, TRIPLE_CENTER, TRIPLE_CORNER;

        Shape() {}

        public String getSerializedName()
        {
            return this.name().toLowerCase();
        }
    }
}
