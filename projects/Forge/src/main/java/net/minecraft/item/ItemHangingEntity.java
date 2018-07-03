package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemHangingEntity extends Item
{
    private final Class <? extends EntityHanging > hangingEntityClass;

    public ItemHangingEntity(Class <? extends EntityHanging > entityClass)
    {
        this.hangingEntityClass = entityClass;
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        BlockPos blockpos = pos.offset(facing);

        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && player.canPlayerEdit(blockpos, facing, itemstack))
        {
            EntityHanging entityhanging = this.createEntity(worldIn, blockpos, facing);

            if (entityhanging != null && entityhanging.onValidSurface())
            {
                if (!worldIn.isRemote)
                {
                    // CraftBukkit start - fire HangingPlaceEvent
                    org.bukkit.entity.Player who = (player == null) ? null : (org.bukkit.entity.Player) player.getBukkitEntity();
                    org.bukkit.block.Block blockClicked = worldIn.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
                    org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(facing);
                    org.bukkit.event.hanging.HangingPlaceEvent event = new org.bukkit.event.hanging.HangingPlaceEvent((org.bukkit.entity.Hanging) entityhanging.getBukkitEntity(), who, blockClicked, blockFace);
                    worldIn.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) return EnumActionResult.FAIL;
                    // CraftBukkit end
                    entityhanging.playPlaceSound();
                    worldIn.spawnEntity(entityhanging);
                }

                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }

    @Nullable
    private EntityHanging createEntity(World worldIn, BlockPos pos, EnumFacing clickedSide)
    {
        if (this.hangingEntityClass == EntityPainting.class)
        {
            return new EntityPainting(worldIn, pos, clickedSide);
        }
        else
        {
            return this.hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(worldIn, pos, clickedSide) : null;
        }
    }
}