package net.mistersecret312.client.dimension;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.mistersecret312.RocketryScienceMod;
import net.povstalec.sgjourney.client.render.level.StellarViewCompatibility;
import net.povstalec.stellarview.api.client.StellarViewRendering;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class RocketryDimensionalEffects
{
    public static final ResourceLocation LUNA = ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna");

    public static class Luna extends DimensionSpecialEffects
    {
        public Luna()
        {
            super(0f, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight)
        {
            return Vec3.ZERO;
        }

        @Override
        public boolean isFoggyAt(int pX, int pY)
        {
            return false;
        }

        @Override
        public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX,
                                    double camY, double camZ, Matrix4f projectionMatrix)
        {
            return true;
        }

        @Override
        public boolean tickRain(ClientLevel level, int ticks, Camera camera)
        {
            return true;
        }

        @Override
        public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture,
                                         double camX, double camY, double camZ)
        {
            return true;
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                                 Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
        {
            return StellarViewCompatibility.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
        }

        @Override
        public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken,
                                         float blockLightRedFlicker, float skyLight, int pixelX, int pixelY,
                                         Vector3f colors)
        {
            super.adjustLightmapColors(level, partialTicks, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY,
                                       colors);
        }

        @Override
        public @Nullable float[] getSunriseColor(float pTimeOfDay, float pPartialTicks)
        {
            return new float[]{0, 0, 0 ,0};
        }
    }

    public static void register(RegisterDimensionSpecialEffectsEvent event)
    {
        event.register(RocketryDimensionalEffects.LUNA, new Luna());
    }
}
