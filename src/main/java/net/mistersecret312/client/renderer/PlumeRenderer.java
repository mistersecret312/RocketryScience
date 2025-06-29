package net.mistersecret312.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.client.RocketRenderTypes;
import net.mistersecret312.client.model.PlumeModel;

public class PlumeRenderer implements BlockEntityRenderer<RocketEngineBlockEntity>
{
    private PlumeModel model;
    public  PlumeRenderer(PlumeModel model)
    {
        this.model = model;
    }

    @Override
    public void render(RocketEngineBlockEntity rocketEngine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay)
    {
        if(rocketEngine.getBlueprint() != null && rocketEngine.isRunning() && rocketEngine.getNozzle() != null)
        {
            int length = Math.max(0, Math.min(4, rocketEngine.throttle-2));
            rocketEngine.animTick++;
            int offset = 0;
            if(rocketEngine.animTick > 10)
            {
                rocketEngine.frame++;
                rocketEngine.animTick = 0;
            }
            if(rocketEngine.frame > 1)
                rocketEngine.frame = 0;

            ResourceLocation textureStart = new ResourceLocation(RocketryScienceMod.MODID, "textures/misc/plume/hydrolox/atmosphere/start/"+rocketEngine.frame+".png");
            ResourceLocation textureMiddle = new ResourceLocation(RocketryScienceMod.MODID, "textures/misc/plume/hydrolox/atmosphere/middle/"+rocketEngine.frame+".png");
            ResourceLocation textureEnd = new ResourceLocation(RocketryScienceMod.MODID, "textures/misc/plume/hydrolox/atmosphere/end/"+rocketEngine.frame+".png");

            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(rocketEngine.getBlockState().getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
            poseStack.translate(0f, 1f, 0f);

            poseStack.scale(1.375f, 1, 1.375f);

            this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(textureStart)), LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();

            for (int segment = 0; segment < length; segment++)
            {
                offset++;
                poseStack.pushPose();
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(rocketEngine.getBlockState().getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
                poseStack.translate(0f, 2+segment, 0f);

                poseStack.scale(1.375f, 1, 1.375f);
                this.model.renderToBuffer(poseStack, buffer.getBuffer(RocketRenderTypes.entityCutout(textureMiddle)), LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
                poseStack.popPose();
            }

            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(rocketEngine.getBlockState().getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
            poseStack.translate(0f, 2+offset, 0f);

            poseStack.scale(1.375f, 1, 1.375f);
            this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(textureEnd)), LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RocketEngineBlockEntity pBlockEntity)
    {
        return true;
    }

    @Override
    public boolean shouldRender(RocketEngineBlockEntity pBlockEntity, Vec3 pCameraPos)
    {
        return true;
    }
}
