package net.minecraft.entity.item;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEnderCrystal extends Entity
{
    private static final DataParameter<Optional<BlockPos>> BEAM_TARGET = EntityDataManager.<Optional<BlockPos>>createKey(EntityEnderCrystal.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Boolean> SHOW_BOTTOM = EntityDataManager.<Boolean>createKey(EntityEnderCrystal.class, DataSerializers.BOOLEAN);
    public int innerRotation;

    public EntityEnderCrystal(World worldIn)
    {
        super(worldIn);
        this.preventEntitySpawning = true;
        this.setSize(2.0F, 2.0F);
        this.innerRotation = this.rand.nextInt(100000);
    }

    public EntityEnderCrystal(World worldIn, double x, double y, double z)
    {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        this.getDataManager().register(BEAM_TARGET, Optional.absent());
        this.getDataManager().register(SHOW_BOTTOM, Boolean.valueOf(true));
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.innerRotation;

        if (!this.world.isRemote)
        {
            BlockPos blockpos = new BlockPos(this);

            if (this.world.provider instanceof WorldProviderEnd && this.world.getBlockState(blockpos).getBlock() != Blocks.FIRE)
            {
                // CraftBukkit start
                if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.world, blockpos.getX(), blockpos.getY(), blockpos.getZ(), this).isCancelled()) {
                    this.world.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
                } // CraftBukkit end
            }
        }
    }

    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        if (this.getBeamTarget() != null)
        {
            compound.setTag("BeamTarget", NBTUtil.createPosTag(this.getBeamTarget()));
        }

        compound.setBoolean("ShowBottom", this.shouldShowBottom());
    }

    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("BeamTarget", 10))
        {
            this.setBeamTarget(NBTUtil.getPosFromTag(compound.getCompoundTag("BeamTarget")));
        }

        if (compound.hasKey("ShowBottom", 1))
        {
            this.setShowBottom(compound.getBoolean("ShowBottom"));
        }
    }

    public boolean canBeCollidedWith()
    {
        return true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (source.getTrueSource() instanceof EntityDragon)
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.world.isRemote)
            {
                if (org.bukkit.craftbukkit.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, source, amount)) return false; // CraftBukkit - All non-living entities need this
                this.setDead();

                if (!this.world.isRemote)
                {
                    if (!source.isExplosion())
                    {
                        // CraftBukkit start
                        org.bukkit.event.entity.ExplosionPrimeEvent event = new org.bukkit.event.entity.ExplosionPrimeEvent(this.getBukkitEntity(), 6.0F, true);
                        this.world.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            this.isDead = false;
                            return false;
                        }
                        this.world.createExplosion(this, this.posX, this.posY, this.posZ, event.getRadius(), event.getFire());
                        // CraftBukkit end
                    }

                    this.onCrystalDestroyed(source);
                }
            }

            return true;
        }
    }

    public void onKillCommand()
    {
        this.onCrystalDestroyed(DamageSource.GENERIC);
        super.onKillCommand();
    }

    private void onCrystalDestroyed(DamageSource source)
    {
        if (this.world.provider instanceof WorldProviderEnd)
        {
            WorldProviderEnd worldproviderend = (WorldProviderEnd)this.world.provider;
            DragonFightManager dragonfightmanager = worldproviderend.getDragonFightManager();

            if (dragonfightmanager != null)
            {
                dragonfightmanager.onCrystalDestroyed(this, source);
            }
        }
    }

    public void setBeamTarget(@Nullable BlockPos beamTarget)
    {
        this.getDataManager().set(BEAM_TARGET, Optional.fromNullable(beamTarget));
    }

    @Nullable
    public BlockPos getBeamTarget()
    {
        return (BlockPos)((Optional)this.getDataManager().get(BEAM_TARGET)).orNull();
    }

    public void setShowBottom(boolean showBottom)
    {
        this.getDataManager().set(SHOW_BOTTOM, Boolean.valueOf(showBottom));
    }

    public boolean shouldShowBottom()
    {
        return ((Boolean)this.getDataManager().get(SHOW_BOTTOM)).booleanValue();
    }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return super.isInRangeToRenderDist(distance) || this.getBeamTarget() != null;
    }
}