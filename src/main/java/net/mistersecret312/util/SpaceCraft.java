package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.util.rocket.Stage;

import java.util.LinkedHashSet;
import java.util.Optional;

public class SpaceCraft implements SpaceObject, Vessel
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
        CompoundTag tag = new CompoundTag();

        ListTag stageTag = new ListTag();
        for(Stage stage : stages)
            stageTag.add(stage.save());
        tag.put("stages", stageTag);

        return tag;
    }

    @Override
    public void load(Level level, CompoundTag tag)
    {
        ListTag stageTag = tag.getList("stages", Tag.TAG_COMPOUND);
        LinkedHashSet<Stage> stages = new LinkedHashSet<>();
        for(Tag listTag : stageTag)
        {
            Stage stage = new Stage(this);
            stage.load((CompoundTag) listTag, level.getServer());
            stages.add(stage);
        }
        this.stages = stages;
    }

    @Override
    public void tick()
    {
        tick(level.getServer());
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
