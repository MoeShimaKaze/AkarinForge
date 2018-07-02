package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final EntityDragon dragon;
    private final IPhase[] phases = new IPhase[PhaseList.getTotalPhases()];
    private IPhase phase;

    public PhaseManager(EntityDragon dragonIn)
    {
        this.dragon = dragonIn;
        this.setPhase(PhaseList.HOVER);
    }

    public void setPhase(PhaseList<?> phaseIn)
    {
        if (this.phase == null || phaseIn != this.phase.getType())
        {
            if (this.phase != null)
            {
                this.phase.removeAreaEffect();
            }
            // CraftBukkit start - Call EnderDragonChangePhaseEvent
            org.bukkit.event.entity.EnderDragonChangePhaseEvent event = new org.bukkit.event.entity.EnderDragonChangePhaseEvent(
                    (org.bukkit.craftbukkit.entity.CraftEnderDragon) this.dragon.getBukkitEntity(),
                    (this.phase == null) ? null : org.bukkit.craftbukkit.entity.CraftEnderDragon.getBukkitPhase(this.phase.getType()),
                            org.bukkit.craftbukkit.entity.CraftEnderDragon.getBukkitPhase(phaseIn)
            );
            this.dragon.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            phaseIn = org.bukkit.craftbukkit.entity.CraftEnderDragon.getMinecraftPhase(event.getNewPhase());
            // CraftBukkit end

            this.phase = this.getPhase(phaseIn);

            if (!this.dragon.world.isRemote)
            {
                this.dragon.getDataManager().set(EntityDragon.PHASE, Integer.valueOf(phaseIn.getId()));
            }

            LOGGER.debug("Dragon is now in phase {} on the {}", phaseIn, this.dragon.world.isRemote ? "client" : "server");
            this.phase.initPhase();
        }
    }

    public IPhase getCurrentPhase()
    {
        return this.phase;
    }

    public <T extends IPhase> T getPhase(PhaseList<T> phaseIn)
    {
        int i = phaseIn.getId();

        if (this.phases[i] == null)
        {
            this.phases[i] = phaseIn.createPhase(this.dragon);
        }

        return (T)this.phases[i];
    }
}