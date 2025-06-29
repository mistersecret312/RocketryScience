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
                true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setTextureState(new TextureStateShard(rl, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .createCompositeState(true));
    }
}
