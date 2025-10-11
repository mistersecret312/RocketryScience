package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.rocket.Rocket;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import org.joml.Vector2f;

public class Orbit
{
    private static final String PARENT = "parent";
    private static final String SPACE_OBJECT = "space_object";
    private static final String IS_ARTIFICIAL = "is_artificial";
    private static final String ECCENTRICITY = "eccentricity";
    private static final String SMA = "semi_major_axis";
    private static final String ARGUMENT = "argument_periapsis";
    private static final String ANOMALY = "true_anomaly";

    // 2D orbital state
    private double semiMajorAxis;  // meters
    private double eccentricity;   // 0=circle, <1 ellipse
    private double trueAnomaly;    // radians
    private double argumentOfPeriapsis; // radians

    // Derived quantities
    public double orbitalPeriod;  // seconds
    private double radius;         // current distance from parent center
    public double angle;          // position angle for rendering

    public CelestialBody parent;
    public SpaceObject spaceObject;

    public Orbit(CelestialBody parent, double altitude) {
        this(parent, parent.getRadius() + altitude, 0, 0,0); // circular by default
    }

    public Orbit(CelestialBody parent, double semiMajorAxis, double eccentricity, double argumentOfPeriapsis, double trueAnomaly) {
        this.parent = parent;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.argumentOfPeriapsis = argumentOfPeriapsis;
        this.trueAnomaly = trueAnomaly;
        recalcDerived();
    }

    /**
     *
     * @param level - Level, used to access datapack registries
     * @param speed - siparent.getGravitationalParameter()lation rate, default should be 0.05, because 1 tick is 1/20th of a second
     */
    public void tick(Level level, double speed)
    {
        double n = Math.sqrt(parent.getGravitationalParameter() / (semiMajorAxis * semiMajorAxis * semiMajorAxis));
        trueAnomaly += n * speed;
        if (trueAnomaly > 2 * Math.PI) trueAnomaly -= 2 * Math.PI;

        // Compute radius in 2D orbit
        radius = (semiMajorAxis * (1 - eccentricity * eccentricity))
                / (1 + eccentricity * Math.cos(trueAnomaly));

        // Compute position angle for visualization
        angle = argumentOfPeriapsis + trueAnomaly;

        double parameter = parent.getGravitationalParameter();

        // Check for SOI transitions
        checkSOI(level);
    }

    /**
     * Apply a burn.
     * @param deltaV Magnitude of velocity change (m/s)
     * @param direction Direction relative to current velocity (radians)
     *                  0 = prograde, PI = retrograde, PI/2 = radial outward, -PI/2 = radial inward
     */
    public void applyBurn(double deltaV, double direction) {
        // Current orbital velocity magnitude
        double vCurrent = Math.sqrt(parent.getGravitationalParameter() * (2 / radius - 1 / semiMajorAxis));

        // Δv components in orbital plane
        double deltaVr = deltaV * Math.sin(direction); // radial component
        double deltaVt = deltaV * Math.cos(direction); // tangential component

        // Assume current velocity tangential to orbit for simplicity
        double vt = vCurrent + deltaVt;

        // New semi-major axis via vis-viva
        double vNew = Math.sqrt(deltaVr * deltaVr + vt*vt);
        semiMajorAxis = 1 / ((2 / radius) - (vNew * vNew / parent.getGravitationalParameter()));

        // Update eccentricity vector automatically
        double ex = (vt*vt + deltaVr * deltaVr - parent.getGravitationalParameter()/radius) * Math.cos(angle) - radius* deltaVr *vt / parent.getGravitationalParameter();
        double ey = (vt*vt + deltaVr * deltaVr - parent.getGravitationalParameter()/radius) * Math.sin(angle) - radius* deltaVr *vt / parent.getGravitationalParameter();
        eccentricity = Math.sqrt(ex*ex + ey*ey);

        // Argument of periapsis
        argumentOfPeriapsis = Math.atan2(ey, ex);

        // True anomaly from current position
        double cosNu = (semiMajorAxis*(1 - eccentricity*eccentricity)/radius - 1) / eccentricity;
        trueAnomaly = Math.acos(Math.max(-1, Math.min(1, cosNu)));
        if (vt < 0) trueAnomaly = 2 * Math.PI - trueAnomaly;

        recalcDerived();
    }
    
    /** Recalculate derived state variables */
    private void recalcDerived() {
        radius = semiMajorAxis * (1 - eccentricity * eccentricity) / (1 + eccentricity * Math.cos(trueAnomaly));
        angle = argumentOfPeriapsis + trueAnomaly;

        double parameter = parent.getGravitationalParameter();

        orbitalPeriod = 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / parent.getGravitationalParameter());
    }

    /** Check if orbiter should enter/leave Sphere of Influence */
    private void checkSOI(Level level) {
        for (ResourceKey<CelestialBody> bodyKey : parent.getChildren())
        {
            CelestialBody body = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).get(bodyKey);
            if(body == null)
                continue;

            double distance = getDistanceTo(body);
            if (distance < body.getRadiusSphereOfInfluence()) {
                // Enter new SOI
                parent = body;
                recalcDerived();
                return;
            }
        }

        // Check if we left current parent's SOI
        double distFromParent = getDistanceTo(parent);
        if (parent.getParent().isPresent() && distFromParent > parent.getRadiusSphereOfInfluence())
        {
            parent = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).get(parent.getParent().get());
            recalcDerived();
        }
    }

    private double getDistanceTo(CelestialBody body) {
        // Replace with real vector math if you have positions
        return Math.abs(radius - body.getRadius());
    }

    public CompoundTag save(Level level)
    {
        CompoundTag tag = new CompoundTag();

        tag.putDouble(SMA, semiMajorAxis);
        tag.putDouble(ECCENTRICITY, eccentricity);

        ResourceLocation rl = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).getKey(parent);
        if(rl == null)
            rl = new ResourceLocation(RocketryScienceMod.MODID, "error");
        tag.putString(PARENT, rl.toString());
        tag.putBoolean(IS_ARTIFICIAL, spaceObject instanceof SpaceCraft);
        tag.put(SPACE_OBJECT, spaceObject.save(level));

        return tag;
    }

    public static Orbit load(Level level, CompoundTag tag)
    {
        CelestialBody parent = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY)
                                    .get(ResourceLocation.parse(tag.getString(PARENT)));

        double sma = tag.getDouble(SMA);
        double e = tag.getDouble(ECCENTRICITY);
        double argument = tag.getDouble(ARGUMENT);
        double anomaly = tag.getDouble(ANOMALY);

        Orbit orbit = new Orbit(parent, sma, e, argument, anomaly);
        boolean artificial = tag.getBoolean(IS_ARTIFICIAL);
        SpaceObject object;
        if(artificial)
        {
            object = new SpaceCraft();
            object.load(level, tag.getCompound(SPACE_OBJECT));
        }
        else
        {
            CompoundTag compoundTag = tag.getCompound(SPACE_OBJECT);
            ResourceLocation key = ResourceLocation.parse(compoundTag.getString("key"));
            object = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).get(key);
        }

        orbit.spaceObject = object;

        return orbit;
    }

    public double getPeriapsis() {
        return semiMajorAxis * (1 - eccentricity);
    }

    // Farthest point from parent
    public double getApoapsis() {
        return semiMajorAxis * (1 + eccentricity);
    }

    // Orbital period in seconds
    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    // Current radius (distance from parent)
    public double getRadius() {
        return radius;
    }

    public double getAngle() {
        return angle;
    }

    // Orbital elements
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getArgumentOfPeriapsis() {
        return argumentOfPeriapsis;
    }

    public double getTrueAnomaly() {
        return trueAnomaly;
    }

    public CelestialBody getParent()
    {
        return parent;
    }
}
