package org.codemine.countdownsigns;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Name: CDS.java Created: 14 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class CDS extends JavaPlugin implements Listener {

    private static CDS instance;
    private boolean debug;
    private RunningSign task;
    private CommandManager cm;
    public void onEnable(){
        ConfigurationSerialization.registerClass(BlockLoc.class);
        ConfigurationSerialization.registerClass(SignTimer.class);
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getPluginManager().registerEvents(new SignChange(this),this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        this.debug = getConfig().getBoolean("sign.settings.debug",false);
        instance = this;
        cm = new CommandManager(this);
        getCommand("cdspause").setExecutor(cm);
        getCommand("cdsstatus").setExecutor(cm);
        getCommand("cdshelp").setExecutor(cm);
        getCommand("cdshelp").setTabCompleter(cm);
        SignTimer signTimer;
        if (getConfig().contains("sign.signtimer")) {
            System.out.println("It has found sign timer");
            signTimer = (SignTimer)getConfig().get("sign.signtimer");
            //System.out.println("The block is ");
            if (!signTimer.isPaused()) {
                task = new RunningSign(this,signTimer);

            }
        }
    }
    public void onDisable(){
        if (getServer().getScheduler().getPendingTasks().size() > 0) {
            for (BukkitTask bukkitTask : getServer().getScheduler().getPendingTasks()) {
                System.out.println("The task is owned by " + bukkitTask.getOwner().getName());
            }

        }
        System.out.println("Number of Active workers is " + getServer().getScheduler().getActiveWorkers().size());
        if (task != null && task.isRunning()) {
            SignTimer timer = task.getSignTimer();
            getConfig().set("sign.signtimer",timer);
            MessageUtil.logInfoFormatted("Saving the Count Downs State");
            task.cancelTheTask();
        }
        saveConfig();
    }
    /**
     * Utility method for getting a plugins Main JavaPlugin Class
     *
     * @return CDS a static instance of the main plugin Class
     */
    public static CDS getInstance(){
        return instance;

    }
    public void onPlay(BlockBreakEvent e){
        e.getPlayer().sendMessage(ChatColor.GREEN + "Block broken was " + e.getBlock().getType().name());
        e.getPlayer().sendMessage("hello");
    }
    public void onPlace(BlockPlaceEvent e){
        if (e.getBlockPlaced().getType().isTransparent()) {
            e.setCancelled(true);
            return;
        }
        e.getPlayer().sendMessage(ChatColor.AQUA + "Block placed is " + e.getBlock().getType().name());

    }
    /**
     * Get the Task that is running the countdown sign
     *
     * @return the running sign
     */
    public RunningSign getTask(){
        return task;
    }
    public void startTask(){
        try {
            task = null;
            task = new RunningSign(this,(SignTimer)getConfig().get("sign.signtimer"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Is debugging on.
     *
     * @return the if the plugin is in debug mode
     */
    public boolean isDebug(){
        return debug;
    }

}
