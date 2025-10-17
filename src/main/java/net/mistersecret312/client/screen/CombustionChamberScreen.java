package net.mistersecret312.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.menus.CombustionChamberMenu;
import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.util.Map;

public class CombustionChamberScreen extends AbstractContainerScreen<CombustionChamberMenu>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "textures/gui/steel_combustion_engine_gui.png");

    public CombustionChamberScreen(CombustionChamberMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 237;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY)
    {
        PoseStack pose = graphics.pose();
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null)
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pose.pushPose();
        boolean systemMap = true;
        if(!systemMap)
        {
            graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        }
        else
        {
            Registry<CelestialBody> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
            for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
            {
                CelestialBody body = entry.getValue();
                Vector2d pos = new Vector2d(0,0);
                long time = level.getDayTime();
                if(body.getOrbit() == null)
                {
                    pos = new Vector2d(0, 0);
                }
                else
                {
                    pos = body.getCoordinates(body.getAltitude(), body.getOrbit().getAngle(time)).mul(70);
                    CelestialBody parentBody = body.getOrbit().getParent();
                    if(parentBody.getOrbit() != null)
                        pos.add(parentBody.getCoordinates(parentBody.getAltitude(), parentBody.getOrbit().getAngle(time)).mul(70));
                }
                pose.pushPose();
                pose.translate(width/2, height/2, 0);

                if(body.getOrbit() != null)
                    drawCircle(graphics, x, y, (float) body.getOrbit().getOrbitalAltitude(), 10, ChatFormatting.AQUA.getColor().intValue());

                graphics.blit(body.getTexture(), (int) (pos.x), (int) (pos.y), 0, 0,
                              32, 32, 32, 32);
                graphics.drawString(Minecraft.getInstance().font, body.getName(), (int) (pos.x), (int) (pos.y),
                                    ChatFormatting.WHITE.getColor().intValue());
                pose.popPose();
            }
        }
        pose.popPose();
    }

    private void drawCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int numSegments, int color) {
        // Get the pose matrix from the GuiGraphics object
        Matrix4f matrix = graphics.pose().last().pose();

        // Get the Tesselator and BufferBuilder instances
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // Set the shader to render simple colored vertices
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Extract individual color components (Red, Green, Blue, Alpha)
        // The bitwise operations isolate each 8-bit color channel.
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;

        // Begin drawing a "LINE_STRIP", which connects all vertices in order
        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // Loop from 0 to numSegments to create each vertex of the circle
        for (int i = 0; i <= numSegments; i++) {
            // Calculate the angle for the current vertex
            double angle = i * 2.0 * Math.PI / numSegments;

            // Calculate the (x, y) position of the vertex using trigonometry
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));

            // Add the vertex to the buffer with its position and color
            bufferBuilder.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }

        // Finish and draw all the vertices
        BufferUploader.drawWithShader(bufferBuilder.end());

        // Reset the shader back to the default for GUI rendering
        // This is crucial so it doesn't break other rendering operations.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY)
    {

    }
}
