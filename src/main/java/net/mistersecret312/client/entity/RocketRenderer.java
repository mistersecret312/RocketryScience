package net.mistersecret312.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.Tags;
import net.mistersecret312.client.renderer.FuelTankRenderer;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.BlockInit;
import org.joml.Quaternionf;

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
        pose.mulPose(new Quaternionf().rotationX(rocket.getViewXRot(partial)).rotationY(rocket.getViewYRot(partial)));
        pose.translate(-0.5f, 0f, -0.5f);
        FuelTankRenderer.renderSingularWidth(2, rocket.level(), rocket.getOnPos(), 0f, pose, buffer, OverlayTexture.NO_OVERLAY, light);
        pose.popPose();
    }

    public void renderBlockState(RocketEntity rocket, MultiBufferSource buffer, PoseStack pose, BlockState state)
    {
        BakedModel model = dispatcher.getBlockModel(state);
        int light = LevelRenderer.getLightColor(rocket.level(), rocket.getOnPos());
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
            blockRenderer.renderModel(pose.last(), buffer.getBuffer(rt), null, model, 1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY);

    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity)
    {
        return new ResourceLocation("block/air");
    }
}
