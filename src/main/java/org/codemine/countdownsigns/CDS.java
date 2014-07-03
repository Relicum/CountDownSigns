package org.codemine.countdownsigns;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.codemine.schedule.TimeUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main Plugin file that currently provides a lot of the thread logic.
 *
 * @author Relicum
 * @version 0.0.1
 */
public class CDS extends JavaPlugin implements Listener {

    private static CDS instance;

    public boolean toggle = false;

    public boolean deleteConfigs = false;

    private boolean debug;

    private SignTimer signTimer;

    private RunningSign runningSign;

    private BukkitTask signTask;

    private Explosion explosion;

    private CommandManager cm;

    private Map<String, Object> signMap;

    private boolean saveOnDisable = true;

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

        this.debug = getConfig().getBoolean("sign.settings.debug");
        if (containsSignTimer()) {
            signTimer = (SignTimer) getConfig().get("sign.signtimer");
        }
        instance = this;
        cm = new CommandManager(this);
        getCommand("cds").setExecutor(cm);
        getCommand("cds").setTabCompleter(cm);
        getCommand("cds toggle").setExecutor(cm);
        getCommand("cds status").setExecutor(cm);
        getCommand("cds help").setExecutor(cm);
        getCommand("cds help").setTabCompleter(cm);
        getCommand("cds remove").setExecutor(cm);
        getCommand("cds remove").setTabCompleter(cm);
        getCommand("cds tp").setExecutor(cm);
        getCommand("cds set").setExecutor(cm);
        getCommand("cds set").setTabCompleter(cm);

        if (checkSignTimerIsLoaded()) {

            if (isDebug())
                MessageUtil.logDebug(Level.INFO, "A sign timer object has been found");

            registerProtection();

            //If the SignTimer Object is not set to paused then create a new
            //Repeating task passing in the SignTimer Object
            if (autoRestart()) {
                runningSign = startCountdownTask(true);
                signTask = runningSign.runTaskTimer(this, 100l, getSignTimer().getUpdatePeriod(), TimeUnit.TICKS);
                getSignTimer().setPaused(false);
                toggle = true;
            }
        }


    }

    public void onDisable() {
        if (getServer().getScheduler().getPendingTasks().size() > 0) {
            for (BukkitTask bukkitTask : getServer().getScheduler().getPendingTasks()) {
                System.out.println("The task is owned by " + bukkitTask.getOwner().getName());
            }

        }
        System.out.println("Number of Active workers is " + getServer().getScheduler().getActiveWorkers().size());


        if (isCountDownRunning()) {

            try {

                stopCountdownTask(false, false);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (deleteConfigs) {
            if (!deleteSignConfigs()) {
                if (isDebug())
                    MessageUtil.logDebug(Level.SEVERE, "Failed to delete sign configs");
            } else {
                MessageUtil.logDebug(Level.INFO, "Countdown Sign Configs Deleted");
            }
        }


        if (checkSignTimerIsLoaded()) {
            saveSignTimer(true);
            setSignTimer(null);

        }


        if (isSaveOnDisable()) {
            if (isDebug()) {
                MessageUtil.logDebug(Level.INFO, "Final save is having to be made");
            }

        }
        saveConfig();
    }

    /**
     * Register protection Listeners
     */
    public void registerProtection() {
        if (checkSignTimerIsLoaded()) {

            if (getConfig().getBoolean("sign.settings.auto-protect")) {
                getServer().getPluginManager().registerEvents(new SignProtection(this, getSignTimer().getSignLocation().getLocation()), this);

            }
        }
    }

    /**
     * Unregister BlockBreakEvent and BlockDamageEvent
     */
    public void unregisterEvent() {

        try {
            BlockBreakEvent.getHandlerList().unregister((org.bukkit.plugin.Plugin) this);
            BlockDamageEvent.getHandlerList().unregister((org.bukkit.plugin.Plugin) this);
        } catch (Exception e) {
            if (isDebug())
                MessageUtil.logDebug(Level.SEVERE, "Unable to unregister events Break and Damage");
            e.printStackTrace();
        }
    }

    /**
     * Get the Task that is running the countdown sign <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this method)
     *
     * @return the running sign
     */
    public synchronized RunningSign getRunningSign() {
        return runningSign;
    }

    /**
     * Set runningSign Property, or set to null;
     *
     * @param paramRunningSign set to an instance of {@link org.codemine.countdownsigns.RunningSign} or set to null
     */
    public synchronized void setRunningSign(RunningSign paramRunningSign) {

        if (paramRunningSign == null) {
            runningSign = null;
        } else {
            runningSign = paramRunningSign;
        }
    }

    private BukkitTask getSignTask() {
        return signTask;
    }

    public synchronized Explosion getExplosionTask() {
        return explosion;
    }

    /**
     * Is count down running.
     *
     * @return the boolean true and the task is running, false and its not
     */
    public boolean isCountDownRunning() {

        if (runningSign == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if the sign should be started on restarts
     *
     * @return the boolean true if the SignTimer object has running set to true. False and the SignTimer object has running set to false
     */
    public boolean autoRestart() {

        if (signTimer == null) return false;

        if (signTimer.isStartOnRestart()) return true;

        return false;

    }

    /**
     * Toggle countdown.
     *
     * @return boolean
     */
    public int toggleCountdown() {

        if (checkSignTimerIsLoaded() && signTimer.isCompleted()) {
            return -1;
        }

        if (!containsSignTimer()) {

            return 0;
        }

        if (!toggle) {
            try {
                MessageUtil.logInfoFormatted("Toggling On");
                getSignTimer().setPaused(false);

                runningSign = startCountdownTask(true);

                startSignTask(runningSign);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            toggle = true;
            return 2;

        } else {
            MessageUtil.logInfoFormatted("Toggling Off");
            getRunningSign().getSignTimer().setPaused(true);

            try {
                stopCountdownTask(true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(signTimer.toString());
            toggle = false;
            return 1;

        }

    }

    /**
     * Start a new Sign Countdown task.
     *
     * @param forceStart if set to true it will check if there is a current task running. If false if will just cancel any current running task and start a new one.
     * @return the boolean true if the Countdown task was started. False if there was a problem.
     * @throws IllegalStateException the illegal state exception then forceStart was set to false and a current task was found to be running
     * @throws NullPointerException  when trying to load SignTimer object from config file
     */
    private RunningSign startCountdownTask(boolean forceStart) throws IllegalStateException, NullPointerException {

        if (!forceStart) {
            if (runningSign != null) {
                throw new IllegalStateException("Unable to start new Countdown task as there is already one running");

            }
        } else {
            try {
                if (isCountDownRunning()) {
                    getSignTimer().setPaused(false);
                    setSignTimer(getRunningSign().getSignTimer());
                    saveSignTimer(true);
                    getSignTask().cancel();
                    getRunningSign().cancelTheTask();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        if (!checkSignTimerIsLoaded()) {
            loadSignTimer();
        }

        return new RunningSign(this, getSignTimer());
    }

    /**
     * Stop countdown task and optional start the Explosion task as well
     *
     * @param save           the save if true the SignTimer object will be save to disk.
     * @param startExplosion if true it will start the explosion task. False and it will just stop the Countdown task
     */
    public void stopCountdownTask(boolean save, boolean startExplosion) throws Exception {
        //Running and task completed

        if (!isCountDownRunning()) {
            if (isDebug()) {
                MessageUtil.logDebug(Level.SEVERE, "The stopCountdownTask method is returning the there are no countdowns running, this strong indicates a bug.");
                throw new Exception("No countdowns running but stopCountdownTask has been called ???");

            }
        }

        if (isCountDownRunning()) {
            getRunningSign().getSignTimer().setPaused(true);
            saveSignTimer(true);
            getSignTask().cancel();
            getRunningSign().cancelTheTask();


            setCountdownTaskToNull();

            if (runningSign != null || signTask != null) {
                if (isDebug()) {
                    MessageUtil.logDebug(Level.SEVERE, "Error: The RunningSign task or SignTask is not null even though we previously set it to null");
                }
                return;
            }

            if (isDebug())
                MessageUtil.logDebug(Level.INFO, "All Tasks connected to the sign countdown should now have been cancelled and nulled");


            if (deleteConfigs) {
                if (isDebug())
                    MessageUtil.logDebug(Level.INFO, "Delete Configs is set to true");
                if (deleteSignConfigs()) {
                    MessageUtil.logDebug(Level.INFO, "Successfully removed sign timer configs from file.");
                }
                if (isDebug()) {
                    MessageUtil.logDebug(Level.SEVERE, "FAILED to remove sign timer configs from file.");
                }
                deleteConfigs = false;
                toggle = false;
            }

            //Should of cancelled everything now ready to start countdown
            //First return all players that have explosion set to false.
            if (!startExplosion) {
                MessageUtil.logInfoFormatted("Countdown tasks are now shutdown");
                toggle = false;
                return;
            }


            //Start Explosion Effect
            if (getSignTimer().isExplosion()) {

                try {
                    startExplosion(getSignTimer());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                if (removeABlock(getSignTimer().getSignLocation().getLocation())) {

                    MessageUtil.logInfoFormatted("Sign Successfully Removed");
                } else {
                    MessageUtil.logServereFormatted("Unable to remove sign");
                }

                if (removeABlock(getSignTimer().getExplodingLocation().getLocation())) {
                    MessageUtil.logInfoFormatted("Block Successfully Removed");
                } else {
                    MessageUtil.logServereFormatted("Unable to remove block");
                }


                if (deleteSignConfigs()) {
                    MessageUtil.logInfoFormatted("Configs successfully removed from file");
                    unregisterEvent();
                    if (signTimer != null)
                        signTimer = null;
                } else MessageUtil.logServereFormatted("Unable to remove configs from file");



            }
        }

    } //here.............

    public void setSignMap() {
        this.signMap = new HashMap<>();
        signMap = getRunningSign().getSignTimerMap();

    }

    public boolean preStartExplosion() {

        if (!checkSignTimerIsLoaded()) {
            MessageUtil.logDebug(Level.SEVERE, "Can start Explosion no settings found in memory");
            return false;
        }

        startExplosion(getSignTimer());
        return true;
    }


    /**
     * Start explosion when the count down has finished
     *
     * @param signTimer the {@link org.codemine.countdownsigns.SignTimer}
     */
    protected void startExplosion(SignTimer signTimer) {
        try {
            deleteConfigs = true;
            explosion = new Explosion(this, getSignTimer().getSignLocation().getLocation().getBlock().getState(),
                    getSignTimer().getExplodingLocation().getLocation().getBlock().getState(),
                    getSignTimer().isUseSound(),
                    getSignTimer().isExplosion(),
                    getSignTimer().isUseEffect());
        } catch (IllegalArgumentException | IllegalStateException e) {
            MessageUtil.logDebug(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopExplosion() {

        if (getExplosionTask().innerRunning()) {
            try {
                getExplosionTask().getInnerRun().cancel();
                if (isDebug())
                    MessageUtil.logDebug(Level.INFO, "Inner Runner in Explosion has been successfully stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        deleteSignConfigs();
        deleteConfigs = false;
        toggle = false;
        if (isDebug())
            MessageUtil.logDebug(Level.INFO, "Configs were deleted from file as explosion has finished");

        if (getExplosionTask() != null && getExplosionTask().isMainRunning()) {

            try {
                getExplosionTask().cancel();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (isDebug())
                MessageUtil.logDebug(Level.INFO, "Main explosion task has now been fully stopped");

        }
        setExplosionTaskToNull();

        if (checkSignTimerIsLoaded()) {
            setSignTimer(null);

            if (isDebug())
                MessageUtil.logDebug(Level.INFO, "Sign Timer object has been set to null");
        }


        MessageUtil.logInfoFormatted("Explosion task has been successfully stopped");
    }




    /**
     * Sets countdown task to null.
     */
    public synchronized void setCountdownTaskToNull() {
        signTask = null;
        runningSign = null;
    }

    /**
     * Set explosion task to null.
     */
    public synchronized void setExplosionTaskToNull() {
        explosion = null;
    }

    /**
     * Is debugging on.
     *
     * @return the if the plugin is in debug mode
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Gets the {@link org.codemine.countdownsigns.SignTimer} object. <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this
     * method)
     *
     * @return the {@link org.codemine.countdownsigns.SignTimer} object.
     */
    public synchronized SignTimer getSignTimer() {
        return signTimer;
    }

    /**
     * Sets new {@link org.codemine.countdownsigns.SignTimer} object <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this
     * method)
     *
     * @param paramSignTimer set the field to a instance of {@link org.codemine.countdownsigns.SignTimer}
     */
    public synchronized void setSignTimer(SignTimer paramSignTimer) {

        if (paramSignTimer == null) {
            signTimer = null;
        } else {
            signTimer = paramSignTimer;
        }

    }

    /**
     * Load SignTimer object from disk and stores it in signTimer field
     *
     * @throws NullPointerException the null pointer exception if there is no record stored
     */
    public void loadSignTimer() throws NullPointerException {

        if (getSignTimer() != null) return;

        if (containsSignTimer()) {
            setSignTimer((SignTimer) getConfig().get("sign.signtimer"));
            setSaveOnDisable(true);
        } else {
            throw new NullPointerException("No record of SignTimer can be found in config file");
        }

    }

    /**
     * Reload sign timer.
     *
     * @throws NullPointerException if the config file does not contain a record of SignTimer
     */
    public void reloadSignTimer() throws NullPointerException {

        if (containsSignTimer()) {
            setSignTimer((SignTimer) getConfig().get("sign.signtimer"));
        } else {
            throw new NullPointerException("No record of SignTimer can be found in config file");
        }

    }

    /**
     * Checks to see if SignTimer is already loaded
     *
     * @return the boolean true and its is load, false and it is not loaded. False does not mean you can go ahead and load it as it may not even be set.
     */
    public boolean checkSignTimerIsLoaded() {

        if (getSignTimer() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Save SignTimer Object from Task back to Main Class.
     *
     * @param toDisk set to true for the SignTimer object to be saved back to disk at same time
     */
    public void saveSignTimer(boolean toDisk) {

        if (isCountDownRunning()) {

            setSignTimer(getRunningSign().getSignTimer());
        }

        if (!checkSignTimerIsLoaded()) {
            MessageUtil.logServereFormatted("There is no SignTimer object loaded");
            return;
        }

        getConfig().set("sign.signtimer", getSignTimer());

        if (toDisk) {
            saveConfig();
            setSaveOnDisable(false);
        } else setSaveOnDisable(true);
    }

    /**
     * Start sign task.
     *
     * @param paramSign the param sign
     */
    public void startSignTask(RunningSign paramSign) {

        this.signTask = paramSign.runTaskTimer(this, 0l, signTimer.getUpdatePeriod(), TimeUnit.TICKS);

    }

    /**
     * Checks to see if there is a stored instance of SignTimer.
     *
     * @return the true if there is a stored record false if not
     */
    public boolean containsSignTimer() {

        return getConfig().contains("sign.signtimer");
    }

    /**
     * Remove a block.
     *
     * @param loc the loc
     * @return the boolean
     */
    public boolean removeABlock(Location loc) {

        try {
            BlockState blockState = loc.getBlock().getState();
            blockState.setType(Material.AIR);
            blockState.update(true);
        } catch (Exception e) {
            if (debug) {
                e.printStackTrace();
            }
            return false;
        }

        return true;

    }

    public boolean deleteSignConfigs() {

        try {

            getConfig().getConfigurationSection("sign").set("signtimer", null);
            saveConfig();
            reloadConfig();
            deleteConfigs = false;
            toggle = false;
            if (isSaveOnDisable())
                setSaveOnDisable(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets saveOnDisable.
     *
     * @return Value of saveOnDisable.
     */
    public boolean isSaveOnDisable() {
        return saveOnDisable;
    }

    /**
     * Sets new saveOnDisable.
     *
     * @param saveOnDisable New value of saveOnDisable.
     */
    public void setSaveOnDisable(boolean saveOnDisable) {
        this.saveOnDisable = saveOnDisable;
    }
}
