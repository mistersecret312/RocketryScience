package net.mistersecret312.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RocketRenderTypes extends RenderType
{

    public RocketRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize,
                             boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState,
                             Runnable pClearState)
    {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static RenderType plume(ResourceLocation rl)
    {
        return create("plume", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER)
                        .setTextureState(new TextureStateShard(rl, false, false))
                        .setCullState(CULL)
                        .createCompositeState(true));
    }

    public static RenderType frost()
    {
        return create("frost", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_TEXT_BACKGROUND_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(true));
    }

    public static RenderType fuelTank()
    {
        return create("cutout", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
                131072, true, false,
                RenderType.CompositeState.builder()
                        .setLightmapState(LIGHTMAP)
                        .setShaderState(RENDERTYPE_CUTOUT_SHADER)
                        .setCullState(RenderStateShard.CULL)
                        .setTextureState(BLOCK_SHEET)
                        .createCompositeState(true));

    }
}
