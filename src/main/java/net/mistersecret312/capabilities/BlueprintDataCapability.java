package net.mistersecret312.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.mistersecret312.blueprint.Blueprint;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;

import java.util.ArrayList;
import java.util.List;

public class BlueprintDataCapability implements INBTSerializable<CompoundTag>
{
    public List<RocketEngineBlueprint> rocketEngineBlueprints = new ArrayList<>();

    public BlueprintDataCapability()
    {
        this.rocketEngineBlueprints.add(new RocketEngineBlueprint(RocketMaterial.STAINLESS_STEEL, RocketMaterial.STAINLESS_STEEL, RocketFuel.HYDROLOX));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        ListTag blueprintList = new ListTag();
        this.rocketEngineBlueprints.forEach(blueprint -> {
            blueprintList.add(blueprint.serializeNBT());
        });
        tag.put("blueprints", blueprintList);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        ListTag blueprintsTag = tag.getList("blueprints", ListTag.TAG_COMPOUND);
        List<RocketEngineBlueprint> rocketEngineBlueprints = new ArrayList<>();

        blueprintsTag.forEach(blueprintTag -> {
            RocketEngineBlueprint blueprint = new RocketEngineBlueprint();
            blueprint.deserializeNBT(((CompoundTag) blueprintTag));
            rocketEngineBlueprints.add(blueprint);
        });


        this.rocketEngineBlueprints = rocketEngineBlueprints;
    }
}
