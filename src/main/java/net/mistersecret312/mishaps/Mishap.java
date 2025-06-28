package net.mistersecret312.mishaps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.block_entities.BlueprintBlockEntity;
import net.mistersecret312.blueprint.Blueprint;
import net.mistersecret312.init.MishapInit;
import org.jetbrains.annotations.Nullable;

public class Mishap<T extends BlueprintBlockEntity, B extends Blueprint>
{
    public final MishapType<?, T, B> type;
    public T blockEntity;
    public Mishap(MishapType<?, T, B> type, T blockEntity, B blueprint)
    {
        this.type = type;
    }

    public void tickToPhysical(T blockEntity)
    {

    }

    public void applyToPhysical(T blockEntity)
    {

    }

    public void removeFromPhysical(T blockEntity)
    {

    }

    public void applyToBlueprint(B blueprint)
    {

    }

    public void removeFromBlueprint(B blueprint)
    {

    }

    public CompoundTag writeToNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", String.valueOf(MishapInit.REGISTRY.get().getKey(this.getType())));

        return tag;
    }

    public void loadFromNBT(CompoundTag tag)
    {

    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <B extends BlueprintBlockEntity> Mishap<B, ?> loadBlockEntityStatic(CompoundTag tag, B blockEntity)
    {
        String s = tag.getString("id");
        ResourceLocation key = ResourceLocation.tryParse(s);
        if(key != null)
        {
            MishapType<?, B, ?> type = (MishapType<?, B, ?>) MishapInit.REGISTRY.get().getValue(key);
            if(type != null)
                return type.create(blockEntity, null);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <B extends Blueprint> Mishap<?, B> loadBlueprintStatic(CompoundTag tag, B blueprint)
    {
        String s = tag.getString("id");
        ResourceLocation key = ResourceLocation.tryParse(s);
        if(key != null)
        {
            MishapType<?, ?, B> type = (MishapType<?, ?, B>) MishapInit.REGISTRY.get().getValue(key);
            if(type != null)
                return type.create(null, blueprint);
        }
        return null;
    }

    public MishapType<?, ?, ?> getType()
    {
        return type;
    }

    public T getBlockEntity()
    {
        return blockEntity;
    }
}
