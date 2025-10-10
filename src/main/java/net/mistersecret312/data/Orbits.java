package net.mistersecret312.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketPadBlockEntity;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.SpaceObject;
import net.mistersecret312.util.infrastructure.RocketPad;
import net.mistersecret312.util.rocket.Rocket;

import javax.annotation.Nonnull;
import java.util.*;

public class Orbits extends SavedData
{
    private static final String FILE_NAME = RocketryScienceMod.MODID + "-orbits";

    private static final String ORBITS = "orbits";

    public List<Orbit> orbits = new ArrayList<>();

    private MinecraftServer server;

    //============================================================================================
    //*************************************Saving and Loading*************************************
    //============================================================================================

    private CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(ORBITS, serializeOrbits());

        return tag;
    }

    private ListTag serializeOrbits()
    {
        ListTag objectsTag = new ListTag();
        this.orbits.forEach(orbit ->
        {
            objectsTag.add(orbit.save(server.overworld()));
        });

        return objectsTag;
    }

    private void deserialize(CompoundTag tag)
    {
        deserializeOrbits(tag.getList(ORBITS, ListTag.TAG_COMPOUND));
    }

    private void deserializeOrbits(ListTag tag)
    {
        for(Tag taG : tag)
        {
            CompoundTag compound = ((CompoundTag) taG);
            this.orbits.add(Orbit.load(server.overworld(), compound));
        }
    }

    public void addOrbit(double altitude, CelestialBody parentBody, SpaceObject spaceObject)
    {
        Orbit orbit = new Orbit(parentBody, altitude);
        orbit.spaceObject = spaceObject;
        this.orbits.add(orbit);
        this.setDirty();
    }

    @Override
    public void setDirty()
    {
        super.setDirty();
    }

    public Orbits(MinecraftServer server)
    {
        this.server = server;
    }

    public static Orbits create(MinecraftServer server)
    {
        return new Orbits(server);
    }

    public static Orbits load(MinecraftServer server, CompoundTag tag)
    {
        Orbits data = create(server);

        data.server = server;
        data.deserialize(tag);

        return data;
    }

    public CompoundTag save(CompoundTag tag)
    {
        tag = serialize();

        return tag;
    }

    @Nonnull
    public static Orbits get(Level level)
    {
        if(level.isClientSide())
            throw new RuntimeException("Don't access this client-side!");

        return Orbits.get(level.getServer());
    }

    @Nonnull
    public static Orbits get(MinecraftServer server)
    {
        DimensionDataStorage storage = server.overworld().getDataStorage();

        return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
    }
}
