package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import akka.routing.ListenerMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, ISidedInventory
{
    public static final Potion[][] EFFECTS_LIST = new Potion[][] {{MobEffects.SPEED, MobEffects.HASTE}, {MobEffects.RESISTANCE, MobEffects.JUMP_BOOST}, {MobEffects.STRENGTH}, {MobEffects.REGENERATION}};
    private static final Set<Potion> VALID_EFFECTS = Sets.<Potion>newHashSet();
    private final List<TileEntityBeacon.BeamSegment> beamSegments = Lists.<TileEntityBeacon.BeamSegment>newArrayList();
    @SideOnly(Side.CLIENT)
    private long beamRenderCounter;
    @SideOnly(Side.CLIENT)
    private float beamRenderScale;
    private boolean isComplete;
    private int levels = -1;
    @Nullable
    public Potion primaryEffect; // Akarin Forge - public
    @Nullable
    public Potion secondaryEffect; // Akarin Forge - public
    private ItemStack payment = ItemStack.EMPTY;
    private String customName;
    // CraftBukkit start - add fields and methods
    public List<org.bukkit.entity.HumanEntity> transaction = new java.util.ArrayList<org.bukkit.entity.HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return Arrays.asList(this.payment);
    }

    public void onOpen(org.bukkit.craftbukkit.entity.CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(org.bukkit.craftbukkit.entity.CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<org.bukkit.entity.HumanEntity> getViewers() {
        return transaction;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    public PotionEffect getPrimaryEffect() {
        return (this.primaryEffect != null) ? org.bukkit.craftbukkit.potion.CraftPotionUtil.toBukkit(new PotionEffect(this.primaryEffect, getLevel(), getAmplification(), true, true)) : null;
    }

    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect()) ? org.bukkit.craftbukkit.potion.CraftPotionUtil.toBukkit(new PotionEffect(this.secondaryEffect, getLevel(), getAmplification(), true, true)) : null;
    } // CraftBukkit end

    public void update()
    {
        if (this.world.getTotalWorldTime() % 80L == 0L)
        {
            this.updateBeacon();
        }
    }

    public void updateBeacon()
    {
        if (this.world != null)
        {
            this.updateSegmentColors();
            this.addEffectsToPlayers();
        }
    }

    // CraftBukkit start - split into components
    private byte getAmplification() {
    {
            int i = 0;

            if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect)
            {
                i = 1;
            }

            return (byte) i;
    }}
    private int getLevel() {{
            int j = (9 + this.levels * 2) * 20;
            return j;
    }}
    public List<EntityPlayer> getHumansInRange() {{
            double d0 = (double) (this.levels * 10 + 10);
            int k = this.pos.getX();
            int l = this.pos.getY();
            int i1 = this.pos.getZ();
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double)k, (double)l, (double)i1, (double)(k + 1), (double)(l + 1), (double)(i1 + 1))).grow(d0).expand(0.0D, (double)this.world.getHeight(), 0.0D);
            List<EntityPlayer> list = this.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

            return list;
    }}
    private void applyEffect(List<EntityPlayer> list, Potion effects, int j, int i) {{
            for (EntityPlayer entityplayer : list)
            {
                entityplayer.addPotionEffect(new PotionEffect(effects, j, i, true, true));
            }
    }}
    private boolean hasSecondaryEffect() {{
            if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null)
            {
                return true;
            }
            return false;
        }
    }
    private void addEffectsToPlayers() {
        if (this.isComplete && this.levels > 0 && !this.world.isRemote && this.primaryEffect != null) {
            byte b0 = getAmplification();
            int i = getLevel();
            List<EntityPlayer> list = getHumansInRange();
            applyEffect(list, this.primaryEffect, i, b0);
            if (hasSecondaryEffect()) applyEffect(list, this.secondaryEffect, i, 0);
        }
    } // CraftBukkit end

    private void updateSegmentColors()
    {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        int l = this.levels;
        this.levels = 0;
        this.beamSegments.clear();
        this.isComplete = true;
        TileEntityBeacon.BeamSegment tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(EnumDyeColor.WHITE.getColorComponentValues());
        this.beamSegments.add(tileentitybeacon$beamsegment);
        boolean flag = true;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i1 = j + 1; i1 < 256; ++i1)
        {
            IBlockState iblockstate = this.world.getBlockState(blockpos$mutableblockpos.setPos(i, i1, k));
            float[] afloat;

            if (iblockstate.getBlock() == Blocks.STAINED_GLASS)
            {
                afloat = ((EnumDyeColor)iblockstate.getValue(BlockStainedGlass.COLOR)).getColorComponentValues();
            }
            else
            {
                if (iblockstate.getBlock() != Blocks.STAINED_GLASS_PANE)
                {
                    if (iblockstate.getLightOpacity(world, blockpos$mutableblockpos) >= 15 && iblockstate.getBlock() != Blocks.BEDROCK)
                    {
                        this.isComplete = false;
                        this.beamSegments.clear();
                        break;
                    }
                    float[] customColor = iblockstate.getBlock().getBeaconColorMultiplier(iblockstate, this.world, blockpos$mutableblockpos, getPos());
                    if (customColor != null)
                        afloat = customColor;
                    else {
                    tileentitybeacon$beamsegment.incrementHeight();
                    continue;
                    }
                }
                else
                afloat = ((EnumDyeColor)iblockstate.getValue(BlockStainedGlassPane.COLOR)).getColorComponentValues();
            }

            if (!flag)
            {
                afloat = new float[] {(tileentitybeacon$beamsegment.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[2] + afloat[2]) / 2.0F};
            }

            if (Arrays.equals(afloat, tileentitybeacon$beamsegment.getColors()))
            {
                tileentitybeacon$beamsegment.incrementHeight();
            }
            else
            {
                tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(afloat);
                this.beamSegments.add(tileentitybeacon$beamsegment);
            }

            flag = false;
        }

        if (this.isComplete)
        {
            for (int l1 = 1; l1 <= 4; this.levels = l1++)
            {
                int i2 = j - l1;

                if (i2 < 0)
                {
                    break;
                }

                boolean flag1 = true;

                for (int j1 = i - l1; j1 <= i + l1 && flag1; ++j1)
                {
                    for (int k1 = k - l1; k1 <= k + l1; ++k1)
                    {
                        Block block = this.world.getBlockState(new BlockPos(j1, i2, k1)).getBlock();

                        if (!block.isBeaconBase(this.world, new BlockPos(j1, i2, k1), getPos()))
                        {
                            flag1 = false;
                            break;
                        }
                    }
                }

                if (!flag1)
                {
                    break;
                }
            }

            if (this.levels == 0)
            {
                this.isComplete = false;
            }
        }

        if (!this.world.isRemote && l < this.levels)
        {
            for (EntityPlayerMP entityplayermp : this.world.getEntitiesWithinAABB(EntityPlayerMP.class, (new AxisAlignedBB((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).grow(10.0D, 5.0D, 10.0D)))
            {
                CriteriaTriggers.CONSTRUCT_BEACON.trigger(entityplayermp, this);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public List<TileEntityBeacon.BeamSegment> getBeamSegments()
    {
        return this.beamSegments;
    }

    @SideOnly(Side.CLIENT)
    public float shouldBeamRender()
    {
        if (!this.isComplete)
        {
            return 0.0F;
        }
        else
        {
            int i = (int)(this.world.getTotalWorldTime() - this.beamRenderCounter);
            this.beamRenderCounter = this.world.getTotalWorldTime();

            if (i > 1)
            {
                this.beamRenderScale -= (float)i / 40.0F;

                if (this.beamRenderScale < 0.0F)
                {
                    this.beamRenderScale = 0.0F;
                }
            }

            this.beamRenderScale += 0.025F;

            if (this.beamRenderScale > 1.0F)
            {
                this.beamRenderScale = 1.0F;
            }

            return this.beamRenderScale;
        }
    }

    public int getLevels()
    {
        return this.levels;
    }

    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    @Nullable
    private static Potion isBeaconEffect(int p_184279_0_)
    {
        Potion potion = Potion.getPotionById(p_184279_0_);
        return VALID_EFFECTS.contains(potion) ? potion : null;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        // Craftbukkit start - persist manually set non-default beacon effects (SPIGOT-3598)
        this.primaryEffect = Potion.getPotionById(compound.getInteger("Primary"));
        this.secondaryEffect = Potion.getPotionById(compound.getInteger("Secondary"));
        // Craftbukkit end
        this.levels = compound.getInteger("Levels");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("Primary", Potion.getIdFromPotion(this.primaryEffect));
        compound.setInteger("Secondary", Potion.getIdFromPotion(this.secondaryEffect));
        compound.setInteger("Levels", this.levels);
        return compound;
    }

    public int getSizeInventory()
    {
        return 1;
    }

    public boolean isEmpty()
    {
        return this.payment.isEmpty();
    }

    public ItemStack getStackInSlot(int index)
    {
        return index == 0 ? this.payment : ItemStack.EMPTY;
    }

    public ItemStack decrStackSize(int index, int count)
    {
        if (index == 0 && !this.payment.isEmpty())
        {
            if (count >= this.payment.getCount())
            {
                ItemStack itemstack = this.payment;
                this.payment = ItemStack.EMPTY;
                return itemstack;
            }
            else
            {
                return this.payment.splitStack(count);
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack removeStackFromSlot(int index)
    {
        if (index == 0)
        {
            ItemStack itemstack = this.payment;
            this.payment = ItemStack.EMPTY;
            return itemstack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index == 0)
        {
            this.payment = stack;
        }
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.beacon";
    }

    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    public int getInventoryStackLimit()
    {
        return 1;
    }

    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getItem() != null && stack.getItem().isBeaconPayment(stack);
    }

    public String getGuiID()
    {
        return "minecraft:beacon";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerBeacon(playerInventory, this);
    }

    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return this.levels;
            case 1:
                return Potion.getIdFromPotion(this.primaryEffect);
            case 2:
                return Potion.getIdFromPotion(this.secondaryEffect);
            default:
                return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                this.levels = value;
                break;
            case 1:
                this.primaryEffect = isBeaconEffect(value);
                break;
            case 2:
                this.secondaryEffect = isBeaconEffect(value);
        }
    }

    public int getFieldCount()
    {
        return 3;
    }

    public void clear()
    {
        this.payment = ItemStack.EMPTY;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            this.updateBeacon();
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }

    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[0];
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return false;
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return false;
    }

    static
    {
        for (Potion[] apotion : EFFECTS_LIST)
        {
            Collections.addAll(VALID_EFFECTS, apotion);
        }
    }

    public static class BeamSegment
        {
            private final float[] colors;
            private int height;

            public BeamSegment(float[] colorsIn)
            {
                this.colors = colorsIn;
                this.height = 1;
            }

            protected void incrementHeight()
            {
                ++this.height;
            }

            public float[] getColors()
            {
                return this.colors;
            }

            @SideOnly(Side.CLIENT)
            public int getHeight()
            {
                return this.height;
            }
        }
}