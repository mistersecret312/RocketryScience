package net.mistersecret312.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
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
import net.mistersecret312.util.OrbitalMath;
import net.mistersecret312.util.trajectories.OrbitalPath;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import java.util.List;
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
        boolean systemMap = false;
        long time = level.getDayTime();

        if(!systemMap)
        {
            graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        }
        else
        {
            Registry<CelestialBody> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);

            for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
            {
                Vector2d pos = new Vector2d((double) (width - 16)/2, (double) (height - 16) /2);

                CelestialBody body = entry.getValue();
                if(body.getOrbit() != null && body.getOrbit().getParent().getParent().isEmpty())
                    drawCircle(graphics, (int) pos.x, (int) pos.y,
                               (float) (45f*body.getAltitude()), 10, 0xFFFFFFFF);
            }

            CelestialBody referenceBody = registry.get(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "sol"));
            if(referenceBody != null)
            {
                CelestialBody mars = registry.get(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "jupiter"));
                CelestialBody terra = registry.get(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "mars"));
                if(mars == null || terra == null || referenceBody == null)
                    return;

                drawTransfer(graphics, referenceBody, terra, mars, time);

                Vector2d pos = new Vector2d((double) (width - 16)/2, (double) (height - 16) /2);
                pos.add(-16, -16);

                referenceBody.render(graphics, pos);
                for(ResourceKey<CelestialBody> childKey : referenceBody.getChildren())
                {
                    CelestialBody child = registry.get(childKey);
                    if(child != null)
                    {
                        Vector2d childPos = child.getCoordinates(child.getAltitude(), child.getOrbit().getAngle(time)).mul(45).add(pos);
                        child.render(graphics, childPos);
                    }
                }
            }
        }
        pose.popPose();
    }

    private void drawTransfer(GuiGraphics graphics, CelestialBody reference, CelestialBody departure, CelestialBody target, long time)
    {
        // --- 1. CALCULATE IDEAL TRANSFER & OPTIMAL ANGLE ---
        // This is the "base" time for a perfect (100% efficient) Hohmann transfer.
        double idealTransferTime = 10 * Math.pow(((departure.altitude + target.altitude) / 2), 1.5) / 2;

        // Use this ideal time to find the optimal angle
        double angleMoved = idealTransferTime * (360 / target.getOrbit().orbitalPeriod);
        double optimalAngle = 180 - angleMoved; // This is the goal angle

        // --- 2. CALCULATE CURRENT PHASE ANGLE & EFFICIENCY (NEW) ---
        double currentPhaseAngle = (target.getOrbit().getAngle(time) - departure.getOrbit().getAngle(time));
        currentPhaseAngle = (currentPhaseAngle % 360 + 360) % 360; // Normalize to 0-360

        // Find the shortest angular distance (error) between current and optimal
        double angularError = currentPhaseAngle - optimalAngle;
        angularError = (angularError % 360 + 360) % 360; // Normalize
        if (angularError > 180) {
            angularError -= 360; // Get shortest path (e.g., -10° instead of 350°)
        }

        // Convert the error (from -180 to 180) into an efficiency score (0-100)
        // cos(0) = 1 (perfect) --> 100%
        // cos(180) = -1 (worst) --> 0%
        double efficiencyScore = (Math.cos(Math.toRadians(angularError)) + 1) / 2;
        double efficiency = efficiencyScore * 100; // This is the 0-100 "grade" you wanted.

        // --- 3. CALCULATE ACTUAL (EFFICIENCY-MODIFIED) TRANSFER TIME ---
        // This line is from your original code. It seems to make the trip *faster* // if efficiency is lower, implying a brute-force burn.
        double actualTransferTime = idealTransferTime * (Math.max(efficiency, 1) / 100);

        // --- 4. CALCULATE WAIT TIME ---
        double speedA = 360D / departure.getOrbit().orbitalPeriod;
        double speedB = 360D / target.getOrbit().orbitalPeriod;
        double relativeSpeed = Math.abs(speedA - speedB);

        double catchUp = (optimalAngle - currentPhaseAngle);
        catchUp = (catchUp % 360 + 360) % 360; // Normalize catchUp

        // Avoid division by zero if planets have the same speed
        if (relativeSpeed > 1e-9)
        {
            double windowTime = catchUp / relativeSpeed;
            double windowTimeTicks = windowTime * 24000D;

            Vector2d sun = new Vector2d((double) (width - 16) / 2, (double) (height - 16) / 2);
            Vector2d a = departure.getCoordinates(departure.getAltitude(),
                                                  departure.getOrbit().getAngle(time)).mul(45).add(sun);

            // **FIXED:** The arrival point 'b' must use the 'actualTransferTime'
            // to find the correct future position.
            long arrivalTimeInTicks = time + (long)(actualTransferTime * 24000D);
            Vector2d b = target.getCoordinates(target.getAltitude(),
                                               target.getOrbit().getAngle(arrivalTimeInTicks)).mul(45).add(sun);

            graphics.drawCenteredString(Minecraft.getInstance().font, "[]", (int) b.x, (int) b.y, 0x00FFFF);

            // Pass the 0.0-1.0 score to your path calculator
            OrbitalPath path = OrbitalMath.calculatePath(sun, a, b, efficiencyScore, reference.getGravitationalParameter());
            if (path != null) {
                List<Vector2d> points = path.getPathPoints(200);
                for (Vector2d point : points) {
                    graphics.drawCenteredString(Minecraft.getInstance().font, ".", (int) point.x, (int) point.y, 0x00FF00);
                }
            }
        }
    }

    private void drawCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int numSegments, int color)
    {
        float rSquared = radius * radius;

        for(int y = (int) -radius; y <= radius; y++)
        {
            for(int x = (int) -radius; x <= radius; x++)
            {
                if (Math.abs(x * x + y * y - rSquared) < radius)
                    graphics.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);

            }
        }
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
