package net.mistersecret312.util.trajectories;

import net.minecraft.util.Mth;
import org.joml.Vector2d;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HyperbolicPath implements OrbitalPath
{
    private final Vector2d f1, f2, center, pointA, pointB;
    private final double a, b, c, angle;
    private final double H_A, H_B; // Hyperbolic "angle" parameters

    public HyperbolicPath(Vector2d f1, Vector2d f2, double a_negative, Vector2d pointA, Vector2d pointB) {
        this.f1 = f1;
        this.f2 = f2;
        this.a = -a_negative; // 'a' is positive by convention, a_negative was from energy calc
        this.pointA = pointA;
        this.pointB = pointB;

        // 1. Calculate hyperbola properties
        this.center = new Vector2d((f1.x + f2.x) / 2.0, (f1.y + f2.y) / 2.0);
        this.c = f1.distance(center); // Focal distance
        this.b = Math.sqrt(c * c - a * a); // Semi-minor axis
        this.angle = Math.atan2(f2.y - f1.y, f2.x - f1.x); // Rotation angle

        // 2. Find the hyperbolic parameters for A and B
        this.H_A = getHyperbolicAngle(pointA);
        this.H_B = getHyperbolicAngle(pointB);
    }

    // This finds the parameter 'H' such that x = a*cosh(H), y = b*sinh(H)
    private double getHyperbolicAngle(Vector2d p) {
        // Translate to center and rotate to align with major axis
        Vector2d p_local = new Vector2d(p).sub(center);
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        double y_prime = p_local.x * sinA + p_local.y * cosA;

        // Use asinh(y'/b) which is more stable and handles sign
        return Math.log((y_prime / b) + Math.sqrt((y_prime / b) * (y_prime / b) + 1)); // asinh
    }

    @Override
    public List<Vector2d> getPathPoints(int segments) {
        List<Vector2d> points = new ArrayList<>(segments + 1);
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            // Simple linear interpolation works for hyperbolic parameter H
            double H = H_A + (H_B - H_A) * t;

            // 1. Get point on unit hyperbola
            // Note: We use the branch closer to F1. F1 is the "interior" focus.
            // The F1-F2 angle determines orientation. We need to check which branch we're on.
            // Our "pick F2" logic should handle this, but let's be safe.
            Vector2d f1_local = new Vector2d(f1).sub(center);
            double f1_local_x = f1_local.x * cosA + f1_local.y * sinA;

            double x_local = a * Math.cosh(H);
            if (f1_local_x < 0) x_local = -x_local; // Use the other branch if F1 is on the -x side

            double y_local = b * Math.sinh(H);

            // 2. Rotate to match hyperbola orientation
            double x_rotated = x_local * cosA - y_local * sinA;
            double y_rotated = x_local * sinA + y_local * cosA;

            // 3. Translate to world position and add
            points.add(new Vector2d(x_rotated + center.x, y_rotated + center.y));
        }
        // Ensure the last point is exactly B
        points.set(segments, new Vector2d(pointB));
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
