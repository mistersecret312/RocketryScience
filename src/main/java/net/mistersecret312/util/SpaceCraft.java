package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.data.Orbits;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.util.rocket.Rocket;
import net.mistersecret312.util.rocket.Stage;
import org.joml.Vector2d;

import java.util.LinkedHashSet;
import java.util.Optional;

public class SpaceCraft implements SpaceObject, Vessel
{
    public LinkedHashSet<Stage> stages;

    public Orbit orbit;
    public TransferData transferData = null;
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

        if(true)
        {
            if(this.transferData == null)
            {
                TransferData data = new TransferData(
                        getOrbit().getParentKey().location(), getOrbit().getOrbitalAltitude(),
                        getOrbit().getAngle(level.getGameTime()), level.getGameTime(),

                        getOrbit().getParentKey().location(), getOrbit().getOrbitalAltitude()+(1000*1000),
                        getOrbit().getAngle(level.getGameTime()+10000), level.getGameTime()+10000,

                        getOrbit().getParentKey().location(), 100d, 0d, TransferData.TransferType.LOCAL_ORBIT);

                this.transferData = data;
            }
        }

        if(false)
        {
            land(server, new Vec3(0.5, 500, 0.5));
        }
    }

    public void land(MinecraftServer server, Vec3 pos)
    {
        RocketEntity rocketEntity = new RocketEntity(getLevel());
        Rocket rocket = new Rocket(rocketEntity, stages);

        LinkedHashSet<Stage> rocketStages = new LinkedHashSet<>();
        for(Stage stage : stages)
        {
            Stage rocketStage = new Stage(rocket);
            rocketStage.load(stage.save(), server);
            rocketStages.add(rocketStage);
        }
        rocket.stages = rocketStages;
        rocket.canLand = false;

        rocketEntity.setRocket(rocket);

        rocketEntity.setPos(pos);
        getLevel().addFreshEntity(rocketEntity);

        System.out.println("Landing the spacecraft!");
        discard(server);
    }

    public void discard(MinecraftServer server)
    {
        Orbits.get(server).markOrbitForRemoval(getOrbit());
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
    public boolean isInSpace()
    {
        return true;
    }

    public TransferData getTransferData()
    {
        return transferData;
    }

    public void setTransferData(TransferData transferData)
    {
        this.transferData = transferData;
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

    public Vector2d getCoordinates(double altitude, double angle)
    {
        double radians = Math.toRadians(angle);

        double x = altitude * Math.cos(radians);
        double y = altitude * Math.sin(radians);

        return new Vector2d(x, y);
    }

    @Override
    public CompoundTag save(Level level)
    {
        CompoundTag tag = new CompoundTag();

        ListTag stageTag = new ListTag();
        for(Stage stage : stages)
            stageTag.add(stage.save());
        tag.put("stages", stageTag);

        if(this.transferData != null)
            tag.put("transfer", this.transferData.save(new CompoundTag()));

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

        if(tag.contains("transfer"))
            this.transferData = TransferData.load(tag.getCompound("transfer"));
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
