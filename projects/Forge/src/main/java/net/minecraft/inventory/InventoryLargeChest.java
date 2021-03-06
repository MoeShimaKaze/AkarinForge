package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class InventoryLargeChest implements ILockableContainer
{
    private final String name;
    private final ILockableContainer upperChest;
    private final ILockableContainer lowerChest;
    // CraftBukkit start - add fields and methods
    public java.util.List<org.bukkit.entity.HumanEntity> transaction = new java.util.ArrayList<org.bukkit.entity.HumanEntity>();

    public java.util.List<ItemStack> getContents() {
        java.util.List<ItemStack> result = new java.util.ArrayList<ItemStack>(this.getSizeInventory());
        for (int i = 0; i < this.getSizeInventory(); i++) {
            result.add(this.getStackInSlot(i));
        }
        return result;
    }
    public void onOpen(org.bukkit.craftbukkit.entity.CraftHumanEntity who) {
        this.upperChest.onOpen(who);
        this.lowerChest.onOpen(who);
        transaction.add(who);
    }
    public void onClose(org.bukkit.craftbukkit.entity.CraftHumanEntity who) {
        this.upperChest.onClose(who);
        this.lowerChest.onClose(who);
        transaction.remove(who);
    }
    public java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return transaction;
    }
    public org.bukkit.inventory.InventoryHolder getOwner() {
        return null; // This method won't be called since CraftInventoryDoubleChest doesn't defer to here
    }
    public void setMaxStackSize(int size) {
        this.upperChest.setMaxStackSize(size);
        this.lowerChest.setMaxStackSize(size);
    }
    @Override public org.bukkit.Location getLocation() {
        return upperChest.getLocation(); // TODO: right?
    } // CraftBukkit end

    public InventoryLargeChest(String nameIn, ILockableContainer upperChestIn, ILockableContainer lowerChestIn)
    {
        this.name = nameIn;

        if (upperChestIn == null)
        {
            upperChestIn = lowerChestIn;
        }

        if (lowerChestIn == null)
        {
            lowerChestIn = upperChestIn;
        }

        this.upperChest = upperChestIn;
        this.lowerChest = lowerChestIn;

        if (upperChestIn.isLocked())
        {
            lowerChestIn.setLockCode(upperChestIn.getLockCode());
        }
        else if (lowerChestIn.isLocked())
        {
            upperChestIn.setLockCode(lowerChestIn.getLockCode());
        }
    }

    public int getSizeInventory()
    {
        return this.upperChest.getSizeInventory() + this.lowerChest.getSizeInventory();
    }

    public boolean isEmpty()
    {
        return this.upperChest.isEmpty() && this.lowerChest.isEmpty();
    }

    public boolean isPartOfLargeChest(IInventory inventoryIn)
    {
        return this.upperChest == inventoryIn || this.lowerChest == inventoryIn;
    }

    public String getName()
    {
        if (this.upperChest.hasCustomName())
        {
            return this.upperChest.getName();
        }
        else
        {
            return this.lowerChest.hasCustomName() ? this.lowerChest.getName() : this.name;
        }
    }

    public boolean hasCustomName()
    {
        return this.upperChest.hasCustomName() || this.lowerChest.hasCustomName();
    }

    public ITextComponent getDisplayName()
    {
        return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
    }

    public ItemStack getStackInSlot(int index)
    {
        return index >= this.upperChest.getSizeInventory() ? this.lowerChest.getStackInSlot(index - this.upperChest.getSizeInventory()) : this.upperChest.getStackInSlot(index);
    }

    public ItemStack decrStackSize(int index, int count)
    {
        return index >= this.upperChest.getSizeInventory() ? this.lowerChest.decrStackSize(index - this.upperChest.getSizeInventory(), count) : this.upperChest.decrStackSize(index, count);
    }

    public ItemStack removeStackFromSlot(int index)
    {
        return index >= this.upperChest.getSizeInventory() ? this.lowerChest.removeStackFromSlot(index - this.upperChest.getSizeInventory()) : this.upperChest.removeStackFromSlot(index);
    }

    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index >= this.upperChest.getSizeInventory())
        {
            this.lowerChest.setInventorySlotContents(index - this.upperChest.getSizeInventory(), stack);
        }
        else
        {
            this.upperChest.setInventorySlotContents(index, stack);
        }
    }

    public int getInventoryStackLimit()
    {
        return Math.min(this.upperChest.getInventoryStackLimit(), this.lowerChest.getInventoryStackLimit()); // CraftBukkit - check both sides
    }

    public void markDirty()
    {
        this.upperChest.markDirty();
        this.lowerChest.markDirty();
    }

    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return this.upperChest.isUsableByPlayer(player) && this.lowerChest.isUsableByPlayer(player);
    }

    public void openInventory(EntityPlayer player)
    {
        this.upperChest.openInventory(player);
        this.lowerChest.openInventory(player);
    }

    public void closeInventory(EntityPlayer player)
    {
        this.upperChest.closeInventory(player);
        this.lowerChest.closeInventory(player);
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    public boolean isLocked()
    {
        return this.upperChest.isLocked() || this.lowerChest.isLocked();
    }

    public void setLockCode(LockCode code)
    {
        this.upperChest.setLockCode(code);
        this.lowerChest.setLockCode(code);
    }

    public LockCode getLockCode()
    {
        return this.upperChest.getLockCode();
    }

    public String getGuiID()
    {
        return this.upperChest.getGuiID();
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    public void clear()
    {
        this.upperChest.clear();
        this.lowerChest.clear();
    }
}