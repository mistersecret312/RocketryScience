package net.mistersecret312.util;

import net.minecraft.world.level.Level;
import net.mistersecret312.util.rocket.Stage;

import java.util.LinkedHashSet;

public interface Vessel
{
    void addStage(Stage stage);
    void removeStage(Stage stage);
    LinkedHashSet<Stage> getStages();

    Orbit getOrbit();
    Level getLevel();
}
