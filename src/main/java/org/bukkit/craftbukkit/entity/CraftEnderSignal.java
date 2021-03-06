package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.item.EntityEnderEye;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;

public class CraftEnderSignal extends CraftEntity implements EnderSignal {
    public CraftEnderSignal(CraftServer server, EntityEnderEye entity) {
        super(server, entity);
    }

    @Override
    public EntityEnderEye getHandle() {
        return (EntityEnderEye) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderSignal";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDER_SIGNAL;
    }

    @Override
    public Location getTargetLocation() {
        return new Location(getWorld(), getHandle().targetX, getHandle().targetY, getHandle().targetZ, getHandle().rotationYaw, getHandle().rotationPitch); // PAIL rename targetX, targetY, targetZ
    }

    @Override
    public void setTargetLocation(Location location) {
        Preconditions.checkArgument(getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        getHandle().moveTowards(new BlockPos(location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public boolean getDropItem() {
        return getHandle().shatterOrDrop; // PAIL rename getDropItem
    }

    @Override
    public void setDropItem(boolean shouldDropItem) {
        getHandle().shatterOrDrop = shouldDropItem; // PAIL rename getDropItem
    }

    @Override
    public int getDespawnTimer() {
        return getHandle().despawnTimer; // PAIL rename despawnTimer
    }

    @Override
    public void setDespawnTimer(int time) {
        getHandle().despawnTimer = time; // PAIL rename despawnTimer
    }
}
