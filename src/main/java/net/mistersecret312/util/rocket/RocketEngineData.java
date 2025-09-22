package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.entities.RocketEntity;

import java.util.function.BiFunction;

public class RocketEngineData extends BlockData
{
    public BlockState nozzleState;

    public RocketEngineData(Stage stage, int state, BlockState nozzleState, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
        this.nozzleState = nozzleState;
    }

    public RocketEngineData()
    {

    }

    @Override
    public void tick(Level level)
    {
        super.tick(level);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getRocket().getRocketEntity().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag extraData;
            if(blockEntity instanceof RocketEngineBlockEntity rocketEngine)
            {
                BlockState nozzleState = level.getBlockState(pos.relative(state.getValue(CombustionChamberBlock.FACING).getOpposite()));
                if (rocketEngine.isBuilt && nozzleState.getBlock() instanceof NozzleBlock)
                {
                    extraData = blockEntity.saveWithId();
                    if(!stage.palette.contains(state))
                        stage.palette.add(state);
                    return new RocketEngineData(stage, stage.palette.indexOf(state), nozzleState, pos, extraData);
                }
            }
            if(state.getBlock() instanceof NozzleBlock)
            {
                RocketEngineBlockEntity rocketEngineBlockEntity = (RocketEngineBlockEntity) level.getBlockEntity(pos.relative(state.getValue(NozzleBlock.FACING)));
                if(rocketEngineBlockEntity != null && rocketEngineBlockEntity.isBuilt)
                    return BlockData.VOID;

            }
            return null;
        };
    }

    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        minX = (Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5));
        minY = (Math.min(aabb.minY, rocket.position().y+pos.getY()-2));
        minZ = (Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5));
        maxX = (Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5));
        maxY = (Math.max(aabb.maxY, rocket.position().y+pos.getY()+1));
        maxZ = (Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5));

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void placeInLevel(Level level, BlockPos pos)
    {
        super.placeInLevel(level, pos);

        level.setBlock(pos.offset(this.getBlockState().getValue(CombustionChamberBlock.FACING).getOpposite().getNormal()), nozzleState, 2);
    }

    @Override
    public void render(RocketEntity rocket, BlockRenderDispatcher dispatcher, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, BlockPos.MutableBlockPos mutablePos)
    {
        super.render(rocket, dispatcher, yaw, partial, pose, buffer, mutablePos);

        pose.pushPose();

        pose.translate(0, -1, 0);

        BakedModel model = dispatcher.getBlockModel(getBlockState());
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(getBlockState(), RandomSource.create(42), ModelData.EMPTY))
        {
            dispatcher.renderBatched(nozzleState, mutablePos.move(pos), rocket.level(), pose, buffer.getBuffer(rt), true, RandomSource.create(42), model.getModelData(rocket.level(), pos, nozzleState, ModelData.EMPTY), null);
        }
        mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());

        pose.popPose();
    }

    @Override
    public boolean doesTick(Level level)
    {
        return true;
    }

}
