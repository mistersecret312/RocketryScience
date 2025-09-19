package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.entities.RocketEntity;

import java.util.function.BiFunction;

public class BlockData
{
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

        initializeData();

        this.stage = stage;
    }

    public void tick(Level level)
    {

    }

    public void render(RocketEntity rocket, BlockRenderDispatcher dispatcher, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, BlockPos.MutableBlockPos mutablePos)
    {
        BakedModel model = dispatcher.getBlockModel(getBlockState());
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(getBlockState(), RandomSource.create(42), ModelData.EMPTY))
        {
            dispatcher.renderBatched(getBlockState(), mutablePos.move(pos), rocket.level(), pose, buffer.getBuffer(rt), true, RandomSource.create(42), model.getModelData(rocket.level(), pos, getBlockState(), ModelData.EMPTY), null);
            mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());
        }
    }

    public void initializeData()
    {

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

        BlockEntity.loadStatic(pos, state, this.extraData);
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
