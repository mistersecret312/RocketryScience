package net.mistersecret312.util.trajectories;

import org.joml.Vector2d;

import java.util.List;

public interface OrbitalPath
{
    List<Vector2d> getPathPoints(int numPoints);
    boolean isRetrograde();
}
