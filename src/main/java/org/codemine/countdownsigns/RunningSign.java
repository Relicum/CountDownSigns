package org.codemine.countdownsigns;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemine.schedule.BukkitRun;
import org.codemine.schedule.TimeUnit;

import java.util.Map;

/**
 * Name: RunningSign.java Created: 15 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class RunningSign extends BukkitRun {

    private final long WEEK = 12096000;
    private final long DAY = 1728000;
    private final long HOUR = 72000;
    private final long MINUTE = 1200;
    private final long SECOND = 20;
    private final Plugin plugin;
    private SignTimer signTimer;
    private final Location signLocation;
    private BlockState signState;
    private Sign sign;
    private boolean running = false;
    public RunningSign(Plugin plugin,SignTimer signTimer){
        this.plugin = plugin;
        this.signTimer = signTimer;
        this.signLocation = signTimer.getSignLocation().getLocation();
        this.signState = signTimer.getSignLocation().getLocation().getBlock().getState();
        if (!checkBlock()) {
            MessageUtil.logInfoFormatted("The location does not contain a sign");
            MessageUtil.logInfoFormatted("CountDown Is stopping");
            this.cancel();
        }
        this.runTaskTimer(plugin,100l,getSignTimer().getUpdatePeriod(),TimeUnit.TICKS);
        running = true;
        MessageUtil.logInfoFormatted("The Count down timer will start in 5 seconds");
    }
    public long getTotal(){
        return getSignTimer().getTimeLeft();
    }
    @Override
    public void run(){
        if (getSignTimer().isCompleted()) {
            MessageUtil.logInfoFormatted("The CountDownSign has completed, but the task still seems to be running,possible its still waiting to be shutdown");
            return;
        }
        if (getSignTimer().getTimeLeft() > 0) {
            String[] li = getPattern(getSignTimer().getTimeLeft());
            loadSign();
            sign.setLine(1,MessageUtil.addColor(li[0]));
            sign.setLine(2,MessageUtil.addColor(li[1]));
            signState.update(true);
            getSignTimer().decrementTimeLeft();
        }
        if (getSignTimer().getTimeLeft() <= 0) {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Boom block explode");
            MessageUtil.logInfoFormatted("CountDown on Sign has finished and the block is no more");
            getSignTimer().setCompleted(true);
            getSignTimer().setPaused(true);
            running = false;
            plugin.getConfig().set("sign.signtimer",getSignTimer());
            plugin.saveConfig();
            MessageUtil.logInfoFormatted("The Timer should stop on the next tick");
            //wait till next tick to cancel the task
            new BukkitRunnable() {

                @Override
                public void run(){
                    cancelTheTask();
                }
            }.runTaskLater(plugin,1l);

        }

    }
    protected void cancelTheTask(){
        MessageUtil.logInfoFormatted("The Count Down Timer Task was been successfully stopped");
        running = false;
        this.cancel();
    }
    //Check chunk is loaded
    protected boolean chunkLoaded(){
        return signLocation.getChunk().isLoaded();
    }
    //Load the chunk
    protected boolean loadChunk(){
        return signLocation.getChunk().load();
    }
    //Check the block is still at that location
    protected boolean checkBlock(){
        return signLocation.getBlock().getType().name().contains("SIGN");
    }
    protected void loadSign(){
        this.sign = (Sign)signState;

    }
    public synchronized SignTimer getSignTimer(){
        return signTimer;
    }
    public Map<String,Object> getSignTimerMap(){
        return getSignTimer().serialize();
    }
    public void setDelayedCancel(long delay){
        new BukkitRunnable() {

            @Override
            public void run(){
                cancelTheTask();
            }
        }.runTaskLater(plugin,delay);
    }
    public boolean isRunning(){
        return running;
    }
    public void setRunning(boolean running){
        this.running = running;
    }
    public String[] getPattern(long left){
        long remaining;
        String[] lines = new String[2];
        StrBuilder sb = new StrBuilder();
        sb.append("&b&l");
        if (left > DAY) {
            lines[0] = "&a&l" + (left/DAY) + " Days";
            remaining = left%DAY;
        } else {
            lines[0] = "&a&l 0 Days";
            remaining = left;
        }
        if (remaining > HOUR) {
            sb.append(remaining/HOUR).append("H-");
            remaining = remaining%HOUR;
        } else {
            sb.append("0").append("H-");
        }
        if (remaining > MINUTE) {
            sb.append(remaining/MINUTE).append("M-");
            remaining = remaining%MINUTE;
        } else {
            sb.append("0").append("M-");
        }
        if (remaining > SECOND) {
            sb.append(remaining/SECOND).append("S");
        } else {
            sb.append("0").append("S");
        }
        lines[1] = sb.toString();
        return lines;
        // return "%DAY%:D %HOUR%:H %MIN%:M";
    }
}
