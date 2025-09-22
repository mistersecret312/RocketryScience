package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.block_entities.IRocketPadConnective;
import net.mistersecret312.block_entities.RocketConstructorBlockEntity;
import net.mistersecret312.block_entities.RocketPadBlockEntity;
import net.mistersecret312.data.RocketPads;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.util.infrastructure.RocketPad;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RocketConstructorBlock extends BaseEntityBlock
{

    public RocketConstructorBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        if(hand == InteractionHand.MAIN_HAND && !level.isClientSide()
        && level.getBlockEntity(pos) instanceof RocketConstructorBlockEntity constructor)
        {
            if(constructor.getPadUUID() == null || level.getServer() == null)
                return InteractionResult.FAIL;

            RocketPad rocketPad = RocketPads.get(level.getServer()).rocketPads.get(constructor.getPadUUID());
            BlockPos padPos = rocketPad.getPos();
            Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
            if(padLevel == null)
                return InteractionResult.FAIL;

            RocketPadBlockEntity pad = (RocketPadBlockEntity) padLevel.getBlockEntity(padPos);
            if(pad != null)
                constructor.assembleRocket(pad, player);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return BlockEntityInit.CONSTRUCTOR.get().create(pPos, pState);
    }
}
