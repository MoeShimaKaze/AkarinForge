package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorDefaultDispenseItem implements IBehaviorDispenseItem
{
    public final ItemStack dispense(IBlockSource source, ItemStack stack)
    {
        ItemStack itemstack = this.dispenseStack(source, stack);
        this.playDispenseSound(source);
        this.spawnDispenseParticles(source, (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
        return itemstack;
    }

    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
    {
        EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
        IPosition iposition = BlockDispenser.getDispensePosition(source);
        ItemStack itemstack = stack.splitStack(1);
        if (!doDispense(source.getWorld(), itemstack, 6, enumfacing, source)) itemstack.grow(1); // CraftBukkit
        return stack;
    }

    public static boolean doDispense(World worldIn, ItemStack stack, int speed, EnumFacing facing, IBlockSource source) // CraftBukkit - void -> boolean return, IPosition -> ISourceBlock last argument
    {
        IPosition position = BlockDispenser.getDispensePosition(source); // CraftBukkit
        double d0 = position.getX();
        double d1 = position.getY();
        double d2 = position.getZ();

        if (facing.getAxis() == EnumFacing.Axis.Y)
        {
            d1 = d1 - 0.125D;
        }
        else
        {
            d1 = d1 - 0.15625D;
        }

        EntityItem entityitem = new EntityItem(worldIn, d0, d1, d2, stack);
        double d3 = worldIn.rand.nextDouble() * 0.1D + 0.2D;
        entityitem.motionX = (double)facing.getFrontOffsetX() * d3;
        entityitem.motionY = 0.20000000298023224D;
        entityitem.motionZ = (double)facing.getFrontOffsetZ() * d3;
        entityitem.motionX += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        entityitem.motionY += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        entityitem.motionZ += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        // CraftBukkit start
        org.bukkit.block.Block block = worldIn.getWorld().getBlockAt(source.getBlockPos().getX(), source.getBlockPos().getY(), source.getBlockPos().getZ());
        org.bukkit.craftbukkit.inventory.CraftItemStack craftItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(stack);
        org.bukkit.event.block.BlockDispenseEvent event = new org.bukkit.event.block.BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(entityitem.motionX, entityitem.motionY, entityitem.motionZ));
        if (!BlockDispenser.eventFired) worldIn.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        entityitem.setItem(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(event.getItem()));
        entityitem.motionX = event.getVelocity().getX();
        entityitem.motionY = event.getVelocity().getY();
        entityitem.motionZ = event.getVelocity().getZ();
        if (!event.getItem().getType().equals(craftItem.getType())) {
            ItemStack eventStack = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(event.getItem()); // Chain to handler for new item
            IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem) BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
            if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior.getClass() != BehaviorDefaultDispenseItem.class) {
                idispensebehavior.dispense(source, eventStack);
            } else {
                worldIn.spawnEntity(entityitem);
            }
            return false;
        }
        // CraftBukkit end
        worldIn.spawnEntity(entityitem);
        return true; // CraftBukkit
    }

    protected void playDispenseSound(IBlockSource source)
    {
        source.getWorld().playEvent(1000, source.getBlockPos(), 0);
    }

    protected void spawnDispenseParticles(IBlockSource source, EnumFacing facingIn)
    {
        source.getWorld().playEvent(2000, source.getBlockPos(), this.getWorldEventDataFrom(facingIn));
    }

    private int getWorldEventDataFrom(EnumFacing facingIn)
    {
        return facingIn.getFrontOffsetX() + 1 + (facingIn.getFrontOffsetZ() + 1) * 3;
    }
}