package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.mistersecret312.block_entities.MultiBlockEntity;
import net.mistersecret312.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MultiblockBlock extends BaseEntityBlock
{
    public MultiblockBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isPistonMove)
    {
        super.onPlace(state, level, pos, oldState, isPistonMove);
        calculateParts(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isPistonMove)
    {
        super.onRemove(state, level, pos, newState, isPistonMove);
        calculateParts(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        calculateParts(level, pos);
    }

    public void calculateParts(Level level, BlockPos pos)
    {
        List<MultiBlockEntity> parts = findAllParts(level, pos);
        MultiBlockEntity masterPart = null;
        if(parts.isEmpty())
            return;

        if(parts.stream().anyMatch(MultiBlockEntity::isMaster))
        {
            List<MultiBlockEntity> masters = new ArrayList<>(parts.stream().filter(MultiBlockEntity::isMaster).toList());
            masters.sort(Comparator.comparing(master -> pos.distManhattan(master.getBlockPos())));
            masterPart = masters.get(0);
        }
        if(masterPart == null)
        {
            masterPart = parts.get(0);
        }

        List<BlockPos> slaveVectors = new ArrayList<>();
        for(MultiBlockEntity part : parts)
        {
            slaveVectors.add(part.getBlockPos().subtract(masterPart.getBlockPos()));
            part.masterVector = masterPart.getBlockPos().subtract(part.getBlockPos());
        }

        masterPart.slaveVectors = slaveVectors;
        masterPart.updateMaster();
    }

    public static List<MultiBlockEntity> findAllParts(Level level, BlockPos startPos)
    {
        Block masterBlock = level.getBlockState(startPos).getBlock();
        MultiBlockEntity master = (MultiBlockEntity) level.getBlockEntity(startPos);

        if(master == null)
            return new ArrayList<>();

        List<MultiBlockEntity> parts = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);

        while(!queue.isEmpty())
        {
            BlockPos pos = queue.remove();
            if (level.getBlockEntity(pos) instanceof MultiBlockEntity blockEntity)
            {
                visited.add(pos);

                if (level.getBlockState(pos).getBlock() != masterBlock) continue;

                if (blockEntity == null) continue;

                if (!blockEntity.getType().equals(master.getType())) continue;

                parts.add(blockEntity);

                for (Direction direction : Direction.values())
                {
                    BlockPos visitPos = pos.relative(direction);
                    if (!visited.contains(visitPos)) queue.add(visitPos);
                }
            }
        }

        return parts;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }
}
