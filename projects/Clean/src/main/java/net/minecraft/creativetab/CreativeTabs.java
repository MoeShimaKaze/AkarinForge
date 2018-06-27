package net.minecraft.creativetab;

import javax.annotation.Nullable;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CreativeTabs
{
    public static CreativeTabs[] CREATIVE_TAB_ARRAY = new CreativeTabs[12];
    public static final CreativeTabs BUILDING_BLOCKS = new CreativeTabs(0, "buildingBlocks")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Item.getItemFromBlock(Blocks.BRICK_BLOCK));
        }
    };
    public static final CreativeTabs DECORATIONS = new CreativeTabs(1, "decorations")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), 1, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta());
        }
    };
    public static final CreativeTabs REDSTONE = new CreativeTabs(2, "redstone")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.REDSTONE);
        }
    };
    public static final CreativeTabs TRANSPORTATION = new CreativeTabs(3, "transportation")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Item.getItemFromBlock(Blocks.GOLDEN_RAIL));
        }
    };
    public static final CreativeTabs MISC = new CreativeTabs(6, "misc")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.LAVA_BUCKET);
        }
    };
    public static final CreativeTabs SEARCH = (new CreativeTabs(5, "search")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.COMPASS);
        }
    }).setBackgroundImageName("item_search.png");
    public static final CreativeTabs FOOD = new CreativeTabs(7, "food")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.APPLE);
        }
    };
    public static final CreativeTabs TOOLS = (new CreativeTabs(8, "tools")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.IRON_AXE);
        }
    }).setRelevantEnchantmentTypes(new EnumEnchantmentType[] {EnumEnchantmentType.ALL, EnumEnchantmentType.DIGGER, EnumEnchantmentType.FISHING_ROD, EnumEnchantmentType.BREAKABLE});
    public static final CreativeTabs COMBAT = (new CreativeTabs(9, "combat")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.GOLDEN_SWORD);
        }
    }).setRelevantEnchantmentTypes(new EnumEnchantmentType[] {EnumEnchantmentType.ALL, EnumEnchantmentType.ARMOR, EnumEnchantmentType.ARMOR_FEET, EnumEnchantmentType.ARMOR_HEAD, EnumEnchantmentType.ARMOR_LEGS, EnumEnchantmentType.ARMOR_CHEST, EnumEnchantmentType.BOW, EnumEnchantmentType.WEAPON, EnumEnchantmentType.WEARABLE, EnumEnchantmentType.BREAKABLE});
    public static final CreativeTabs BREWING = new CreativeTabs(10, "brewing")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
        }
    };
    public static final CreativeTabs MATERIALS = MISC;
    public static final CreativeTabs HOTBAR = new CreativeTabs(4, "hotbar")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Blocks.BOOKSHELF);
        }
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(NonNullList<ItemStack> p_78018_1_)
        {
            throw new RuntimeException("Implement exception client-side.");
        }
        @SideOnly(Side.CLIENT)
        public boolean isAlignedRight()
        {
            return true;
        }
    };
    public static final CreativeTabs INVENTORY = (new CreativeTabs(11, "inventory")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Item.getItemFromBlock(Blocks.CHEST));
        }
    }).setBackgroundImageName("inventory.png").setNoScrollbar().setNoTitle();
    private final int tabIndex;
    private final String tabLabel;
    private String backgroundTexture = "items.png";
    private boolean hasScrollbar = true;
    private boolean drawTitle = true;
    private EnumEnchantmentType[] enchantmentTypes = new EnumEnchantmentType[0];
    private ItemStack iconItemStack;

    public CreativeTabs(int index, String label)
    {
        this.tabIndex = index;
        this.tabLabel = label;
        this.iconItemStack = ItemStack.EMPTY;
        CREATIVE_TAB_ARRAY[index] = this;
    }

    @SideOnly(Side.CLIENT)
    public int getTabIndex()
    {
        return this.tabIndex;
    }

    public CreativeTabs setBackgroundImageName(String texture)
    {
        this.backgroundTexture = texture;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public String getTabLabel()
    {
        return this.tabLabel;
    }

    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return "itemGroup." + this.getTabLabel();
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack()
    {
        if (this.iconItemStack.isEmpty())
        {
            this.iconItemStack = this.getTabIconItem();
        }

        return this.iconItemStack;
    }

    @SideOnly(Side.CLIENT)
    public abstract ItemStack getTabIconItem();

    @SideOnly(Side.CLIENT)
    public String getBackgroundImageName()
    {
        return this.backgroundTexture;
    }

    @SideOnly(Side.CLIENT)
    public boolean drawInForegroundOfTab()
    {
        return this.drawTitle;
    }

    public CreativeTabs setNoTitle()
    {
        this.drawTitle = false;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldHidePlayerInventory()
    {
        return this.hasScrollbar;
    }

    public CreativeTabs setNoScrollbar()
    {
        this.hasScrollbar = false;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public int getTabColumn()
    {
        return this.tabIndex % 6;
    }

    @SideOnly(Side.CLIENT)
    public boolean isTabInFirstRow()
    {
        return this.tabIndex < 6;
    }

    @SideOnly(Side.CLIENT)
    public boolean isAlignedRight()
    {
        return this.getTabColumn() == 5;
    }

    public EnumEnchantmentType[] getRelevantEnchantmentTypes()
    {
        return this.enchantmentTypes;
    }

    public CreativeTabs setRelevantEnchantmentTypes(EnumEnchantmentType... types)
    {
        this.enchantmentTypes = types;
        return this;
    }

    public boolean hasRelevantEnchantmentType(@Nullable EnumEnchantmentType enchantmentType)
    {
        if (enchantmentType != null)
        {
            for (EnumEnchantmentType enumenchantmenttype : this.enchantmentTypes)
            {
                if (enumenchantmenttype == enchantmentType)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public void displayAllRelevantItems(NonNullList<ItemStack> p_78018_1_)
    {
        for (Item item : Item.REGISTRY)
        {
            item.getSubItems(this, p_78018_1_);
        }
    }
}