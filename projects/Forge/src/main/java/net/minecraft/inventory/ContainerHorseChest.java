package net.minecraft.inventory;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerHorseChest extends InventoryBasic
{
    public ContainerHorseChest(String inventoryTitle, int slotCount, net.minecraft.entity.passive.AbstractHorse owner) // CraftBukkit
    {
        super(inventoryTitle, false, slotCount, (org.bukkit.entity.AbstractHorse) owner.getBukkitEntity()); // CraftBukkit
    }

    @SideOnly(Side.CLIENT)
    public ContainerHorseChest(ITextComponent inventoryTitle, int slotCount)
    {
        super(inventoryTitle, slotCount);
    }
}