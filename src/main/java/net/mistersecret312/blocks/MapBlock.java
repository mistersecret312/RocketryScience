package net.mistersecret312.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.mistersecret312.menus.SystemMapMenu;

public class MapBlock extends Block
{

    public MapBlock(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit)
    {
        if(!level.isClientSide())
        {

            MenuProvider containerProvider = new MenuProvider()
            {
                @Override
                public Component getDisplayName()
                {
                    return Component.translatable("screen.rocketry_science.system_map");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
                {
                    return new SystemMapMenu(windowId);
                }
            };
            NetworkHooks.openScreen((ServerPlayer) player, containerProvider, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}
