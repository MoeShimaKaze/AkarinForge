package net.minecraft.server.network;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.status.INetHandlerStatusServer;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class NetHandlerStatusServer implements INetHandlerStatusServer
{
    private static final ITextComponent EXIT_MESSAGE = new TextComponentString("Status request has been handled.");
    private final MinecraftServer server;
    private final NetworkManager networkManager;
    private boolean handled;

    public NetHandlerStatusServer(MinecraftServer serverIn, NetworkManager netManager)
    {
        this.server = serverIn;
        this.networkManager = netManager;
    }

    public void onDisconnect(ITextComponent reason)
    {
    }

    public void processServerQuery(CPacketServerQuery packetIn)
    {
        if (this.handled)
        {
            this.networkManager.closeChannel(EXIT_MESSAGE);
        }
        else
        {
            this.handled = true;
            // CraftBukkit start
            final Object[] players = server.getPlayerList().playerEntityList.toArray();
            class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {
                org.bukkit.craftbukkit.util.CraftIconCache icon = server.server.getServerIcon();
                ServerListPingEvent() {
                    super(((java.net.InetSocketAddress) networkManager.getRemoteAddress()).getAddress(), server.getMOTD(), server.getPlayerList().getMaxPlayers());
                }
                @Override
                public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
                    if (!(icon instanceof org.bukkit.craftbukkit.util.CraftIconCache)) {
                        throw new IllegalArgumentException(icon + " was not created by " + org.bukkit.craftbukkit.CraftServer.class);
                    }
                    this.icon = (org.bukkit.craftbukkit.util.CraftIconCache) icon;
                }
                @Override
                public java.util.Iterator<org.bukkit.entity.Player> iterator() throws UnsupportedOperationException {
                    return new java.util.Iterator<org.bukkit.entity.Player>() {
                        int i;
                        int ret = Integer.MIN_VALUE;
                        net.minecraft.entity.player.EntityPlayerMP player;
                        @Override
                        public boolean hasNext() {
                            if (player != null) {
                                return true;
                            }
                            final Object[] currentPlayers = players;
                            for (int length = currentPlayers.length, i = this.i; i < length; i++) {
                                final net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) currentPlayers[i];
                                if (player != null) {
                                    this.i = i + 1;
                                    this.player = player;
                                    return true;
                                }
                            }
                            return false;
                        }
                        @Override
                        public org.bukkit.entity.Player next() {
                            if (!hasNext()) {
                                throw new java.util.NoSuchElementException();
                            }
                            final net.minecraft.entity.player.EntityPlayerMP player = this.player;
                            this.player = null;
                            this.ret = this.i - 1;
                            return player.getBukkitEntity();
                        }
                        @Override
                        public void remove() {
                            final Object[] currentPlayers = players;
                            final int i = this.ret;
                            if (i < 0 || currentPlayers[i] == null) {
                                throw new IllegalStateException();
                            }
                            currentPlayers[i] = null;
                        }
                    };
                }
            }
            ServerListPingEvent event = new ServerListPingEvent();
            this.server.server.getPluginManager().callEvent(event);
            java.util.List<com.mojang.authlib.GameProfile> profiles = new java.util.ArrayList<com.mojang.authlib.GameProfile>(players.length);
            for (Object player : players) {
                if (player != null) {
                    profiles.add(((net.minecraft.entity.player.EntityPlayerMP) player).getGameProfile());
                }
            }
            net.minecraft.network.ServerStatusResponse.Players playerSample = new net.minecraft.network.ServerStatusResponse.Players(event.getMaxPlayers(), profiles.size());
            playerSample.setPlayers(profiles.toArray(new com.mojang.authlib.GameProfile[profiles.size()]));
            net.minecraft.network.ServerStatusResponse ping = new net.minecraft.network.ServerStatusResponse();
            ping.setFavicon(event.icon.value);
            ping.setServerDescription(new TextComponentString(event.getMotd()));
            ping.setPlayers(playerSample);
            int version = server.getServerStatusResponse().getVersion().getProtocol();
            ping.setVersion(new net.minecraft.network.ServerStatusResponse.Version(server.getServerModName() + " " + server.getMinecraftVersion(), version));
            this.networkManager.sendPacket(new SPacketServerInfo(ping));
            // CraftBukkit end
        }
    }

    public void processPing(CPacketPing packetIn)
    {
        this.networkManager.sendPacket(new SPacketPong(packetIn.getClientTime()));
        this.networkManager.closeChannel(EXIT_MESSAGE);
    }
}