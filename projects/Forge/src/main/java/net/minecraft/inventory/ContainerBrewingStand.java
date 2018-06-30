package net.minecraft.inventory;

import org.bukkit.entity.HumanEntity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerBrewingStand extends Container
{
    private final IInventory tileBrewingStand;
    private final Slot slot;
    private int prevBrewTime;
    private int prevFuel;
    // CraftBukkit start
    private org.bukkit.craftbukkit.inventory.CraftInventoryView bukkitEntity = null;
    private InventoryPlayer inventoryPlayer;
    // CraftBukkit end

    public ContainerBrewingStand(InventoryPlayer playerInventory, IInventory tileBrewingStandIn)
    {
        inventoryPlayer = playerInventory; // CraftBukkit
        this.tileBrewingStand = tileBrewingStandIn;
        this.addSlotToContainer(new ContainerBrewingStand.Potion(tileBrewingStandIn, 0, 56, 51));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(tileBrewingStandIn, 1, 79, 58));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(tileBrewingStandIn, 2, 102, 51));
        this.slot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(tileBrewingStandIn, 3, 79, 17));
        this.addSlotToContainer(new ContainerBrewingStand.Fuel(tileBrewingStandIn, 4, 17, 17));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tileBrewingStand);
    }

    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icontainerlistener = this.listeners.get(i);

            if (this.prevBrewTime != this.tileBrewingStand.getField(0))
            {
                icontainerlistener.sendWindowProperty(this, 0, this.tileBrewingStand.getField(0));
            }

            if (this.prevFuel != this.tileBrewingStand.getField(1))
            {
                icontainerlistener.sendWindowProperty(this, 1, this.tileBrewingStand.getField(1));
            }
        }

        this.prevBrewTime = this.tileBrewingStand.getField(0);
        this.prevFuel = this.tileBrewingStand.getField(1);
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tileBrewingStand.setField(id, data);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        if (!this.checkReachable) return true; // CraftBukkit
        return this.tileBrewingStand.isUsableByPlayer(playerIn);
    }
    // CraftBukkit start
    @Override public org.bukkit.craftbukkit.inventory.CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;
        org.bukkit.craftbukkit.inventory.CraftInventoryBrewer inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryBrewer(this.tileBrewingStand);
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

            if ((index < 0 || index > 2) && index != 3 && index != 4)
            {
                if (this.slot.isItemValid(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 3, 4, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (ContainerBrewingStand.Potion.canHoldPotion(itemstack) && itemstack.getCount() == 1)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 3, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (ContainerBrewingStand.Fuel.isValidBrewingFuel(itemstack))
                {
                    if (!this.mergeItemStack(itemstack1, 4, 5, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 5 && index < 32)
                {
                    if (!this.mergeItemStack(itemstack1, 32, 41, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 32 && index < 41)
                {
                    if (!this.mergeItemStack(itemstack1, 5, 32, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (!this.mergeItemStack(itemstack1, 5, 41, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if (!this.mergeItemStack(itemstack1, 5, 41, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
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

    static class Fuel extends Slot
        {
            public Fuel(IInventory iInventoryIn, int index, int xPosition, int yPosition)
            {
                super(iInventoryIn, index, xPosition, yPosition);
            }

            public boolean isItemValid(ItemStack stack)
            {
                return isValidBrewingFuel(stack);
            }

            public static boolean isValidBrewingFuel(ItemStack itemStackIn)
            {
                return itemStackIn.getItem() == Items.BLAZE_POWDER;
            }

            public int getSlotStackLimit()
            {
                return 64;
            }
        }

    static class Ingredient extends Slot
        {
            public Ingredient(IInventory iInventoryIn, int index, int xPosition, int yPosition)
            {
                super(iInventoryIn, index, xPosition, yPosition);
            }

            public boolean isItemValid(ItemStack stack)
            {
                return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidIngredient(stack);
            }

            public int getSlotStackLimit()
            {
                return 64;
            }
        }

    static class Potion extends Slot
        {
            public Potion(IInventory p_i47598_1_, int p_i47598_2_, int p_i47598_3_, int p_i47598_4_)
            {
                super(p_i47598_1_, p_i47598_2_, p_i47598_3_, p_i47598_4_);
            }

            public boolean isItemValid(ItemStack stack)
            {
                return canHoldPotion(stack);
            }

            public int getSlotStackLimit()
            {
                return 1;
            }

            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
            {
                PotionType potiontype = PotionUtils.getPotionFromItem(stack);

                if (thePlayer instanceof EntityPlayerMP)
                {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerBrewedPotion(thePlayer, stack);
                    CriteriaTriggers.BREWED_POTION.trigger((EntityPlayerMP)thePlayer, potiontype);
                }

                super.onTake(thePlayer, stack);
                return stack;
            }

            public static boolean canHoldPotion(ItemStack stack)
            {
                return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidInput(stack);
            }
        }
}