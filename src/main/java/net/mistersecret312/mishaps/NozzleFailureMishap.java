package net.mistersecret312.mishaps;

import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blueprint.RocketEngineBlueprint;

public class NozzleFailureMishap extends Mishap<RocketEngineBlockEntity, RocketEngineBlueprint>
{

    public NozzleFailureMishap(MishapType<?, RocketEngineBlockEntity, RocketEngineBlueprint> type,
                               RocketEngineBlockEntity blockEntity, RocketEngineBlueprint blueprint)
    {
        super(type, blockEntity, blueprint);
    }

    @Override
    public void applyToPhysical(RocketEngineBlockEntity rocketEngine)
    {
        rocketEngine.getLevel().destroyBlock(rocketEngine.getNozzlePos(), false);
    }

    @Override
    public void applyToBlueprint(RocketEngineBlueprint blueprint)
    {
        blueprint.integrity += 1000;
        blueprint.reliability += type.blueprintEffect;
    }

    @Override
    public void removeFromBlueprint(RocketEngineBlueprint blueprint)
    {
        blueprint.integrity -= 1000;
        blueprint.reliability -= type.blueprintEffect;
    }
}
