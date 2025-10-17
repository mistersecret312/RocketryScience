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
    private final Vector2d focus1, focus2, center;
    private final double semiMajorAxis, semiMinorAxis;
    private final double rotationAngle;
    private final Vector2d startPoint, endPoint;

    public boolean isRetrograde;

    public EllipticalPath(Vector2d f1, Vector2d f2, double a, Vector2d start, Vector2d end)
    {
        this.focus1 = f1;
        this.focus2 = f2;
        this.semiMajorAxis = a;
        this.startPoint = start;
        this.endPoint = end;

        // Calculate derived properties needed for drawing
        this.center = f1.add(f2).mul(0.5d);
        double c = f1.distance(center);
        // Ensure semiMinorAxis is not NaN if a and c are very close
        this.semiMinorAxis = (a * a > c * c) ? Math.sqrt(a * a - c * c) : 0;
        Vector2d rotVector = f2.sub(f1);
        this.rotationAngle = Math.atan2(rotVector.x, rotVector.y);

        double startAngle = getParametricAngleForPoint(start);
        double endAngle = getParametricAngleForPoint(end);

        double progradeDistance = endAngle - startAngle;
        if (progradeDistance < 0) {
            progradeDistance += 2 * Math.PI;
        }

        double retrogradeDistance = 2 * Math.PI - progradeDistance;
        this.isRetrograde = retrogradeDistance < progradeDistance;
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
        if (numPoints < 2) {
            return List.of(startPoint, endPoint);
        }

        List<Vector2d> points = new ArrayList<>();

        // 2. Find the start and end angles on the ellipse
        double startAngle = getParametricAngleForPoint(startPoint);
        double endAngle = getParametricAngleForPoint(endPoint);

        // 3. NEW: Determine the shortest direction of travel
        // Calculate the angular distance if we go counter-clockwise (prograde)
        double progradeDistance = endAngle - startAngle;
        if (progradeDistance < 0) {
            progradeDistance += 2 * Math.PI; // Handle angle wraparound (e.g., from 350 deg to 10 deg)
        }

        // The clockwise (retrograde) distance is simply the rest of the circle
        double retrogradeDistance = 2 * Math.PI - progradeDistance;

        double totalAngleToTravel;

        // Choose the smaller of the two angular distances
        if (progradeDistance <= retrogradeDistance) {
            // The shorter path is counter-clockwise, so we travel a positive angle
            totalAngleToTravel = progradeDistance;
        } else {
            // The shorter path is clockwise, so we travel a negative angle
            totalAngleToTravel = -retrogradeDistance;
        }

        // 4. Generate points along the now-guaranteed shortest arc
        double angleStep = totalAngleToTravel / (numPoints - 1);

        for (int i = 0; i < numPoints; i++) {
            double currentAngle = startAngle + i * angleStep;

            Vector2d newPoint = getPointForParametricAngle(currentAngle);

            newPoint.x = trimDouble(newPoint.x);
            newPoint.y = trimDouble(newPoint.y);

            points.add(newPoint);
        }

        return points;
    }

    @Override
    public boolean isRetrograde()
    {
        return isRetrograde;
    }

    /**
     * Converts a parametric angle 't' into a point on this specific rotated ellipse.
     */
    private Vector2d getPointForParametricAngle(double t)
    {
        // 1. Get the point on a standard, un-rotated ellipse at the origin
        double x0 = semiMajorAxis * Math.cos(t);
        double y0 = semiMinorAxis * Math.sin(t);

        // 2. Rotate the point
        double cosRot = Math.cos(rotationAngle);
        double sinRot = Math.sin(rotationAngle);
        double x1 = x0 * cosRot - y0 * sinRot;
        double y1 = x0 * sinRot + y0 * cosRot;

        // 3. Translate the point to the ellipse's center
        return new Vector2d(x1, y1).add(center);
    }

    /**
     * Converts a point in world coordinates into its corresponding parametric angle on the ellipse.
     * This is the inverse of the method above.
     */
    private double getParametricAngleForPoint(Vector2d point)
    {
        // 1. Translate the point to be relative to the center
        Vector2d p0 = point.sub(center);

        // 2. Un-rotate the point
        double cosRot = Math.cos(-rotationAngle);
        double sinRot = Math.sin(-rotationAngle);
        double x1 = p0.x() * cosRot - p0.y() * sinRot;
        double y1 = p0.x() * sinRot + p0.y() * cosRot;

        // 3. Find the angle using atan2
        return Math.atan2(y1 / semiMinorAxis, x1 / semiMajorAxis);
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
