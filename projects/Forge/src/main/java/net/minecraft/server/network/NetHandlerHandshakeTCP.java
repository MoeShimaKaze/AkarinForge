package net.minecraft.server.network;

import com.google.common.collect.Multiset.Entry;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer
{
    private final MinecraftServer server;
    private final NetworkManager networkManager;
    // CraftBukkit start - add fields
    private static final java.util.HashMap<java.net.InetAddress, Long> throttleTracker = new java.util.HashMap<java.net.InetAddress, Long>();
    private static int throttleCounter = 0;
    // CraftBukkit end

    public NetHandlerHandshakeTCP(MinecraftServer serverIn, NetworkManager netManager)
    {
        this.server = serverIn;
        this.networkManager = netManager;
    }

    public void processHandshake(C00Handshake packetIn)
    {
        if (!net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerHandshake(packetIn, this.networkManager)) return;

        switch (packetIn.getRequestedState())
        {
            case LOGIN:
                this.networkManager.setConnectionState(EnumConnectionState.LOGIN);
                // CraftBukkit start - Connection throttle
                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = net.minecraft.server.MinecraftServer.getServer().server.getConnectionThrottle();
                    java.net. InetAddress address = ((java.net.InetSocketAddress) this.networkManager.getRemoteAddress()).getAddress();
                    synchronized (throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            ITextComponent itextcomponent = new TextComponentTranslation("Connection throttled! Please wait before reconnecting.");
                            this.networkManager.sendPacket(new SPacketDisconnect(itextcomponent));
                            this.networkManager.closeChannel(itextcomponent);
                            return;
                        }
                        throttleTracker.put(address, currentTime);
                        throttleCounter++;
                        if (throttleCounter > 200) {
                            throttleCounter = 0;
                            java.util.Iterator<java.util.Map.Entry<java.net.InetAddress, Long>> iter = throttleTracker.entrySet().iterator(); // Cleanup stale entries
                            while (iter.hasNext()) {
                                java.util.Map.Entry<java.net.InetAddress, Long> entry = iter.next();
                                if (entry.getValue() > connectionThrottle) iter.remove();
                            }
                        }
                    }
                } catch (Throwable t) {
                    org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
                } // CraftBukkit end

                if (packetIn.getProtocolVersion() > 340)
                {
                    ITextComponent itextcomponent = new TextComponentTranslation("multiplayer.disconnect.outdated_server", new Object[] {"1.12.2"});
                    this.networkManager.sendPacket(new SPacketDisconnect(itextcomponent));
                    this.networkManager.closeChannel(itextcomponent);
                }
                else if (packetIn.getProtocolVersion() < 340)
                {
                    ITextComponent itextcomponent1 = new TextComponentTranslation("multiplayer.disconnect.outdated_client", new Object[] {"1.12.2"});
                    this.networkManager.sendPacket(new SPacketDisconnect(itextcomponent1));
                    this.networkManager.closeChannel(itextcomponent1);
                }
                else
                {
                    this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
                    ((NetHandlerLoginServer) this.networkManager.getNetHandler()).hostname = packetIn.ip + ":" + packetIn.port; // CraftBukkit - set hostname
                }

                break;
            case STATUS:
                this.networkManager.setConnectionState(EnumConnectionState.STATUS);
                this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
        }
    }

    public void onDisconnect(ITextComponent reason)
    {
    }
}