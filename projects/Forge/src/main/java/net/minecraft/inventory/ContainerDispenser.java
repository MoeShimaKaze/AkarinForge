package net.minecraft.inventory;

import org.bukkit.entity.HumanEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerDispenser extends Container
{
    private final IInventory dispenserInventory;
    // CraftBukkit start
    private org.bukkit.craftbukkit.inventory.CraftInventoryView bukkitEntity = null;
    private net.minecraft.entity.player.InventoryPlayer inventoryPlayer;
    // CraftBukkit end

    public ContainerDispenser(IInventory playerInventory, IInventory dispenserInventoryIn)
    {
        this.dispenserInventory = dispenserInventoryIn;
        // CraftBukkit start - Save player
        // TODO: Should we check to make sure it really is an InventoryPlayer?
        this.inventoryPlayer = (net.minecraft.entity.player.InventoryPlayer) playerInventory;
        // CraftBukkit end

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(dispenserInventoryIn, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 142));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        if (!this.checkReachable) return true; // CraftBukkit
        return this.dispenserInventory.isUsableByPlayer(playerIn);
    }
    // CraftBukkit start
    @Override
    public org.bukkit.craftbukkit.inventory.CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;
        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventory(this.dispenserInventory);
        bukkitEntity = new org.bukkit.craftbukkit.inventory.CraftInventoryView((HumanEntity) this.inventoryPlayer.player.getBukkitEntity(), inventory, this); // Akarin Forge - FIXME
        return bukkitEntity;
    } // CraftBukkit end

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 9, false))
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

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}