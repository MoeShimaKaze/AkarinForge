package net.minecraft.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemChorusFruit extends ItemFood
{
    public ItemChorusFruit(int amount, float saturation)
    {
        super(amount, saturation, false);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        ItemStack itemstack = super.onItemUseFinish(stack, worldIn, entityLiving);

        if (!worldIn.isRemote)
        {
            double d0 = entityLiving.posX;
            double d1 = entityLiving.posY;
            double d2 = entityLiving.posZ;

            for (int i = 0; i < 16; ++i)
            {
                double d3 = entityLiving.posX + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                double d4 = MathHelper.clamp(entityLiving.posY + (double)(entityLiving.getRNG().nextInt(16) - 8), 0.0D, (double)(worldIn.getActualHeight() - 1));
                double d5 = entityLiving.posZ + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                // CraftBukkit start
                if (entityLiving instanceof net.minecraft.entity.player.EntityPlayerMP) {
                    org.bukkit.entity.Player player = ((net.minecraft.entity.player.EntityPlayerMP) entityLiving).getBukkitEntity();
                    org.bukkit.event.player.PlayerTeleportEvent teleEvent = new org.bukkit.event.player.PlayerTeleportEvent(player, player.getLocation(), new org.bukkit.Location(player.getWorld(), d3, d4, d5), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
                    worldIn.getServer().getPluginManager().callEvent(teleEvent);
                    if (teleEvent.isCancelled()) break;
                    d3 = teleEvent.getTo().getX();
                    d4 = teleEvent.getTo().getY();
                    d5 = teleEvent.getTo().getZ();
                } // CraftBukkit end

                if (entityLiving.isRiding())
                {
                    entityLiving.dismountRidingEntity();
                }

                if (entityLiving.attemptTeleport(d3, d4, d5))
                {
                    worldIn.playSound((EntityPlayer)null, d0, d1, d2, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }

            if (entityLiving instanceof EntityPlayer)
            {
                ((EntityPlayer)entityLiving).getCooldownTracker().setCooldown(this, 20);
            }
        }

        return itemstack;
    }
}