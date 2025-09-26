package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.RocketBlockDataInit;
import net.povstalec.sgjourney.client.render.block_entity.MilkyWayStargateRenderer;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiFunction;

public class BlockData
{
    public static final BlockData VOID = new BlockData(null, -1, null, null);

    public BlockPos pos;
    public int state;
    public CompoundTag extraData;

    private Stage stage;

    public BlockData() {}

    public BlockData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        this.state = state;
        this.pos = pos;
        this.extraData = tag;

        this.stage = stage;

        this.initializeData(stage);
    }

    public void tick(Level level)
    {

    }

    public BlockDataType<?> getType()
    {
        return RocketBlockDataInit.BASE.get();
    }

    public void render(RocketEntity rocket, BlockRenderDispatcher dispatcher, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, BlockPos.MutableBlockPos mutablePos)
    {
        BlockEntityRenderDispatcher blockDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BakedModel model = dispatcher.getBlockModel(getBlockState());
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(getBlockState(), RandomSource.create(42), ModelData.EMPTY))
        {
            if((getBlockState().getRenderShape() == RenderShape.MODEL && getBlockState().hasBlockEntity()) || getBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED)
            {
                if ((!extraData.isEmpty() && extraData != null))
                {
                    BlockEntity blockEntity = BlockEntity.loadStatic(mutablePos.move(pos), getBlockState(), extraData);
                    mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());
                    if (blockEntity != null)
                    {
                        blockEntity.setLevel(rocket.level());
                        if(blockEntity.getBlockState().getBlock() instanceof BaseEntityBlock baseEntity)
                        {
                            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) baseEntity.getTicker(rocket.level(), getBlockState(), blockEntity.getType());
                            if(ticker != null)
                            {
                                ticker.tick(rocket.level(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
                            }
                        }
                        pose.pushPose();
                        blockDispatcher.render(blockEntity, partial, pose, buffer);
                        pose.popPose();
                    }
                }
            }
            if(getBlockState().getRenderShape() == RenderShape.MODEL)
            {
                dispatcher.renderBatched(getBlockState(), mutablePos.move(pos), rocket.level(), pose, buffer.getBuffer(rt), true, RandomSource.create(42), model.getModelData(rocket.level(), pos, getBlockState(), ModelData.EMPTY), null);
                mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());
            }
        }
    }

    public void initializeData(Stage stage)
    {

    }
    
    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        minX = (Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5));
        minY = (Math.min(aabb.minY, rocket.position().y+pos.getY()));
        minZ = (Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5));
        maxX = (Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5));
        maxY = (Math.max(aabb.maxY, rocket.position().y+pos.getY()+1));
        maxZ = (Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5));

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getRocket().getRocketEntity().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag extraData = new CompoundTag();
            if(blockEntity != null)
                extraData = blockEntity.saveWithId();

            if(!stage.palette.contains(state))
                stage.palette.add(state);

            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);

            return new BlockData(stage, stage.palette.indexOf(state), pos, extraData);
        };
    }

    public boolean doesTick(Level level)
    {
        return false;
    }

    public void placeInLevel(Level level, BlockPos pos)
    {
        BlockState state = this.stage.palette.get(this.state);

        level.setBlock(pos, state, Block.UPDATE_ALL);
        if(extraData == null || extraData.isEmpty())
            return;

        BlockEntity entity = BlockEntity.loadStatic(pos, state, this.extraData);
        if(entity != null)
        {
            level.setBlockEntity(entity);
            entity.setChanged();
        }
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.state);
        buffer.writeNbt(this.extraData);
    }

    public void fromNetwork(FriendlyByteBuf buffer, BlockPos pos, Stage stage)
    {
        this.stage = stage;
        this.pos = pos;
        this.state = buffer.readInt();
        this.extraData = buffer.readNbt();
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", RocketBlockDataInit.ROCKET_DATA.get().getKey(getType()).toString());
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        tag.putInt("state", this.state);
        tag.put("extra_data", this.extraData);

        return tag;
    }

    public void load(CompoundTag tag, Stage stage)
    {
        this.pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        this.state = tag.getInt("state");
        this.extraData = tag.getCompound("extra_data");

        this.stage = stage;
    }

    public BlockState getBlockState()
    {
        return this.stage.palette.get(this.state);
    }

    public Stage getStage()
    {
        return stage;
    }
}
