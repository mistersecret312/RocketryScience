package net.mistersecret312.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.Tags;
import net.mistersecret312.client.renderer.FuelTankRenderer;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.BlockInit;
import net.mistersecret312.util.rocket.BlockData;
import net.mistersecret312.util.rocket.Stage;
import org.joml.Quaternionf;

import java.util.Map;

public class RocketRenderer extends EntityRenderer<RocketEntity>
{
    BlockRenderDispatcher dispatcher;
    ModelBlockRenderer blockRenderer;
    public RocketRenderer(EntityRendererProvider.Context pContext)
    {
        super(pContext);
        this.dispatcher = pContext.getBlockRenderDispatcher();
        this.blockRenderer = pContext.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(RocketEntity rocket, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, int light)
    {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(rocket.getViewXRot(partial)));
        pose.translate(-0.5f, 0f, -0.5f);
        BlockPos.MutableBlockPos mutablePos = rocket.getOnPos().mutable().move(0, 1, 0);
        for(Stage stage : rocket.getRocket().stages)
        {
            for(Map.Entry<BlockPos, BlockData> data : stage.blocks.entrySet())
            {
                BlockPos pos = data.getKey();
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                data.getValue().render(rocket, dispatcher, yaw, partial, pose, buffer, mutablePos);
            }
        }
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity)
    {
        return new ResourceLocation("block/air");
    }
}
