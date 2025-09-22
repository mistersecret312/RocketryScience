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
import net.mistersecret312.util.infrastructure.RocketPad;
import net.povstalec.sgjourney.common.data.StargateNetwork;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RocketPads extends SavedData
{
    private static final String FILE_NAME = RocketryScienceMod.MODID + "-rocket_pads";

    private static final String ROCKET_PADS = "rocket_pads";

    public HashMap<UUID, RocketPad> rocketPads = new HashMap<>();

    private MinecraftServer server;

    //============================================================================================
    //*************************************Saving and Loading*************************************
    //============================================================================================

    private CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(ROCKET_PADS, serializeRocketPads());

        return tag;
    }

    private CompoundTag serializeRocketPads()
    {
        CompoundTag objectsTag = new CompoundTag();

        this.rocketPads.forEach((uuid, pad) ->
        {
            objectsTag.put(uuid.toString(), pad.save());
        });

        return objectsTag;
    }

    private void deserialize(CompoundTag tag)
    {
        deserializeRocketPads(tag.getCompound(ROCKET_PADS));
    }

    private void deserializeRocketPads(CompoundTag tag)
    {
        for(String key : tag.getAllKeys())
        {
            this.rocketPads.put(UUID.fromString(key),
                    RocketPad.load(tag.getCompound(key)));
        }
    }

    public void addRocketPad(UUID uuid, BlockPos pos, ResourceKey<Level> dimension)
    {
        this.rocketPads.put(uuid, new RocketPad(pos, dimension));

        this.setDirty();
    }

    @Override
    public void setDirty()
    {
        super.setDirty();
        Iterator<Map.Entry<UUID, RocketPad>> iterator = this.rocketPads.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<UUID, RocketPad> entry = iterator.next();
            ServerLevel level = server.getLevel(entry.getValue().getDimension());
            if(level == null)
                continue;
            if(level.getBlockEntity(entry.getValue().getPos()) instanceof RocketPadBlockEntity pad)
            {
                if(!pad.uuid.equals(entry.getKey()))
                    iterator.remove();
            }
            else iterator.remove();
        }
    }

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
