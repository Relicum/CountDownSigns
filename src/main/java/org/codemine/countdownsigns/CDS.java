package org.codemine.countdownsigns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
    private Explosion explosion;
    private CommandManager cm;
    private Map<String,Object> signMap;

    /**
     * Utility method for getting a plugins Main JavaPlugin Class
     *
     * @return CDS a static instance of the main plugin Class
     */
    public static CDS getInstance() {
        return instance;

    }

    public void onEnable() {
        ConfigurationSerialization.registerClass(BlockLoc.class);
        ConfigurationSerialization.registerClass(SignTimer.class);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new SignChange(this), this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        this.debug=getConfig().getBoolean("sign.settings.debug", false);
        instance=this;
        cm=new CommandManager(this);
        getCommand("cds").setExecutor(cm);
        getCommand("cds").setTabCompleter(cm);
        getCommand("cds toggle").setExecutor(cm);
        getCommand("cds status").setExecutor(cm);
        getCommand("cds help").setExecutor(cm);
        getCommand("cds help").setTabCompleter(cm);
        getCommand("cds remove").setExecutor(cm);
        getCommand("cds remove").setTabCompleter(cm);
        SignTimer signTimer;
        if(getConfig().contains("sign.signtimer")) {
            System.out.println("It has found sign timer");
            signTimer=(SignTimer) getConfig().get("sign.signtimer");
            //System.out.println("The block is ");
            if(!signTimer.isPaused()) {
                task=new RunningSign(this, signTimer);

            }
        }

    }

    public void onDisable() {
        if(getServer().getScheduler().getPendingTasks().size() > 0) {
            for(BukkitTask bukkitTask : getServer().getScheduler().getPendingTasks()) {
                System.out.println("The task is owned by " + bukkitTask.getOwner().getName());
            }

        }
        System.out.println("Number of Active workers is " + getServer().getScheduler().getActiveWorkers().size());
        if(task != null && task.isRunning()) {
            SignTimer timer=task.getSignTimer();
            getConfig().set("sign.signtimer", timer);
            MessageUtil.logInfoFormatted("Saving the Count Downs State");
            task.cancelTheTask();
        }
        saveConfig();
    }

    public void onPlay(final BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.GRASS) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                @Override
                public void run() {
                    BlockState bt=e.getBlock().getState();
                    bt.setType(Material.GRASS);
                    bt.update(true);
                }
            }, 1l);

            //bt.setType(Material.GRASS);
            // bt.update(true);
            getConfig().set("sign.settings.test", "hello");
            saveConfig();
            reloadConfig();
            e.setCancelled(true);
            return;
        }

        e.getPlayer().sendMessage(ChatColor.GREEN + "Block broken was " + e.getBlock().getType().name());
        e.getPlayer().sendMessage("hello");
    }

    //@EventHandler(priority=EventPriority.HIGHEST)
    public void sspawn(CreatureSpawnEvent e) {
        if(e.getEntity().getType().equals(EntityType.CHICKEN)) {
            getServer().broadcastMessage(ChatColor.GREEN + "A Chicken has spawned into world with the id of :" + e.getEntity().getUniqueId().toString());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void despawn(EntityDeathEvent e) {
        if(e.getEntity().getType().equals(EntityType.CHICKEN)) {
            getServer().broadcastMessage(ChatColor.RED + "A Chicken has de spawned with id of: " + e.getEntity().getUniqueId().toString());
        }
    }

    public void onPlace(BlockPlaceEvent e) {
        if(e.getBlockPlaced().getType().isTransparent()) {
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
    public RunningSign getTask() {
        return task;
    }

    public void startTask() {
        try {
            task=null;
            task=new RunningSign(this, (SignTimer) getConfig().get("sign.signtimer"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setSignMap() {
        this.signMap=new HashMap<>();
        signMap=getTask().getSignTimerMap();

    }

    public boolean runExplosion(SignTimer signTimer) {
        startExplosion(signTimer);
        return true;
    }

    /**
     * Start explosion when the count down has finished
     *
     * @param signTimer the {@link org.codemine.countdownsigns.SignTimer}
     */
    public void startExplosion(SignTimer signTimer) {
        try {
            new Explosion(this, signTimer.getSignLocation().getLocation().getBlock().getState(),
                           signTimer.getExplodingLocation().getLocation().getBlock().getState(),
                           signTimer.isUseSound(),
                           signTimer.isExplosion(),
                           signTimer.isUseEffect()).runTaskLater(this, 1l);
        }
        catch(IllegalArgumentException | IllegalStateException e) {
            MessageUtil.logDebug(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }

    }

    public void setTaskToNull() {
        task=null;
    }

    /**
     * Is debugging on.
     *
     * @return the if the plugin is in debug mode
     */
    public boolean isDebug() {
        return debug;
    }

}
