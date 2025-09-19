package net.mistersecret312.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.blocks.FuelTankBlock;
import net.mistersecret312.client.RocketRenderTypes;
import net.mistersecret312.init.BlockInit;
import net.mistersecret312.init.ConfigInit;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class FuelTankRenderer implements BlockEntityRenderer<FuelTankBlockEntity>
{
    public static final BlockState SINGLE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState SINGLE_TOP = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState SINGLE_MIDDLE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState SINGLE_BOTTOM = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState DOUBLE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState DOUBLE_TOP = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState DOUBLE_MIDDLE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState DOUBLE_BOTTOM = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState TRIPLE_SINGLE_CENTER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CENTER).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);
    public static final BlockState TRIPLE_SINGLE_CORNER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CORNER).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);
    public static final BlockState TRIPLE_SINGLE_EDGE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_EDGE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState TRIPLE_LOWER_CENTER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CENTER).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);
    public static final BlockState TRIPLE_LOWER_CORNER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CORNER).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);
    public static final BlockState TRIPLE_LOWER_EDGE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_EDGE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState TRIPLE_MIDDLE_CENTER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CENTER).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_MIDDLE_CORNER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CORNER).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_MIDDLE_EDGE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_EDGE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);

    public static final BlockState TRIPLE_UPPER_CENTER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CENTER).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_UPPER_CORNER = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_CORNER).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_UPPER_EDGE = BlockInit.FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE_EDGE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);


    @Override
    public void render(FuelTankBlockEntity fuelTank, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay)
    {
        if (fuelTank.isController())
        {
            if(fuelTank.getControllerBE().getWidth() == 3)
            {
                renderTripleWidth(fuelTank.getHeight(), fuelTank.getLevel(), fuelTank.getBlockPos(), fuelTank.ratio, pose, buffer, overlay, light);
            }
            if (fuelTank.getControllerBE().getWidth() == 2)
            {
                renderDoubleWidth(fuelTank.getHeight(), fuelTank.getLevel(), fuelTank.getBlockPos(), fuelTank.ratio, pose, buffer, overlay, light);

            }
            if (fuelTank.getControllerBE().getWidth() == 1)
            {
                renderSingularWidth(fuelTank.getHeight(), fuelTank.getLevel(), fuelTank.getBlockPos(), fuelTank.ratio, pose, buffer, overlay, light);
            }
        }
    }

    public static void renderSingularWidth(int height, Level level, BlockPos pos, float ratio, PoseStack pose, MultiBufferSource buffer, int overlay, int light)
    {
        List<BlockState> states = new ArrayList<>();
        if(height != 1)
        {
            states.add(SINGLE_BOTTOM);
            for (int i = 0; i < height-2; i++)
            {
                states.add(SINGLE_MIDDLE);
            }
            states.add(SINGLE_TOP);
        }
        else
        {
            states.add(SINGLE);
        }
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        pose.pushPose();
        for(int i = 0; i < states.size(); i++)
        {
            BlockState state = states.get(i);
            pose.translate(0f, i == 0 ? 0f : 1f, 0f);
            BakedModel model = blockRenderer.getBlockModel(state);
            light = LevelRenderer.getLightColor(level, pos.offset(0, i, 0));
            for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
        }
        pose.popPose();
        if(ConfigInit.enable_frost_layer.get())
        {
            for (int i = 0; i <= 3; i++)
            {
                float fluidLevel = (height-0.1F) * ratio;
                if (fluidLevel != 0.0F)
                {
                    pose.pushPose();
                    if (i == 0)
                    {
                        pose.translate(0.9f, 0f, 1f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);
                    }
                    if (i == 1)
                    {
                        pose.translate(0f, 0f, 1f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);

                    }
                    if (i == 2)
                    {
                        pose.translate(0f, 0f, 0f);

                    }
                    if (i == 3)
                    {
                        pose.translate(0f, 0f, 0.9f);

                    }
                    pose.translate(0f, fluidLevel, 0f);
                    int r = 255, g = 255, b = 255, a = 120;
                    VertexConsumer consumer = buffer.getBuffer(RocketRenderTypes.frost());
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    consumer.vertex(pose.last().pose(), 0, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 0, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 0.9f, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 0.9f, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    pose.popPose();
                }
            }
        }
    }

    public static void renderDoubleWidth(int height, Level level, BlockPos pos, float ratio, PoseStack pose, MultiBufferSource buffer, int overlay, int light)
    {
        List<BlockState> states = new ArrayList<>();
        if(height != 1)
        {
            states.add(DOUBLE_BOTTOM);
            for (int i = 0; i < height-2; i++)
            {
                states.add(DOUBLE_MIDDLE);
            }
            states.add(DOUBLE_TOP);
        }
        else
        {
            states.add(DOUBLE);
        }
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
                BakedModel model = blockRenderer.getBlockModel(state);
                light = LevelRenderer.getLightColor(level, pos.offset(0, i, 0));
                for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                    modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
            }
            pose.popPose();
        }
        pose.popPose();

        if(ConfigInit.enable_frost_layer.get())
        {
            for (int i = 0; i <= 3; i++)
            {
                float fluidLevel = (height-0.1F) * ratio;
                if (fluidLevel != 0.0F)
                {
                    pose.pushPose();
                    if (i == 0)
                    {
                        pose.translate(1.9f, 0f, 2f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);
                    }
                    if (i == 1)
                    {
                        pose.translate(0f, 0f, 2f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);

                    }
                    if (i == 2)
                    {
                        pose.translate(0f, 0f, 0f);

                    }
                    if (i == 3)
                    {
                        pose.translate(0f, 0f, 1.9f);

                    }
                    pose.translate(0f, fluidLevel, 0f);
                    int r = 255, g = 255, b = 255, a = 120;
                    VertexConsumer consumer = buffer.getBuffer(RocketRenderTypes.frost());
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    consumer.vertex(pose.last().pose(), 0, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 0, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 1.9f, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 1.9f, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    pose.popPose();
                }
            }
        }
    }

    public static void renderTripleWidth(int height, Level level, BlockPos pos, float ratio, PoseStack pose, MultiBufferSource buffer, int overlay, int light)
    {
        List<Pair<BlockPos, Pair<Integer, BlockState>>> states = new ArrayList<>();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        if (height != 1)
        {
            for (int y = 0; y < height; y++)
            {
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
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_CORNER)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_CORNER)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_EDGE)));
                            }
                            if (x == 1)
                            {
                                rotation = 180;
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_EDGE)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_EDGE)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_CENTER)));
                            }
                            if (x == 0)
                            {
                                rotation = 180;
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_CORNER)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_CORNER)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_EDGE)));
                            }
                        }
                        if (z == 1)
                        {
                            if (x == 2)
                            {
                                rotation = 90;
                                if(y == height-1)
                                {
                                        states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_EDGE)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_EDGE)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_CENTER)));
                            }
                            if (x == 1)
                            {
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_CENTER)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_CENTER)));
                                    continue;
                                }
                            }
                            if (x == 0)
                            {
                                rotation = 270;
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_EDGE)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_EDGE)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_CENTER)));
                            }
                        }
                        if (z == 2)
                        {
                            if (x == 2)
                            {
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_CORNER)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_CORNER)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_EDGE)));
                            }
                            if (x == 1)
                            {
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_EDGE)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_EDGE)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_CENTER)));
                            }
                            if (x == 0)
                            {
                                rotation = 270;
                                if(y == height-1)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_UPPER_CORNER)));
                                    continue;
                                }
                                if(y == 0)
                                {
                                    states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_LOWER_CORNER)));
                                    continue;
                                }
                                states.add(Pair.of(new BlockPos(x, y, z), Pair.of(rotation, TRIPLE_MIDDLE_EDGE)));
                            }
                        }
                    }
                }
            }
        }
        else
        {
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
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_CORNER)));
                        }
                        if (x == 1)
                        {
                            rotation = 180;
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_EDGE)));
                        }
                        if (x == 0)
                        {
                            rotation = 180;
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_CORNER)));
                        }
                    }
                    if (z == 1)
                    {
                        if (x == 2)
                        {
                            rotation = 90;
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_EDGE)));
                        }
                        if (x == 1)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_CENTER)));
                        }
                        if (x == 0)
                        {
                            rotation = 270;
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_EDGE)));
                        }
                    }
                    if (z == 2)
                    {
                        if (x == 2)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_CORNER)));
                        }
                        if (x == 1)
                        {
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_EDGE)));
                        }
                        if (x == 0)
                        {
                            rotation = 270;
                            states.add(Pair.of(new BlockPos(x, 0, z), Pair.of(rotation, TRIPLE_SINGLE_CORNER)));
                        }
                    }
                }
            }
        }

        for (int i = 0; i < states.size(); i++)
        {
            BlockState state = states.get(i).getSecond().getSecond();
            BlockPos statePos = states.get(i).getFirst();
            int x = statePos.getX();
            int y = statePos.getY();
            int z = statePos.getZ();
            pose.pushPose();
            pose.rotateAround(Axis.YP.rotationDegrees(states.get(i).getSecond().getFirst()), x+0.5f, 0, z+0.5f);
            pose.translate(x, y, z);
            BakedModel model = blockRenderer.getBlockModel(state);
            light = LevelRenderer.getLightColor(level, pos.offset(x,y,z));
            for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
            pose.popPose();
        }

        if(ConfigInit.enable_frost_layer.get())
        {
            for (int i = 0; i <= 3; i++)
            {
                float fluidLevel = (height-0.1F) * ratio;
                if (fluidLevel != 0.0F)
                {
                    pose.pushPose();
                    if (i == 0)
                    {
                        pose.translate(2.9f, 0f, 3f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);
                    }
                    if (i == 1)
                    {
                        pose.translate(0f, 0f, 3f);
                        pose.rotateAround(Axis.YP.rotationDegrees(90), 0f, 0f, 0f);

                    }
                    if (i == 2)
                    {
                        pose.translate(0f, 0f, 0f);

                    }
                    if (i == 3)
                    {
                        pose.translate(0f, 0f, 2.9f);

                    }
                    pose.translate(0f, fluidLevel, 0f);
                    int r = 255, g = 255, b = 255, a = 120;
                    VertexConsumer consumer = buffer.getBuffer(RocketRenderTypes.frost());
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    consumer.vertex(pose.last().pose(), 0, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 0, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 2.9f, 0.05f, fluidLevel - 0.1f).color(r, g, b, a).uv2(light).endVertex();
                    consumer.vertex(pose.last().pose(), 2.9f, 0.05f, 0).color(r, g, b, a).uv2(light).endVertex();
                    pose.popPose();
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(FuelTankBlockEntity fuelTank)
    {
        return true;
    }

    @Override
    public boolean shouldRender(FuelTankBlockEntity fuelTank, Vec3 cameraPos)
    {
        return true;
    }
}
