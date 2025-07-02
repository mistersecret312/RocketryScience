package net.mistersecret312.mishaps;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.mistersecret312.block_entities.LiquidRocketEngineBlockEntity;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.block_entities.SolidRocketBoosterBlockEntity;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.init.MishapInit;

public class FuelLeakMishap extends Mishap<RocketEngineBlockEntity, RocketEngineBlueprint>
{
    public int drainAmount;
    public FuelLeakMishap(RocketEngineBlockEntity blockEntity, RocketEngineBlueprint blueprint, int drainAmount)
    {
        super(MishapInit.FUEL_LEAK.get(), blockEntity, blueprint);
        this.drainAmount = drainAmount;
    }

    @Override
    public void tickToPhysical(RocketEngineBlockEntity rocketEngine)
    {
        if(rocketEngine instanceof LiquidRocketEngineBlockEntity liquidEngine)
            liquidEngine.fuelTank.drain(this.drainAmount, IFluidHandler.FluidAction.EXECUTE);
        rocketEngine.integrity = RocketEngineBlockEntity.trimDouble(rocketEngine.integrity-0.02);
    }

    @Override
    public void applyToPhysical(RocketEngineBlockEntity rocketEngine)
    {
        rocketEngine.reliability = RocketEngineBlockEntity.trimDouble(rocketEngine.reliability-type.physicalEffect);
    }

    @Override
    public void removeFromPhysical(RocketEngineBlockEntity rocketEngine)
    {
        rocketEngine.reliability = RocketEngineBlockEntity.trimDouble(rocketEngine.reliability+type.physicalEffect);
    }

    @Override
    public void applyToBlueprint(RocketEngineBlueprint blueprint)
    {
        blueprint.setReliability(blueprint.getReliability()+type.blueprintEffect);
    }

    @Override
    public void removeFromBlueprint(RocketEngineBlueprint blueprint)
    {
        blueprint.setReliability(blueprint.getReliability()-type.blueprintEffect);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        CompoundTag tag = super.writeToNBT();
        tag.putInt("drainAmount", this.drainAmount);
        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag)
    {
        super.loadFromNBT(tag);
        this.drainAmount = tag.getInt("drainAmount");
    }
}
