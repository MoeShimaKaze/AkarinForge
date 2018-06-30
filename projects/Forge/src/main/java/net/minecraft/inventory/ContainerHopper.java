package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerHopper extends Container
{
    private final IInventory hopperInventory;
    // CraftBukkit start
    private org.bukkit.craftbukkit.inventory.CraftInventoryView bukkitEntity = null;
    private InventoryPlayer inventoryPlayer;

    @Override public org.bukkit.craftbukkit.inventory.CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;
        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventory(this.hopperInventory);
        bukkitEntity = new org.bukkit.craftbukkit.inventory.CraftInventoryView((org.bukkit.entity.HumanEntity) this.inventoryPlayer.player.getBukkitEntity(), inventory, this); // Akarin Forge - FIXME
        return bukkitEntity;
    } // CraftBukkit end

    public ContainerHopper(InventoryPlayer playerInventory, IInventory hopperInventoryIn, EntityPlayer player)
    {
        this.hopperInventory = hopperInventoryIn;
        hopperInventoryIn.openInventory(player);
        int i = 51;

        for (int j = 0; j < hopperInventoryIn.getSizeInventory(); ++j)
        {
            this.addSlotToContainer(new Slot(hopperInventoryIn, j, 44 + j * 18, 20));
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 109));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.hopperInventory.isUsableByPlayer(playerIn);
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < this.hopperInventory.getSizeInventory())
            {
                if (!this.mergeItemStack(itemstack1, this.hopperInventory.getSizeInventory(), this.inventorySlots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, this.hopperInventory.getSizeInventory(), false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.hopperInventory.closeInventory(playerIn);
    }
}