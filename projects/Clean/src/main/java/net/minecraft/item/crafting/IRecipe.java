package net.minecraft.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRecipe
{
    boolean matches(InventoryCrafting inv, World worldIn);

    ItemStack getCraftingResult(InventoryCrafting inv);

    @SideOnly(Side.CLIENT)
    boolean canFit(int width, int height);

    ItemStack getRecipeOutput();

    NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv);

default NonNullList<Ingredient> getIngredients()
    {
        return NonNullList.<Ingredient>create();
    }

default boolean isDynamic()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
default String getGroup()
    {
        return "";
    }
}