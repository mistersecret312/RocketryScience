package net.mistersecret312.mishaps;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.block_entities.BlueprintBlockEntity;
import net.mistersecret312.blueprint.Blueprint;

import java.util.function.BiFunction;

public class MishapType<T extends Mishap<U, B>, U extends BlueprintBlockEntity, B extends Blueprint>
{
    public final MishapTarget target;
    public final MishapCategory category;
    public final BiFunction<MishapType<T, U, B>, U, Mishap<U, B>> supplierBlockEntity;
    public final BiFunction<MishapType<T, U, B>, B, Mishap<U, B>> supplierBlueprint;
    public final float chance;
    public final double blueprintEffect;
    public final double physicalEffect;
    public MishapType(MishapTarget target, MishapCategory category,
                      BiFunction<MishapType<T, U, B>, U, Mishap<U, B>> supplierBlockEntity,
                      BiFunction<MishapType<T, U, B>, B, Mishap<U, B>> supplierBlueprint,
                      float chance, double blueprint, double physical)
    {
        this.target = target;
        this.category = category;
        this.supplierBlockEntity = supplierBlockEntity;
        this.supplierBlueprint = supplierBlueprint;
        this.chance = chance;
        this.blueprintEffect = blueprint;
        this.physicalEffect = physical;
    }

    public Mishap<U, B> create(U blockEntity, B blueprint)
    {
        if(blockEntity == null)
            return this.supplierBlueprint.apply(this, blueprint);
        if(blueprint == null)
            return this.supplierBlockEntity.apply(this, blockEntity);

        return null;
    }

    public MishapTarget getTarget()
    {
        return target;
    }

    public float getChance()
    {
        return chance;
    }

    public double getBlueprintEffect()
    {
        return blueprintEffect;
    }

    public double getPhysicalEffect()
    {
        return physicalEffect;
    }

    public enum MishapTarget implements StringRepresentable
    {
        ROCKET_ENGINE("rocket_engine"),
        FUEL_TANK("fuel_tank");

        String name;
        MishapTarget(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }
    }

    public enum MishapCategory implements StringRepresentable
    {
        MINOR("minor"),
        MAJOR("major"),
        CATASTROPHIC("catastrophic");

        String name;
        MishapCategory(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }
    }
}
