package net.minecraft.world;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldType
{
    public static WorldType[] WORLD_TYPES = new WorldType[16];
    public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
    public static final WorldType FLAT = new WorldType(1, "flat");
    public static final WorldType LARGE_BIOMES = new WorldType(2, "largeBiomes");
    public static final WorldType AMPLIFIED = (new WorldType(3, "amplified")).enableInfoNotice();
    public static final WorldType CUSTOMIZED = new WorldType(4, "customized");
    public static final WorldType DEBUG_ALL_BLOCK_STATES = new WorldType(5, "debug_all_block_states");
    public static final WorldType DEFAULT_1_1 = (new WorldType(8, "default_1_1", 0)).setCanBeCreated(false);
    private final int id;
    private final String name;
    private final int version;
    private boolean canBeCreated;
    private boolean versioned;
    private boolean hasInfoNotice;

    private WorldType(int id, String name)
    {
        this(id, name, 0);
    }

    private WorldType(int id, String name, int version)
    {
        this.name = name;
        this.version = version;
        this.canBeCreated = true;
        this.id = id;
        WORLD_TYPES[id] = this;
    }

    public String getName()
    {
        return this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return "generator." + this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getInfoTranslationKey()
    {
        return this.getTranslationKey() + ".info";
    }

    public int getVersion()
    {
        return this.version;
    }

    public WorldType getWorldTypeForGeneratorVersion(int version)
    {
        return this == DEFAULT && version == 0 ? DEFAULT_1_1 : this;
    }

    private WorldType setCanBeCreated(boolean enable)
    {
        this.canBeCreated = enable;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean canBeCreated()
    {
        return this.canBeCreated;
    }

    private WorldType setVersioned()
    {
        this.versioned = true;
        return this;
    }

    public boolean isVersioned()
    {
        return this.versioned;
    }

    public static WorldType parseWorldType(String type)
    {
        for (WorldType worldtype : WORLD_TYPES)
        {
            if (worldtype != null && worldtype.name.equalsIgnoreCase(type))
            {
                return worldtype;
            }
        }

        return null;
    }

    public int getId()
    {
        return this.id;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasInfoNotice()
    {
        return this.hasInfoNotice;
    }

    private WorldType enableInfoNotice()
    {
        this.hasInfoNotice = true;
        return this;
    }
}