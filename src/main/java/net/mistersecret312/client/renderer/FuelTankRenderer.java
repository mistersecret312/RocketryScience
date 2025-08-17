package net.mistersecret312.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.blocks.FuelTankBlock;
import net.mistersecret312.init.BlockInit;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class FuelTankRenderer implements BlockEntityRenderer<FuelTankBlockEntity>
{
    public static final BlockState SINGLE = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState SINGLE_TOP = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState SINGLE_MIDDLE = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState SINGLE_BOTTOM = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState DOUBLE_TOP = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState DOUBLE_MIDDLE = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState DOUBLE_BOTTOM = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.DOUBLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);

    public static final BlockState TRIPLE_TOP = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE).setValue(FuelTankBlock.TOP, true).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_MIDDLE = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, false);
    public static final BlockState TRIPLE_BOTTOM = BlockInit.LOW_PRESSURE_FUEL_TANK.get().defaultBlockState().setValue(FuelTankBlock.SHAPE, FuelTankBlock.Shape.TRIPLE).setValue(FuelTankBlock.TOP, false).setValue(FuelTankBlock.BOTTOM, true);


    @Override
    public void render(FuelTankBlockEntity fuelTank, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay)
    {
        if (!fuelTank.isController()) return;
        if (fuelTank.getWidth() == 1)
        {
            renderSingularWidth(fuelTank, pose, buffer, overlay);
        }
        if (fuelTank.getWidth() == 2)
        {
            renderDoubleWidth(fuelTank, pose, buffer, overlay);
        }
    }

    public void renderSingularWidth(FuelTankBlockEntity fuelTank, PoseStack pose, MultiBufferSource buffer, int overlay)
    {
        List<BlockState> states = new ArrayList<>();
        if(fuelTank.getHeight() != 1)
        {
            states.add(SINGLE_BOTTOM);
            for (int i = 0; i < fuelTank.getHeight()-2; i++)
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
            int light = LevelRenderer.getLightColor(fuelTank.getLevel(), fuelTank.getBlockPos().offset(0, i, 0));
            for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
        }
        pose.popPose();
    }

    public void renderDoubleWidth(FuelTankBlockEntity fuelTank, PoseStack pose, MultiBufferSource buffer, int overlay)
    {
        List<BlockState> states = new ArrayList<>();
        if(fuelTank.getHeight() != 1)
        {
            states.add(DOUBLE_BOTTOM);
            for (int i = 0; i < fuelTank.getHeight()-2; i++)
            {
                states.add(DOUBLE_MIDDLE);
            }
            states.add(DOUBLE_TOP);
        }
        else
        {
            states.add(DOUBLE_MIDDLE);
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
                int light = LevelRenderer.getLightColor(fuelTank.getLevel(), fuelTank.getBlockPos().offset(0, i, 0));
                for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                    modelRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, overlay);
            }
            pose.popPose();
        }
        pose.popPose();
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
