package net.mistersecret312.menus.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.mistersecret312.items.CombustionChamberItem;
import net.mistersecret312.items.TurboPumpItem;
import org.jetbrains.annotations.NotNull;

public class RocketComponentSlotHandler extends SlotItemHandler
{
    public ComponentType type;

    public RocketComponentSlotHandler(IItemHandler itemHandler, ComponentType type, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.type = type;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        if(stack.getItem() instanceof TurboPumpItem && this.type == ComponentType.TURBOPUMP)
            return true;
        return stack.getItem() instanceof CombustionChamberItem && this.type == ComponentType.COMBUSTION_CHAMBER;
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    public enum ComponentType
    {
        COMBUSTION_CHAMBER,
        TURBOPUMP;
    }
}
