package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.*;

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

    public void stage(Level level)
    {
        Stage oldDetach = null;
        for (Stage stage : this.stages)
        {
            oldDetach = stage;
            break;
        }

        if(oldDetach != null)
        {
            RocketEntity rocketEntityNew = new RocketEntity(level);
            Rocket rocketNew = new Rocket(rocketEntityNew, new LinkedHashSet<>());
            Stage stageNew = new Stage(rocketNew);

            stageNew.palette = oldDetach.palette;
            stageNew.blocks = oldDetach.blocks;

            rocketNew.stages.add(stageNew);
            this.stages.remove(oldDetach);
            rocketEntityNew.setRocket(rocketNew);

            double height = rocketEntityNew.makeBoundingBox().getYsize();

            BlockPos origin = null;
            Iterator<Stage> stages = this.stages.iterator();
            while(stages.hasNext())
            {
                Stage stage = stages.next();
                HashMap<BlockPos, BlockData> blocks = new HashMap<>();
                for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
                {
                    if(origin == null)
                        origin = entry.getKey();

                    BlockPos pos = entry.getKey().offset(0, (int) -height, 0);
                    BlockData data = entry.getValue();
                    data.pos = pos;
                    blocks.put(pos, data);
                }
                stage.blocks = blocks;
            }

            rocketEntityNew.setPos(this.getRocketEntity().position());
            this.getRocketEntity().setPos(this.getRocketEntity().position().add(0, height, 0));
            level.addFreshEntity(rocketEntityNew);
        }
    }

    public RocketEntity getRocketEntity()
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

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        ListTag stageTag = new ListTag();
        for(Stage stage : stages)
            stageTag.add(stage.save());
        tag.put("stages", stageTag);

        return tag;
    }

    public void load(CompoundTag tag)
    {
        ListTag stageTag = tag.getList("stages", Tag.TAG_COMPOUND);
        LinkedHashSet<Stage> stages = new LinkedHashSet<>();
        for(Tag listTag : stageTag)
        {
            Stage stage = new Stage(this);
            stage.load((CompoundTag) listTag);
            stages.add(stage);
        }
        this.stages = stages;
    }
}
