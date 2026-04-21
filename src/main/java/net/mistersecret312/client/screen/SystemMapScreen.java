package net.mistersecret312.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.menus.SystemMapMenu;
import net.mistersecret312.util.OrbitalMath;
import net.mistersecret312.util.trajectories.OrbitalPath;
import org.joml.Vector2d;

import java.util.*;

import static net.mistersecret312.RocketryScienceMod.MODID;
import static net.mistersecret312.datapack.CelestialBody.REGISTRY_KEY;

public class SystemMapScreen extends AbstractContainerScreen<SystemMapMenu>
{
    // --- CONSTANTS ---
    /** The scale factor for rendering orbits. From your original code. */
    private static final double ORBIT_SCALE = 45.0;
    /** The clickable radius around a body's icon. Adjust as needed. */
    private static final int BODY_CLICK_RADIUS = 8;
    /** The default body to center on (e.g., the Sun). */
    private static final ResourceKey<CelestialBody> DEFAULT_CENTER_KEY = ResourceKey.create(
            REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(MODID, "sol")
    );

    private ResourceKey<CelestialBody> centeredBodyKey;
    private ResourceKey<CelestialBody> selectedDepartureBodyKey;
    private ResourceKey<CelestialBody> selectedTargetBodyKey;

    private double zoom = 0.75; // Start with the zoom from your original code
    private Vector2d viewOffset = new Vector2d(0, 0);
    private boolean isDragging = false;

    /** Stores the calculated screen position (center) of each rendered body. */
    private final Map<ResourceKey<CelestialBody>, Vector2d> bodyScreenPositions = new HashMap<>();
    /** Stores the clickable area for each rendered body. */
    private final Map<ResourceKey<CelestialBody>, Rect2i> bodyClickAreas = new HashMap<>();

    public SystemMapScreen(SystemMapMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle);
        this.centeredBodyKey = DEFAULT_CENTER_KEY;
    }

    @Override
    protected void init() {
        super.init();
        this.viewOffset = new Vector2d(0, 0);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }


    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        Registry<CelestialBody> registry = level.registryAccess().registryOrThrow(REGISTRY_KEY);
        if (registry == null) return false;

        for (Map.Entry<ResourceKey<CelestialBody>, net.minecraft.client.renderer.Rect2i> entry : this.bodyClickAreas.entrySet()) {
            if (entry.getValue().contains((int) pMouseX, (int) pMouseY)) {
                ResourceKey<CelestialBody> clickedBodyKey = entry.getKey();

                if (pButton == 0) {
                    this.centeredBodyKey = clickedBodyKey;
                    this.viewOffset = new Vector2d(0, 0);
                    return true;
                }

                if (pButton == 1) {
                    if (this.selectedDepartureBodyKey == null || clickedBodyKey.equals(this.selectedDepartureBodyKey)) {
                        this.selectedDepartureBodyKey = clickedBodyKey;
                        this.selectedTargetBodyKey = null;
                    } else if (this.selectedTargetBodyKey == null) {

                        boolean isParentChild = isAncestor(registry, this.selectedDepartureBodyKey, clickedBodyKey) ||
                                                        isAncestor(registry, clickedBodyKey, this.selectedDepartureBodyKey);

                        if (!isParentChild) {
                            this.selectedTargetBodyKey = clickedBodyKey;
                        } else {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        Component.literal("Cannot target a parent or child body."), true);
                            }
                        }

                    } else {
                        this.selectedDepartureBodyKey = clickedBodyKey;
                        this.selectedTargetBodyKey = null;
                    }
                    return true;
                }
            }
        }

        if (pButton == 2) {
            this.isDragging = true;
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 2) {
            this.isDragging = false;
            return true;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }


    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isDragging) {
            this.viewOffset.add(new Vector2d(pDragX, pDragY));
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        double zoomFactor = (pDelta > 0) ? 1.1 : 0.9;
        this.zoom *= zoomFactor;
        this.zoom = Mth.clamp(this.zoom, 0.05, 10.0);
        return true;
    }


    @Override
    public void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.bodyScreenPositions.clear();
        this.bodyClickAreas.clear();
        this.renderBackground(graphics);

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        long time = level.getDayTime();
        Registry<CelestialBody> registry = level.registryAccess().registryOrThrow(REGISTRY_KEY);

        ResourceKey<CelestialBody> rootKey = getRootKey(registry, this.centeredBodyKey);
        if (rootKey == null) return;

        Vector2d rootScreenPos = calculateRootScreenPos(registry, time);

        renderBodyAndChildren(graphics, registry, rootKey, rootScreenPos, time);

        drawTransferUtility(graphics, registry, time);

        for (Map.Entry<ResourceKey<CelestialBody>, net.minecraft.client.renderer.Rect2i> entry : this.bodyClickAreas.entrySet()) {
            if (entry.getValue().contains(pMouseX, pMouseY)) {
                CelestialBody body = registry.get(entry.getKey());
                if (body != null) {
                    graphics.renderTooltip(this.font, body.getName(), pMouseX, pMouseY);
                    break;
                }
            }
        }

        if (selectedDepartureBodyKey != null) {
            graphics.drawString(this.font, "From: " + registry.get(selectedDepartureBodyKey).getName().getString(), 5, 5, 0xFFFFFF);
        }
        if (selectedTargetBodyKey != null) {
            graphics.drawString(this.font, "To: " + registry.get(selectedTargetBodyKey).getName().getString(), 5, 15, 0xFFFFFF);
        }
    }



    /**
     * Calculates the screen position of the system's root body ("sol")
     * based on the currently centered body, zoom, and pan.
     */
    private Vector2d calculateRootScreenPos(Registry<CelestialBody> registry, long time) {
        Vector2d pos = new Vector2d(this.width / 2.0, this.height / 2.0).add(this.viewOffset);

        List<ResourceKey<CelestialBody>> pathToRoot = getPathToRoot(registry, this.centeredBodyKey);

        for (int i = pathToRoot.size() - 2; i >= 0; i--) {
            CelestialBody childBody = registry.get(pathToRoot.get(i));
            if (childBody == null || childBody.getOrbit() == null) continue;

            Vector2d relPos = childBody.getCoordinates(childBody.getAltitude(), childBody.getOrbit().getAngle(time));

            pos.add(relPos.mul(-1 * this.zoom * ORBIT_SCALE));
        }
        return pos;
    }

    /**
     * Recursively renders a body and all of its children.
     */
    private void renderBodyAndChildren(GuiGraphics graphics, Registry<CelestialBody> registry, ResourceKey<CelestialBody> bodyKey, Vector2d screenPos, long time) {
        CelestialBody body = registry.get(bodyKey);
        if (body == null) return;

        final int padding = 64;
        boolean isBodyInView = screenPos.x >= -padding && screenPos.x <= this.width + padding &&
                                       screenPos.y >= -padding && screenPos.y <= this.height + padding;

        if (isBodyInView) {
            body.render(graphics, new Vector2d(screenPos).add(-16, -16));

            this.bodyScreenPositions.put(bodyKey, screenPos);
            int radius = BODY_CLICK_RADIUS;
            net.minecraft.client.renderer.Rect2i clickArea = new net.minecraft.client.renderer.Rect2i(
                    (int) (screenPos.x - radius), (int) (screenPos.y - radius), radius * 2, radius * 2
            );
            this.bodyClickAreas.put(bodyKey, clickArea);
        }

        boolean canRenderChildren = false;
        if (body.getOrbit() == null) {
            canRenderChildren = true;
        } else if (bodyKey.equals(this.centeredBodyKey) || isAncestor(registry, bodyKey, this.centeredBodyKey)) {
            canRenderChildren = true;
        }

        if (canRenderChildren) {
            final float MIN_VISIBLE_RADIUS = 10F;
            final float MAX_VISIBLE_RADIUS = 700F;

            for (ResourceKey<CelestialBody> childKey : body.getChildren()) {
                CelestialBody childBody = registry.get(childKey);
                if (childBody == null || childBody.getOrbit() == null)
                    continue;

                float orbitRadius = (float) (childBody.getAltitude() * this.zoom * ORBIT_SCALE);

                Vector2d relPos = childBody.getCoordinates(childBody.getAltitude(), childBody.getOrbit().getAngle(time));
                Vector2d childScreenPos = new Vector2d(relPos).mul(this.zoom * ORBIT_SCALE).add(screenPos);

                boolean isChildInView = childScreenPos.x >= -padding && childScreenPos.x <= this.width + padding &&
                                               childScreenPos.y >= -padding && childScreenPos.y <= this.height + padding;

                if (orbitRadius > MIN_VISIBLE_RADIUS &&
                            isCircleInView(screenPos, orbitRadius) && isChildInView) {
                    if(orbitRadius < MAX_VISIBLE_RADIUS)
                        drawCircle(graphics, (int) screenPos.x, (int) screenPos.y,
                            orbitRadius, 10, 0xFFFFFFFF);
                }
                else continue;

                renderBodyAndChildren(graphics, registry, childKey, childScreenPos, time);
            }
        }
    }

    /** Gets the path from a body up to the root (e.g., [Earth, Sol]). */
    private List<ResourceKey<CelestialBody>> getPathToRoot(Registry<CelestialBody> registry, ResourceKey<CelestialBody> bodyKey) {
        List<ResourceKey<CelestialBody>> path = new ArrayList<>();
        ResourceKey<CelestialBody> currentKey = bodyKey;
        CelestialBody currentBody;

        while (currentKey != null) {
            path.add(currentKey);
            currentBody = registry.get(currentKey);
            if (currentBody == null || currentBody.getOrbit() == null) {
                break;
            }
            currentKey = currentBody.parent;
        }
        return path;
    }

    /** Finds the root body of a given body. */
    private ResourceKey<CelestialBody> getRootKey(Registry<CelestialBody> registry, ResourceKey<CelestialBody> bodyKey) {
        List<ResourceKey<CelestialBody>> path = getPathToRoot(registry, bodyKey);
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    /** Finds the common ancestor of two bodies (e.g., Sol for Earth and Mars). */
    private ResourceKey<CelestialBody> findCommonAncestorKey(Registry<CelestialBody> registry, CelestialBody bodyA, CelestialBody bodyB) {
        List<ResourceKey<CelestialBody>> pathA = getPathToRoot(registry, ResourceKey.create(registry.key(), registry.getKey(bodyA)));
        List<ResourceKey<CelestialBody>> pathB = getPathToRoot(registry, ResourceKey.create(registry.key(), registry.getKey(bodyB)));

        Set<ResourceKey<CelestialBody>> ancestorsA = new HashSet<>(pathA);

        for (ResourceKey<CelestialBody> keyB : pathB) {
            if (ancestorsA.contains(keyB)) {
                return keyB;
            }
        }
        return null;
    }


    /** Wrapper to draw the transfer orbit if bodies are selected. */
    private void drawTransferUtility(GuiGraphics graphics, Registry<CelestialBody> registry, long time) {
        if (this.selectedDepartureBodyKey == null || this.selectedTargetBodyKey == null) {
            return;
        }

        CelestialBody departure = registry.get(this.selectedDepartureBodyKey);
        CelestialBody target = registry.get(this.selectedTargetBodyKey);
        if (departure == null || target == null) return;

        ResourceKey<CelestialBody> referenceKey = findCommonAncestorKey(registry, departure, target);
        if (referenceKey == null) return;

        CelestialBody reference = registry.get(referenceKey);
        Vector2d referenceScreenPos = this.bodyScreenPositions.get(referenceKey);

        if (reference != null && referenceScreenPos != null) {
            drawTransfer(graphics, reference, departure, target, time, referenceScreenPos);
        }
    }

    /**
     * Draws a Hohmann transfer path. (Your original code, modified for dynamic pos/zoom).
     */
    private void drawTransfer(GuiGraphics graphics, CelestialBody reference, CelestialBody departure, CelestialBody target, long time, Vector2d referenceScreenPos) {
        double idealTransferTime = 10 * Math.pow(((departure.getAltitude() + target.getAltitude()) / 2), 1.5) / 2;
        double angleMoved = idealTransferTime * (360 / target.getOrbit().orbitalPeriod);
        double optimalAngle = 180 - angleMoved;

        double currentPhaseAngle = (target.getOrbit().getAngle(time) - departure.getOrbit().getAngle(time));
        currentPhaseAngle = (currentPhaseAngle % 360 + 360) % 360;
        double angularError = currentPhaseAngle - optimalAngle;
        angularError = (angularError % 360 + 360) % 360;
        if (angularError > 180) {
            angularError -= 360;
        }
        double efficiencyScore = (Math.cos(Math.toRadians(angularError)) + 1) / 2;
        double efficiency = efficiencyScore * 100;

        double actualTransferTime = idealTransferTime * (Math.max(efficiency, 1) / 100);

        double speedA = 360D / departure.getOrbit().orbitalPeriod;
        double speedB = 360D / target.getOrbit().orbitalPeriod;
        double relativeSpeed = Math.abs(speedA - speedB);

        if (relativeSpeed > 0) {
            Vector2d sun = referenceScreenPos;

            Vector2d a = departure.getCoordinates(departure.getAltitude(),
                                                  departure.getOrbit().getAngle(time))
                                  .mul(this.zoom * ORBIT_SCALE)
                                  .add(sun);

            long arrivalTimeInTicks = time + (long) (actualTransferTime * 24000D);
            Vector2d b = target.getCoordinates(target.getAltitude(),
                                               target.getOrbit().getAngle(arrivalTimeInTicks))
                               .mul(this.zoom * ORBIT_SCALE)
                               .add(sun);

            graphics.drawCenteredString(Minecraft.getInstance().font, "[]", (int) b.x, (int) b.y, 0x00FFFF);

            OrbitalPath path = OrbitalMath.calculatePath(sun, a, b, efficiency, reference.getGravitationalParameter());
            if (path != null) {
                List<Vector2d> points = path.getPathPoints(100);
                for (Vector2d point : points) {
                    graphics.drawCenteredString(Minecraft.getInstance().font, ".", (int) point.x, (int) point.y, 0x00FF00);
                }
            }
        }
    }

    /**
     * Draws a circle. (Your original helper method).
     */
    private void drawCircle(GuiGraphics graphics, int centerX, int centerY, float radius, int numSegments, int color) {
        int r = (int) radius;
        int x = r;
        int y = 0;

        // Plot the first points
        graphics.fill(centerX + x, centerY, centerX + x + 1, centerY + 1, color); // Right
        graphics.fill(centerX - x, centerY, centerX - x + 1, centerY + 1, color); // Left
        graphics.fill(centerX, centerY + r, centerX + 1, centerY + r + 1, color); // Top
        graphics.fill(centerX, centerY - r, centerX + 1, centerY - r + 1, color); // Bottom

        int P = 1 - r;
        while (x > y) {
            y++;

            if (P <= 0) {
                P = P + 2 * y + 1;
            } else {
                x--;
                P = P + 2 * y - 2 * x + 1;
            }

            if (x < y)
                break;

            // Plot 8-way symmetry points
            graphics.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
            graphics.fill(centerX - x, centerY + y, centerX - x + 1, centerY + y + 1, color);
            graphics.fill(centerX + x, centerY - y, centerX + x + 1, centerY - y + 1, color);
            graphics.fill(centerX - x, centerY - y, centerX - x + 1, centerY - y + 1, color);

            if (x != y) {
                graphics.fill(centerX + y, centerY + x, centerX + y + 1, centerY + x + 1, color);
                graphics.fill(centerX - y, centerY + x, centerX - y + 1, centerY + x + 1, color);
                graphics.fill(centerX + y, centerY - x, centerX + y + 1, centerY - x + 1, color);
                graphics.fill(centerX - y, centerY - x, centerX - y + 1, centerY - x + 1, color);
            }
        }
    }

    /**
     * Checks if one body is an ancestor (parent, grandparent, etc.) of another.
     * @param registry The celestial body registry.
     * @param potentialAncestor The body to check if it's in the other's lineage.
     * @param body The body whose ancestors will be checked.
     * @return true if potentialAncestor is an ancestor of body, false otherwise.
     */
    private boolean isAncestor(Registry<CelestialBody> registry, ResourceKey<CelestialBody> potentialAncestor, ResourceKey<CelestialBody> body) {
        // A body cannot be its own ancestor
        if (potentialAncestor.equals(body)) return false;

        List<ResourceKey<CelestialBody>> path = getPathToRoot(registry, body);
        // We skip the body itself (which is at index 0)
        for (int i = 1; i < path.size(); i++) {
            if (path.get(i).equals(potentialAncestor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a circle is visible on the screen.
     *
     * @param center The center position of the circle.
     * @param radius The radius of the circle.
     * @return true if any part of the circle is within the screen bounds, false otherwise.
     */
    private boolean isCircleInView(Vector2d center, float radius) {
        // Find the closest point on the screen's AABB (0,0 to width,height) to the circle's center
        double closestX = Mth.clamp(center.x, 0, this.width);
        double closestY = Mth.clamp(center.y, 0, this.height);

        // Calculate the distance squared from the circle's center to this closest point
        double distanceX = center.x - closestX;
        double distanceY = center.y - closestY;
        double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        // If the distance squared is less than the radius squared, they are intersecting
        return distanceSquared < (radius * radius);
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
