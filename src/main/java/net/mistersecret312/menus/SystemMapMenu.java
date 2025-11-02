package net.mistersecret312.menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.init.MenuInit;

public class SystemMapMenu extends AbstractContainerMenu
{
    public SystemMapMenu(int containerID, Inventory inventory, FriendlyByteBuf buffer)
    {
        this(containerID);
    }

    public SystemMapMenu(int pContainerId)
    {
        super(MenuInit.SYSTEM_MAP.get(), pContainerId);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer)
    {
        return true;
    }
}
