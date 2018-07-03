package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import javax.naming.NamingSecurityException;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CommandBlockBaseLogic implements ICommandSender
{
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    private ITextComponent lastOutput;
    private String commandStored = "";
    private String customName = "@";
    private final CommandResultStats resultStats = new CommandResultStats();
    protected org.bukkit.command.CommandSender sender; // CraftBukkit - add sender

    public int getSuccessCount()
    {
        return this.successCount;
    }

    public void setSuccessCount(int successCountIn)
    {
        this.successCount = successCountIn;
    }

    public ITextComponent getLastOutput()
    {
        return (ITextComponent)(this.lastOutput == null ? new TextComponentString("") : this.lastOutput);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound p_189510_1_)
    {
        p_189510_1_.setString("Command", this.commandStored);
        p_189510_1_.setInteger("SuccessCount", this.successCount);
        p_189510_1_.setString("CustomName", this.customName);
        p_189510_1_.setBoolean("TrackOutput", this.trackOutput);

        if (this.lastOutput != null && this.trackOutput)
        {
            p_189510_1_.setString("LastOutput", ITextComponent.Serializer.componentToJson(this.lastOutput));
        }

        p_189510_1_.setBoolean("UpdateLastExecution", this.updateLastExecution);

        if (this.updateLastExecution && this.lastExecution > 0L)
        {
            p_189510_1_.setLong("LastExecution", this.lastExecution);
        }

        this.resultStats.writeStatsToNBT(p_189510_1_);
        return p_189510_1_;
    }

    public void readDataFromNBT(NBTTagCompound nbt)
    {
        this.commandStored = nbt.getString("Command");
        this.successCount = nbt.getInteger("SuccessCount");

        if (nbt.hasKey("CustomName", 8))
        {
            this.customName = nbt.getString("CustomName");
        }

        if (nbt.hasKey("TrackOutput", 1))
        {
            this.trackOutput = nbt.getBoolean("TrackOutput");
        }

        if (nbt.hasKey("LastOutput", 8) && this.trackOutput)
        {
            try
            {
                this.lastOutput = ITextComponent.Serializer.jsonToComponent(nbt.getString("LastOutput"));
            }
            catch (Throwable throwable)
            {
                this.lastOutput = new TextComponentString(throwable.getMessage());
            }
        }
        else
        {
            this.lastOutput = null;
        }

        if (nbt.hasKey("UpdateLastExecution"))
        {
            this.updateLastExecution = nbt.getBoolean("UpdateLastExecution");
        }

        if (this.updateLastExecution && nbt.hasKey("LastExecution"))
        {
            this.lastExecution = nbt.getLong("LastExecution");
        }
        else
        {
            this.lastExecution = -1L;
        }

        this.resultStats.readStatsFromNBT(nbt);
    }

    public boolean canUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 2;
    }

    public void setCommand(String command)
    {
        this.commandStored = command;
        this.successCount = 0;
    }

    public String getCommand()
    {
        return this.commandStored;
    }

    public boolean trigger(World worldIn)
    {
        if (!worldIn.isRemote && worldIn.getTotalWorldTime() != this.lastExecution)
        {
            if ("Searge".equalsIgnoreCase(this.commandStored))
            {
                this.lastOutput = new TextComponentString("#itzlipofutzli");
                this.successCount = 1;
                return true;
            }
            else
            {
                MinecraftServer minecraftserver = this.getServer();

                if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled())
                {
                    try
                    {
                        this.lastOutput = null;
                        this.successCount = executeSafely(this, sender, this.commandStored); // CraftBukkit - Handle command block commands using Bukkit dispatcher
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Executing command block");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
                        crashreportcategory.addDetail("Command", new ICrashReportDetail<String>()
                        {
                            public String call() throws Exception
                            {
                                return CommandBlockBaseLogic.this.getCommand();
                            }
                        });
                        crashreportcategory.addDetail("Name", new ICrashReportDetail<String>()
                        {
                            public String call() throws Exception
                            {
                                return CommandBlockBaseLogic.this.getName();
                            }
                        });
                        throw new ReportedException(crashreport);
                    }
                }
                else
                {
                    this.successCount = 0;
                }

                if (this.updateLastExecution)
                {
                    this.lastExecution = worldIn.getTotalWorldTime();
                }
                else
                {
                    this.lastExecution = -1L;
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }
    // CraftBukkit start
    public static int executeSafely(ICommandSender sender, org.bukkit.command.CommandSender bSender, String command) {
        try {
            return executeCommand(sender, bSender, command);
        } catch (net.minecraft.command.CommandException commandexception) {
            // Taken from CommandHandler
            net.minecraft.util.text.TextComponentTranslation chatmessage = new net.minecraft.util.text.TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            chatmessage.getStyle().setColor(net.minecraft.util.text.TextFormatting.RED);
            sender.sendMessage(chatmessage);
        }
        return 0;
    }
    public static int executeCommand(ICommandSender sender, org.bukkit.command.CommandSender bSender, String command) throws net.minecraft.command.CommandException {
        org.bukkit.command.SimpleCommandMap commandMap = sender.getEntityWorld().getServer().getCommandMap();
        com.google.common.base.Joiner joiner = com.google.common.base.Joiner.on(" ");
        if (command.startsWith("/")) command = command.substring(1);
        org.bukkit.event.server.ServerCommandEvent event = new org.bukkit.event.server.ServerCommandEvent(bSender, command);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;

        command = event.getCommand();
        String[] args = command.split(" ");
        java.util.ArrayList<String[]> commands = new java.util.ArrayList<String[]>();
        String cmd = args[0];
        if (cmd.startsWith("minecraft:")) cmd = cmd.substring("minecraft:".length());
        if (cmd.startsWith("bukkit:")) cmd = cmd.substring("bukkit:".length());
        // Block disallowed commands
        if (cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("op")
                || cmd.equalsIgnoreCase("deop") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("ban-ip")
                || cmd.equalsIgnoreCase("pardon") || cmd.equalsIgnoreCase("pardon-ip") || cmd.equalsIgnoreCase("reload")) {
            return 0;
        }
        // Handle vanilla commands;
        org.bukkit.command.Command commandBlockCommand = commandMap.getCommand(args[0]);
        if (sender.getEntityWorld().getServer().getCommandBlockOverride(args[0])) {
            commandBlockCommand = commandMap.getCommand("minecraft:" + args[0]);
        }
        if (commandBlockCommand instanceof org.bukkit.craftbukkit.command.VanillaCommandWrapper) {
            command = command.trim();
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            String as[] = command.split(" ");
            as = org.bukkit.craftbukkit.command.VanillaCommandWrapper.dropFirstArgument(as);
            if (!sender.getEntityWorld().getServer().getPermissionOverride(sender) && !((org.bukkit.craftbukkit.command.VanillaCommandWrapper) commandBlockCommand).testPermission(bSender)) {
                return 0;
            }
            return ((org.bukkit.craftbukkit.command.VanillaCommandWrapper) commandBlockCommand).dispatchVanillaCommand(bSender, sender, as);
        }
        // Make sure this is a valid command
        if (commandMap.getCommand(args[0]) == null) return 0;

        commands.add(args);
        // Find positions of command block syntax, if any        
        net.minecraft.world.WorldServer[] prev = net.minecraft.server.MinecraftServer.getServer().worlds;
        MinecraftServer server = net.minecraft.server.MinecraftServer.getServer();
        server.worlds = new net.minecraft.world.WorldServer[server.bworlds.size()];
        server.worlds[0] = (net.minecraft.world.WorldServer) sender.getEntityWorld();
        int bpos = 0;
        for (int pos = 1; pos < server.worlds.length; pos++) {
            net.minecraft.world.WorldServer world = server.bworlds.get(bpos++);
            if (server.worlds[0] == world) {
                pos--;
                continue;
            }
            server.worlds[pos] = world;
        }
        try {
            java.util.ArrayList<String[]> newCommands = new java.util.ArrayList<String[]>();
            for (int i = 0; i < args.length; i++) {
                if (net.minecraft.command.EntitySelector.isSelector(args[i])) {
                    for (int j = 0; j < commands.size(); j++) {
                        newCommands.addAll(buildCommands(sender, commands.get(j), i));
                    }
                    java.util.ArrayList<String[]> temp = commands;
                    commands = newCommands;
                    newCommands = temp;
                    newCommands.clear();
                }
            }
        } finally {
            net.minecraft.server.MinecraftServer.getServer().worlds = prev;
        }
        int completed = 0;
        // Now dispatch all of the commands we ended up with
        for (int i = 0; i < commands.size(); i++) {
            try {
                if (commandMap.dispatch(bSender, joiner.join(java.util.Arrays.asList(commands.get(i))))) {
                    completed++;
                }
            } catch (Throwable exception) {
                if (sender.getCommandSenderEntity() instanceof net.minecraft.entity.item.EntityMinecartCommandBlock) {
                    net.minecraft.server.MinecraftServer.getServer().server.getLogger().log(java.util.logging.Level.WARNING, String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()), exception);
                } else if (sender instanceof CommandBlockBaseLogic) {
                    CommandBlockBaseLogic listener = (CommandBlockBaseLogic) sender;
                    net.minecraft.server.MinecraftServer.getServer().server.getLogger().log(java.util.logging.Level.WARNING, String.format("CommandBlock at (%d,%d,%d) failed to handle command", listener.getPosition().getX(), listener.getPosition().getY(), listener.getPosition().getZ()), exception);
                } else {
                    net.minecraft.server.MinecraftServer.getServer().server.getLogger().log(java.util.logging.Level.WARNING, String.format("Unknown CommandBlock failed to handle command"), exception);
                }
            }
        }

        return completed;
    }
    private static java.util.ArrayList<String[]> buildCommands(ICommandSender sender, String[] args, int pos) throws net.minecraft.command.CommandException {
        java.util.ArrayList<String[]> commands = new java.util.ArrayList<String[]>();
        java.util.List<net.minecraft.entity.player.EntityPlayerMP> players = (java.util.List<net.minecraft.entity.player.EntityPlayerMP>)net.minecraft.command.EntitySelector.matchEntities(sender, args[pos], net.minecraft.entity.player.EntityPlayerMP.class);
        if (players != null) {
            for (net.minecraft.entity.player.EntityPlayerMP player : players) {
                if (player.world != sender.getEntityWorld()) {
                    continue;
                }
                String[] command = args.clone();
                command[pos] = player.getName();
                commands.add(command);
            }
        }
        return commands;
    }
    public static org.bukkit.command.CommandSender unwrapSender(ICommandSender listener) {
        org.bukkit.command.CommandSender sender = null;
        while (sender == null) {
            if (listener instanceof net.minecraft.server.dedicated.DedicatedServer) {
                sender = ((net.minecraft.server.dedicated.DedicatedServer) listener).console;
            } else if (listener instanceof net.minecraft.network.rcon.RConConsoleSource) {
                sender = ((net.minecraft.network.rcon.RConConsoleSource) listener).getServer().remoteConsole;
            } else if (listener instanceof CommandBlockBaseLogic) {
                sender = ((CommandBlockBaseLogic) listener).sender;
            } else if (listener instanceof net.minecraft.advancements.FunctionManager.CustomFunctionListener) {
                sender = ((net.minecraft.advancements.FunctionManager.CustomFunctionListener) listener).sender;
            } else if (listener instanceof net.minecraft.command.CommandSenderWrapper) {
                listener = ((net.minecraft.command.CommandSenderWrapper) listener).delegate; // Search deeper
            } else if (org.bukkit.craftbukkit.command.VanillaCommandWrapper.lastSender != null) {
                sender = org.bukkit.craftbukkit.command.VanillaCommandWrapper.lastSender;
            } else if (listener.getCommandSenderEntity() != null) {
                sender = listener.getCommandSenderEntity().getBukkitEntity();
            } else {
                throw new RuntimeException("Unhandled executor " + listener.getClass().getSimpleName());
            }
        }
        return sender;
    } // CraftBukkit end

    public String getName()
    {
        return this.customName;
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    public void sendMessage(ITextComponent component)
    {
        if (this.trackOutput && this.getEntityWorld() != null && !this.getEntityWorld().isRemote)
        {
            this.lastOutput = (new TextComponentString("[" + TIMESTAMP_FORMAT.format(new Date()) + "] ")).appendSibling(component);
            this.updateCommand();
        }
    }

    public boolean sendCommandFeedback()
    {
        MinecraftServer minecraftserver = this.getServer();
        return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worlds[0].getGameRules().getBoolean("commandBlockOutput");
    }

    public void setCommandStat(CommandResultStats.Type type, int amount)
    {
        this.resultStats.setCommandStatForSender(this.getServer(), this, type, amount);
    }

    public abstract void updateCommand();

    @SideOnly(Side.CLIENT)
    public abstract int getCommandBlockType();

    @SideOnly(Side.CLIENT)
    public abstract void fillInInfo(ByteBuf buf);

    public void setLastOutput(@Nullable ITextComponent lastOutputMessage)
    {
        this.lastOutput = lastOutputMessage;
    }

    public void setTrackOutput(boolean shouldTrackOutput)
    {
        this.trackOutput = shouldTrackOutput;
    }

    public boolean shouldTrackOutput()
    {
        return this.trackOutput;
    }

    public boolean tryOpenEditCommandBlock(EntityPlayer playerIn)
    {
        if (!playerIn.canUseCommandBlock())
        {
            return false;
        }
        else
        {
            if (playerIn.getEntityWorld().isRemote)
            {
                playerIn.displayGuiEditCommandCart(this);
            }

            return true;
        }
    }

    public CommandResultStats getCommandResultStats()
    {
        return this.resultStats;
    }
}