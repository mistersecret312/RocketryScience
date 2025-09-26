package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.client.model.PlumeModel;
import net.mistersecret312.client.renderer.PlumeRenderer;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.RocketBlockDataInit;

import java.util.*;
import java.util.function.BiFunction;

public class RocketEngineData extends BlockData
{
    public BlockState nozzleState;
    public boolean enabled;
    public ArrayList<Map.Entry<BlockPos, BlockData>> tanks;

    public int frame = 0;
    public int animTick = 0;

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
        if(!enabled)
            return;

        RocketEntity rocket = this.getStage().getRocket().getRocketEntity();
        rocket.addDeltaMovement(new Vec3(0, 0.056, 0));

        animTick++;
        if(animTick > 10)
        {
            frame++;
            animTick = 0;
        }
        if(frame > 1)
        {
            frame = 0;
        }
    }

    public void toggle()
    {
        this.enabled = !enabled;
    }

    @Override
    public BlockDataType<?> getType()
    {
        return RocketBlockDataInit.ROCKET_ENGINE.get();
    }

    @Override
    public void initializeData(Stage stage)
    {
        ArrayList<Map.Entry<BlockPos, BlockData>> datas = new ArrayList<>(stage.blocks.entrySet());
        datas.removeIf(entry -> !(entry.getValue() instanceof FuelTankData));
        datas.sort(Comparator.comparing(entry -> entry.getKey().distSqr(pos)));

        this.tanks = datas;
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

                    level.removeBlockEntity(pos);
                    level.removeBlock(pos, false);
                    level.removeBlock(pos.relative(state.getValue(CombustionChamberBlock.FACING).getOpposite()), false);
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
        minY = (Math.min(aabb.minY, rocket.position().y+pos.getY()-1));
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
        PlumeRenderer plume = new PlumeRenderer(RocketryScienceMod.ClientModEvents.plumeModel);

        pose.pushPose();

        if(enabled)
            plume.renderPlume(frame, 15, getBlockState(), pose, buffer, OverlayTexture.NO_OVERLAY);

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
    public CompoundTag save()
    {
        CompoundTag tag = super.save();
        tag.put("nozzle", NbtUtils.writeBlockState(nozzleState));
        return tag;
    }

    @Override
    public void load(CompoundTag tag, Stage stage)
    {
        super.load(tag, stage);
        this.nozzleState = NbtUtils.readBlockState(stage.getRocket().getRocketEntity().level().holderLookup(Registries.BLOCK),
                                                   tag.getCompound("nozzle"));
    }

    @Override
    public boolean doesTick(Level level)
    {
        return true;
    }

}
