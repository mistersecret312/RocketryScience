package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.data.ClientOrbits;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.network.ClientPacketHandler;
import net.mistersecret312.util.Orbit;
import net.mistersecret312.util.SpaceCraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientOrbitsUpdatePacket
{
    public List<Orbit> orbits;
    public List<ClientOrbits.ClientOrbit> clientOrbits;

    public ClientOrbitsUpdatePacket(List<Orbit> orbits, List<ClientOrbits.ClientOrbit> clientOrbits)
    {
        this.orbits = orbits;
        this.clientOrbits = clientOrbits;
    }

    public static void write(ClientOrbitsUpdatePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeCollection(packet.orbits, (writer, orbit) ->
            {
                writer.writeDouble(orbit.epoch);
                writer.writeDouble(orbit.orbitalAltitude);
                writer.writeDouble(orbit.orbitalPeriod);

                writer.writeBoolean(orbit.spaceObject instanceof SpaceCraft);

                writer.writeResourceKey(orbit.parentKey);
            });
    }

    public static ClientOrbitsUpdatePacket read(FriendlyByteBuf buffer)
    {
        List<ClientOrbits.ClientOrbit> clientOrbitList = buffer.readCollection(ArrayList::new,
                (reader) ->
                    {
                        double epoch = reader.readDouble();
                        double altitude = reader.readDouble();
                        double period = reader.readDouble();

                        boolean isArtifical = reader.readBoolean();

                        ResourceKey<CelestialBody> parent = reader.readResourceKey(CelestialBody.REGISTRY_KEY);

                        return new ClientOrbits.ClientOrbit(epoch, altitude, period, parent, isArtifical);
                    });

        return new ClientOrbitsUpdatePacket(new ArrayList<>(), clientOrbitList);
    }

    public static void handle(ClientOrbitsUpdatePacket packet, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
            {
                ClientOrbits.ORBITS = packet.clientOrbits;
            });
        context.get().setPacketHandled(true);
    }
}
