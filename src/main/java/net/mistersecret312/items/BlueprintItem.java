package net.mistersecret312.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BlueprintItem extends Item
{
    public static final String ID = "blueprintId";

    public BlueprintItem(Properties pProperties)
    {
        super(pProperties);
    }

    public int getBlueprintID(ItemStack stack)
    {
        if(stack.getTag() != null && stack.getTag().contains(ID))
            return stack.getTag().getInt(ID);

        return -1;
    }

    public void setBlueprintID(ItemStack stack, int id)
    {
        stack.getOrCreateTag().putInt(ID, id);
    }
}
