package net.mistersecret312.data_serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.mistersecret312.util.rocket.Rocket;

public class RocketDataSerializer implements EntityDataSerializer<Rocket>
{
    public void write(FriendlyByteBuf buf, Rocket rocket)
    {
        rocket.toNetwork(buf);
    }

    public Rocket read(FriendlyByteBuf buf)
    {
        return Rocket.fromNetwork(buf);
    }

    @Override
    public Rocket copy(Rocket rocket)
    {
        return new Rocket(rocket.rocket, rocket.stages);
    }
}
