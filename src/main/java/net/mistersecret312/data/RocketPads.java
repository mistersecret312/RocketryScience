package net.mistersecret312.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.util.infrastructure.RocketPad;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class RocketPads extends SavedData
{
    private static final String FILE_NAME = RocketryScienceMod.MODID + "-rocket_pads";

    private static final String SPACE_OBJECTS = "rocket_pads";

    public HashMap<String, RocketPad> rocketPads = new HashMap<>();

    private MinecraftServer server;

    //============================================================================================
    //*************************************Saving and Loading*************************************
    //============================================================================================

    private CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(SPACE_OBJECTS, serializeRocketPads());

        return tag;
    }

    private CompoundTag serializeRocketPads()
    {
        CompoundTag objectsTag = new CompoundTag();

        this.rocketPads.forEach((objectID, pad) -> objectsTag.put(objectID, pad.save()));

        return objectsTag;
    }

    private void deserialize(CompoundTag tag)
    {
        deserializeRocketPads(tag);
    }

    private void deserializeRocketPads(CompoundTag tag)
    {
        tag.getAllKeys().forEach(string -> {
            RocketPad pad = RocketPad.load(tag.getCompound(string));
            this.rocketPads.put(string, pad);
        });
    }

    //============================================================================================
    //********************************************Data********************************************
    //============================================================================================



    public RocketPads(MinecraftServer server)
    {
        this.server = server;
    }

    public static RocketPads create(MinecraftServer server)
    {
        return new RocketPads(server);
    }

    public static RocketPads load(MinecraftServer server, CompoundTag tag)
    {
        RocketPads data = create(server);

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
    public static RocketPads get(Level level)
    {
        if(level.isClientSide())
            throw new RuntimeException("Don't access this client-side!");

        return RocketPads.get(level.getServer());
    }

    @Nonnull
    public static RocketPads get(MinecraftServer server)
    {
        DimensionDataStorage storage = server.overworld().getDataStorage();

        return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
    }
}
