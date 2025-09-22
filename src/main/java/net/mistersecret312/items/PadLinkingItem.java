package net.mistersecret312.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.mistersecret312.block_entities.IRocketPadConnective;
import net.mistersecret312.block_entities.RocketPadBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PadLinkingItem extends Item
{

    public PadLinkingItem(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if(level.isClientSide() || player == null)
            return InteractionResult.PASS;

        if(level.getBlockEntity(pos) instanceof RocketPadBlockEntity pad)
        {
            this.setUUID(context.getItemInHand(), pad.uuid);
            player.displayClientMessage(Component.literal("UUID stored - " + this.getUUID(context.getItemInHand())), true);

        }
        if(level.getBlockEntity(pos) instanceof IRocketPadConnective connective)
        {
            connective.setPadUUID(this.getUUID(context.getItemInHand()));
            player.displayClientMessage(Component.literal("UUID set to - " + this.getUUID(context.getItemInHand())), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching())
            this.setUUID(stack, null);

        return InteractionResultHolder.pass(stack);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced)
    {
        super.appendHoverText(stack, pLevel, pTooltipComponents, pIsAdvanced);
        String id = this.getUUID(stack)  == null ? "" : this.getUUID(stack).toString();
        if(id != null)
            pTooltipComponents.add(Component.literal(id));
    }

    public UUID getUUID(ItemStack stack)
    {
        if(stack.getTag() != null && stack.getTag().contains("uuid"))
        {
            return stack.getTag().getUUID("uuid");
        }
        else return null;
    }

    public void setUUID(ItemStack stack, UUID uuid)
    {
        if(uuid == null)
            stack.getOrCreateTag().remove("uuid");
        else stack.getOrCreateTag().putUUID("uuid", uuid);
    }
}
