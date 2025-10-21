package net.mistersecret312.util.trajectories;

import net.mistersecret312.util.OrbitalMath;
import org.joml.Vector2d;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class EllipticalPath implements OrbitalPath
{
    private final Vector2d f1, f2, center, pointA, pointB;
    private final double a, b, c, angle;
    private final double eccentricAngleA, eccentricAngleB;

    public EllipticalPath(Vector2d f1, Vector2d f2, double a, Vector2d pointA, Vector2d pointB)
    {
        this.f1 = f1;
        this.f2 = f2;
        this.a = a; // Semi-major axis
        this.pointA = pointA;
        this.pointB = pointB;

        // 1. Calculate ellipse properties
        this.center = new Vector2d((f1.x + f2.x) / 2.0, (f1.y + f2.y) / 2.0);
        this.c = f1.distance(center); // Focal distance
        this.b = Math.sqrt(a * a - c * c); // Semi-minor axis
        this.angle = Math.atan2(f2.y - f1.y, f2.x - f1.x); // Rotation angle

        // 2. Find the eccentric anomalies for A and B
        this.eccentricAngleA = getEccentricAngle(pointA);
        this.eccentricAngleB = getEccentricAngle(pointB);
    }

    private double getEccentricAngle(Vector2d p) {
        // Translate to center and rotate to align with major axis
        Vector2d p_local = new Vector2d(p).sub(center);
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        double x_prime = p_local.x * cosA - p_local.y * sinA;
        double y_prime = p_local.x * sinA + p_local.y * cosA;

        // Find angle
        return Math.atan2(y_prime / b, x_prime / a);
    }

    /**
     * Generates a list of points representing the elliptical arc from the start to the end point.
     *
     * @param numPoints The number of line segments to approximate the curve.
     * @return A list of Vector2d points in absolute world coordinates.
     */
    @Override
    public List<Vector2d> getPathPoints(int numPoints)
    {
        List<Vector2d> points = new ArrayList<>(numPoints + 1);
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        for (int i = 0; i <= numPoints; i++) {
            double t = i / (double) numPoints;
            // Interpolate the angle using the short-arc helper
            double E = OrbitalMath.lerpAngle(eccentricAngleA, eccentricAngleB, t, true);

            // 1. Get point on unit ellipse
            double x_local = a * Math.cos(E);
            double y_local = b * Math.sin(E);

            // 2. Rotate to match ellipse orientation
            double x_rotated = x_local * cosA - y_local * sinA;
            double y_rotated = x_local * sinA + y_local * cosA;

            // 3. Translate to world position and add
            points.add(new Vector2d(x_rotated + center.x, y_rotated + center.y));
        }
        // Ensure the last point is exactly B
        points.set(numPoints, new Vector2d(pointB));
        return points;
    }

    @Override
    public boolean isRetrograde()
    {
        return false;
    }

    public double trimDouble(double value)
    {
        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(3);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        return Double.parseDouble(fraction.format(value));
    }
}
