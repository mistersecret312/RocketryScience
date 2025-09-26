package net.mistersecret312.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.block_entities.SeparatorBlockEntity;
import net.mistersecret312.blocks.SeparatorBlock;
import net.mistersecret312.blocks.SeparatorBlock;
import net.mistersecret312.client.RocketRenderTypes;
import net.mistersecret312.init.BlockInit;
import net.mistersecret312.init.ConfigInit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SeparatorRenderer implements BlockEntityRenderer<SeparatorBlockEntity>
{
    public static final BlockState SINGLE = BlockInit.SEPARATOR.get().defaultBlockState();
    public static final BlockState SINGLE_EXTENDED = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.EXTENDED, true);

    public static final BlockState DOUBLE = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.DOUBLE);
    public static final BlockState DOUBLE_EXTENDED = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.DOUBLE).setValue(SeparatorBlock.EXTENDED, true);

    public static final BlockState TRIPLE_CENTER = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_CENTER);
    public static final BlockState TRIPLE_CORNER = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_CORNER);
    public static final BlockState TRIPLE_EDGE = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_EDGE);

    public static final BlockState TRIPLE_CENTER_EXTENDED = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_CENTER).setValue(SeparatorBlock.EXTENDED, true);
    public static final BlockState TRIPLE_CORNER_EXTENDED = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_CORNER).setValue(SeparatorBlock.EXTENDED, true);
    public static final BlockState TRIPLE_EDGE_EXTENDED = BlockInit.SEPARATOR.get().defaultBlockState().setValue(SeparatorBlock.SHAPE, SeparatorBlock.Shape.TRIPLE_EDGE).setValue(SeparatorBlock.EXTENDED, true);


    @Override
    public void render(SeparatorBlockEntity separator, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay)
    {

        if (separator.isController() && separator.getController() != null)
        {
            AABB aabb = null;
            if (separator.getControllerBE().getWidth() == 3)
                aabb = new AABB(separator.getController().offset(0, 2, 0), separator.getController().offset(2, 2, 2));
            if (separator.getControllerBE().getWidth() == 2)
                aabb = new AABB(separator.getController().offset(0, 2, 0), separator.getController().offset(1, 2, 1));

            boolean extend = false;
            if (aabb != null)
            {
                Stream<BlockState> states = separator.getLevel().getBlockStates(aabb);
                extend = states.anyMatch(state -> state.is(BlockInit.STEEL_COMBUSTION_CHAMBER.get()) || state.is(BlockInit.STEEL_ROCKET_ENGINE_STUB.get()));
            }
            else extend = separator.getLevel().getBlockState(separator.getController().offset(0, 2, 0)).is(BlockInit.STEEL_COMBUSTION_CHAMBER.get()) || separator.getLevel().getBlockState(separator.getController().offset(0, 2, 0)).is(BlockInit.STEEL_ROCKET_ENGINE_STUB.get());
            if(separator.getControllerBE().getWidth() == 3)
            {
                renderTripleWidth(separator.getLevel(), separator.getBlockPos(), pose, buffer, overlay, light, extend);
            }
            if (separator.getControllerBE().getWidth() == 2)
            {
                renderDoubleWidth(separator.getLevel(), separator.getBlockPos(), pose, buffer, overlay, light, extend);

            }
            if (separator.getControllerBE().getWidth() == 1)
            {
                renderSingularWidth(separator.getLevel(), separator.getBlockPos(), pose, buffer, overlay, light, extend);
            }
        }
    }

    public static void renderSingularWidth(Level level, BlockPos pos, PoseStack pose, MultiBufferSource buffer, int overlay, int light, boolean extend)
    {
        List<BlockState> states = new ArrayList<>();
        if(extend)
            states.add(SINGLE_EXTENDED);
        else states.add(SINGLE);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        pose.pushPose();
        for(int i = 0; i < states.size(); i++)
        {
            BlockState state = states.get(i);
            pose.translate(0f, i == 0 ? 0f : 1f, 0f);
            if(extend)
                pose.translate(0f, 0.75f, 0f);
            BakedModel model = blockRenderer.getBlockModel(state);
            light = LevelRenderer.getLightColor(level, pos.offset(0, i, 0));
            for (RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
        }
        pose.popPose();
    }

    public static void renderDoubleWidth(Level level, BlockPos pos, PoseStack pose, MultiBufferSource buffer, int overlay, int light, boolean extend)
    {
        List<BlockState> states = new ArrayList<>();
        if(extend)
            states.add(DOUBLE_EXTENDED);
        else states.add(DOUBLE);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();

        pose.pushPose();
        for (int corner = 0; corner < 4; corner++)
        {
            pose.rotateAround(Axis.YP.rotationDegrees(90), 0.5f, 0, 0.5f);
            pose.pushPose();
            if(corner == 0)
                pose.translate(0, 0, 1);
            if(corner == 1)
                pose.translate(0, 0, 0);
            if(corner == 2)
                pose.translate(1, 0, 0);
            if(corner == 3)
                pose.translate(1, 0, 1);
            for(int i = 0; i < states.size(); i++)
            {
                BlockState state = states.get(i);
                pose.translate(0f, i == 0 ? 0f : 1f, 0f);
                if(extend)
                    pose.translate(0f, 0.75f, 0f);
                BakedModel model = blockRenderer.getBlockModel(state);
                light = LevelRenderer.getLightColor(level, pos.offset(0, i, 0));
                for (RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                    modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
            }
            pose.popPose();
        }
        pose.popPose();
    }

    public static void renderTripleWidth(Level level, BlockPos pos, PoseStack pose, MultiBufferSource buffer, int overlay, int light, boolean extend)
    {
        List<Pair<BlockPos, Pair<Integer, BlockState>>> states = new ArrayList<>();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        for (int x = 0; x < 3; x++)
        {
            for (int z = 0; z < 3; z++)
            {
                int rotation = 0;
                if (z == 0)
                {
                    if (x == 2)
                    {
                        rotation = 90;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER)));
                    }
                    if (x == 1)
                    {
                        rotation = 180;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE)));
                    }
                    if (x == 0)
                    {
                        rotation = 180;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER)));
                    }
                }
                if (z == 1)
                {
                    if (x == 2)
                    {
                        rotation = 90;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE)));
                    }
                    if (x == 1)
                    {
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CENTER_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CENTER)));
                    }
                    if (x == 0)
                    {
                        rotation = 270;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE)));
                    }
                }
                if (z == 2)
                {
                    if (x == 2)
                    {
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER)));
                    }
                    if (x == 1)
                    {
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_EDGE)));
                    }
                    if (x == 0)
                    {
                        rotation = 270;
                        if(extend)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER_EXTENDED)));
                            continue;
                        }
                        states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_CORNER)));
                    }
                }
            }
        }
        for (int i = 0; i < states.size(); i++)
        {
            BlockState state = states.get(i).getSecond().getSecond();
            BlockPos position = states.get(i).getFirst();
            int x = position.getX();
            int y = position.getY();
            int z = position.getZ();
            pose.pushPose();
            pose.rotateAround(Axis.YP.rotationDegrees(states.get(i).getSecond().getFirst()), x+0.5f, 0, z+0.5f);
            pose.translate(x, y, z);
            if(extend && state != TRIPLE_CENTER_EXTENDED)
                pose.translate(0f, 0.75f, 0f);
            BakedModel model = blockRenderer.getBlockModel(state);
            light = LevelRenderer.getLightColor(level, pos.offset(x,y,z));
            for (RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(SeparatorBlockEntity separator)
    {
        return true;
    }

    @Override
    public boolean shouldRender(SeparatorBlockEntity separator, Vec3 cameraPos)
    {
        return true;
    }
}
