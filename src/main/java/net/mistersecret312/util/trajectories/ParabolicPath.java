package net.mistersecret312.util.trajectories;

import net.mistersecret312.util.OrbitalMath;
import org.joml.Vector2d;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ParabolicPath implements OrbitalPath
{
    private final Vector2d f1, pointA, pointB;
    private final double p; // Semi-latus rectum
    private final double omega; // Angle of periapsis
    private final double thetaA, thetaB; // Polar angles of A and B

    public ParabolicPath(Vector2d f1, Vector2d pointA, Vector2d pointB) {
        this.f1 = f1;
        this.pointA = pointA;
        this.pointB = pointB;

        // 1. Convert A and B to polar coordinates relative to F1
        double rA = f1.distance(pointA);
        double rB = f1.distance(pointB);
        this.thetaA = Math.atan2(pointA.y - f1.y, pointA.x - f1.x);
        this.thetaB = Math.atan2(pointB.y - f1.y, pointB.x - f1.x);

        // 2. Solve for omega (angle of periapsis)
        // rB(1 + cos(th_B - w)) = rA(1 + cos(th_A - w))
        // rB - rA = rA*cos(th_A-w) - rB*cos(th_B-w)
        // rB - rA = (rA*cosA - rB*cosB)cos(w) + (rA*sinA - rB*sinB)sin(w)
        double C = rB - rA;
        double K1 = rA * Math.cos(thetaA) - rB * Math.cos(thetaB);
        double K2 = rA * Math.sin(thetaA) - rB * Math.sin(thetaB);
        double R = Math.sqrt(K1 * K1 + K2 * K2);
        double alpha = Math.atan2(K2, K1);

        // C = R * cos(w - alpha)
        // There are two solutions for omega.
        double w_minus_alpha = Math.acos(C / R);

        // We pick the solution that gives the "short path"
        // (i.e., the one where B is not on the other side of the focus from A)
        // This is a complex heuristic, so we'll pick one solution.
        // If it goes "the long way", we'd pick -acos(C/R).
        // For a game, one solution is usually sufficient.
        this.omega = alpha + w_minus_alpha;

        // 3. Solve for p (semi-latus rectum)
        this.p = rA * (1.0 + Math.cos(thetaA - omega));
    }

    @Override
    public List<Vector2d> getPathPoints(int segments) {
        List<Vector2d> points = new ArrayList<>(segments + 1);

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;

            // Interpolate the polar angle
            double theta = OrbitalMath.lerpAngle(thetaA, thetaB, t, true);

            // Calculate radius at this angle
            double r = p / (1.0 + Math.cos(theta - omega));

            if (r < 0) {
                // This can happen if the path is invalid (crosses focus)
                // Just stop drawing
                break;
            }

            // Convert polar back to cartesian
            double x = f1.x + r * Math.cos(theta);
            double y = f1.y + r * Math.sin(theta);

            points.add(new Vector2d(x, y));
        }
        // Ensure the last point is exactly B
        if (!points.isEmpty()) {
            points.set(points.size() - 1, new Vector2d(pointB));
        }
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
