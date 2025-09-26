package net.mistersecret312.util.rocket;

import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockDataType<T extends BlockData>
{
    public final Supplier<T> supplier;
    public final String id;

    public BlockDataType(Supplier<T> supplier, String id)
    {
        this.supplier = supplier;
        this.id = id;
    }

}
