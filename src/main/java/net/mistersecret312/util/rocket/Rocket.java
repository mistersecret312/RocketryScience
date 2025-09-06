package net.mistersecret312.util.rocket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.LinkedHashSet;

public class Rocket
{
    public LinkedHashSet<Stage> stages;
    public RocketEntity rocket;

    public Rocket(RocketEntity rocket, LinkedHashSet<Stage> stages)
    {
        this.stages = stages;
        this.rocket = rocket;
    }

    public void tick(Level level)
    {
        for(Stage stage : stages)
            stage.tick(level);
    }

    public RocketEntity getRocket()
    {
        return rocket;
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.rocket.getId());
        buffer.writeCollection(stages, (writer, stage) -> stage.toNetwork(writer));
    }

    public static Rocket fromNetwork(FriendlyByteBuf buffer)
    {
        RocketEntity rocketEntity = ClientPacketHandler.getEntity(buffer.readInt());
        Rocket rocket = new Rocket(rocketEntity, new LinkedHashSet<>());
        LinkedHashSet<Stage> stages = buffer.readCollection(LinkedHashSet::new, reader -> Stage.fromNetwork(buffer, rocket));

        rocket.stages = stages;
        return rocket;
    }
}
