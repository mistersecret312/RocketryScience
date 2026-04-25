package net.mistersecret312.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.init.NetworkInit;
import net.mistersecret312.network.packets.ClientOrbitsUpdatePacket;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.OrphanObject;
import net.mistersecret312.util.SpaceObject;

import javax.annotation.Nonnull;
import java.util.*;

public class Orbits extends SavedData
{
    private static final String FILE_NAME = RocketryScienceMod.MODID + "-orbits";

    private static final String ORBITS = "orbits";

    public List<Orbit> orbits = new ArrayList<>();
    public List<OrphanObject> orphans = new ArrayList<>();

    private MinecraftServer server;

    private CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(ORBITS, serializeOrbits());
        tag.put("orphans", serializeOrphans());

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

    private ListTag serializeOrphans()
    {
        ListTag listTag = new ListTag();
        this.orphans.forEach(orphan ->
            {
                listTag.add(orphan.save(server.overworld()));
            });
        return listTag;
    }

    private void deserialize(CompoundTag tag)
    {
        deserializeOrbits(tag.getList(ORBITS, ListTag.TAG_COMPOUND));
        deserializeOrphans(tag.getList("orphans", ListTag.TAG_COMPOUND));
    }

    private void deserializeOrbits(ListTag tag)
    {
        for(Tag taG : tag)
        {
            CompoundTag compound = ((CompoundTag) taG);
            this.orbits.add(Orbit.load(server.overworld(), compound));
        }
    }

    private void deserializeOrphans(ListTag list)
    {
        for(Tag tag : list)
        {
            CompoundTag compoundTag = ((CompoundTag) tag);
            this.orphans.add(OrphanObject.load(server.overworld(), compoundTag));
        }
    }

    public void addOrbit(CelestialBody parentBody, double altitude, double epoch, SpaceObject spaceObject)
    {
        Orbit orbit = new Orbit(parentBody, altitude, epoch);
        orbit.spaceObject = spaceObject;
        spaceObject.setOrbit(orbit);
        orbit.tick(server.overworld());
        this.orbits.add(orbit);
        this.setDirty();
    }

    public void addOrphan(CelestialBody body)
    {
        this.orphans.add(new OrphanObject(body));
        this.setDirty();
    }

    public void removeOrbit(Orbit orbit)
    {
        this.orbits.remove(orbit);
        this.setDirty();
    }

    public void markOrbitForRemoval(Orbit orbit)
    {
        orbit.shouldRemove = true;
        this.setDirty();
    }

    public void removeOrphan(CelestialBody body)
    {
        this.orphans.remove(body);
        this.setDirty();
    }

    @Override
    public void setDirty()
    {
        super.setDirty();
        NetworkInit.INSTANCE.send(PacketDistributor.ALL.with(() -> null), new ClientOrbitsUpdatePacket(this.orbits, new ArrayList<>()));
    }

    public List<Orbit> getOrbits()
    {
        this.orbits.removeIf(orbit -> orbit.shouldRemove);
        return orbits;
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
