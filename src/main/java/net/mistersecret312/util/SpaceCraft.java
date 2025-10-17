package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.util.rocket.Stage;

import java.util.LinkedHashSet;
import java.util.Optional;

public class SpaceCraft implements SpaceObject,Vessel
{
    public LinkedHashSet<Stage> stages;

    public Orbit orbit;
    private Level level;

    public SpaceCraft(LinkedHashSet<Stage> stages, Level level)
    {
        this.stages = stages;
        this.level = level;
    }

    public void tick(MinecraftServer server)
    {
        if(this.stages.isEmpty())
            discard(server);

        for(Stage stage : stages)
            stage.orbitalTick(server);

        Optional<ResourceKey<Level>> key = getOrbit().getParent().getDimension();
        if(key.isPresent() && level.getServer() != null
                && !key.get().equals(level.dimension()))
        {
            level = level.getServer().getLevel(key.get());
        }
    }

    public void discard(MinecraftServer server)
    {
        Orbits.get(server).removeOrbit(this.getOrbit());
    }

    @Override
    public Component getName()
    {
        return Component.translatable("spacecraft");
    }

    @Override
    public Orbit getOrbit()
    {
        return orbit;
    }

    @Override
    public Level getLevel()
    {
        return level;
    }

    @Override
    public LinkedHashSet<Stage> getStages()
    {
        return stages;
    }

    @Override
    public void setOrbit(Orbit orbit)
    {
        this.orbit = orbit;
    }

    @Override
    public CompoundTag save(Level level)
    {
        return null;
    }

    @Override
    public void load(Level level, CompoundTag compoundTag)
    {

    }

    @Override
    public void addStage(Stage stage)
    {
        this.stages.add(stage);
    }

    @Override
    public void removeStage(Stage stage)
    {
        this.stages.remove(stage);
    }
}
