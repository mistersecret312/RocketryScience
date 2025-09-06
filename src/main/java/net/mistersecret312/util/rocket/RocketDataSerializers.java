package net.mistersecret312.util.rocket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public class RocketDataSerializers
{
    public static final EntityDataSerializer<Rocket> ROCKET = new EntityDataSerializer<>() {

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

    };

    static
    {
        EntityDataSerializers.registerSerializer(ROCKET);
    }
}
