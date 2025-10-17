package net.mistersecret312.util.trajectories;

import org.joml.Vector2d;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HyperbolicPath implements OrbitalPath
{
    private final Vector2d focus1, focus2, center;
    private final double semiMajorAxis; // Will be negative
    private final double semiMinorAxis;
    private final double rotationAngle;
    private final Vector2d startPoint, endPoint;

    public boolean isRetrograde;

    public HyperbolicPath(Vector2d f1, Vector2d f2, double a, Vector2d start, Vector2d end) {
        this.focus1 = f1;
        this.focus2 = f2;
        this.semiMajorAxis = a; // Keep it negative
        this.startPoint = start;
        this.endPoint = end;

        this.center = f1.add(f2).mul(0.5);
        double c = f1.distance(center);
        // For a hyperbola, c^2 = a^2 + b^2
        this.semiMinorAxis = Math.sqrt(c * c - a * a);
        Vector2d rotVector = f2.sub(f1);
        this.rotationAngle = Math.atan2(rotVector.x, rotVector.y);

        this.isRetrograde = getParameterForPoint(end) < 0;
    }

    @Override
    public List<Vector2d> getPathPoints(int numPoints) {
        if (numPoints < 2) {
            return List.of(startPoint, endPoint);
        }

        List<Vector2d> points = new ArrayList<>();
        double startParam = getParameterForPoint(startPoint);
        double endParam = getParameterForPoint(endPoint);

        double paramStep = (endParam - startParam) / (numPoints - 1);

        for (int i = 0; i < numPoints; i++) {
            double currentParam = startParam + i * paramStep;
            Vector2d newPoint = getPointForParameter(currentParam);

            newPoint.x = trimDouble(newPoint.x);
            newPoint.y = trimDouble(newPoint.y);
            points.add(newPoint);
        }
        return points;
    }

    private Vector2d getPointForParameter(double t) {
        // Note: semiMajorAxis is negative, so we use its absolute value for cosh
        double x0 = Math.abs(semiMajorAxis) * Math.cosh(t);
        double y0 = semiMinorAxis * Math.sinh(t);

        double cosRot = Math.cos(rotationAngle);
        double sinRot = Math.sin(rotationAngle);
        double x1 = x0 * cosRot - y0 * sinRot;
        double y1 = x0 * sinRot + y0 * cosRot;

        return new Vector2d(x1, y1).add(center);
    }

    private double getParameterForPoint(Vector2d point) {
        Vector2d p0 = point.sub(center);

        double cosRot = Math.cos(-rotationAngle);
        double sinRot = Math.sin(-rotationAngle);
        double x1 = p0.x() * cosRot - p0.y() * sinRot;
        double y1 = p0.x() * sinRot + p0.y() * cosRot;

        // Inverse of cosh is acosh. The sign of the parameter 't' depends on the sign of y.
        double t = acosh(x1 / Math.abs(semiMajorAxis));
        return (y1 < 0) ? -t : t;
    }

    // Custom acosh implementation as Math.acosh is not available pre-Java 9
    private static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1.0));
    }

    @Override
    public boolean isRetrograde()
    {
        return isRetrograde;
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
