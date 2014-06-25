package org.codemine.countdownsigns;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.codemine.schedule.BukkitRun;

import java.util.Map;
import java.util.logging.Level;

/**
 * Runnable that is used to control the countdown.
 * <p>Custom BukkitRunnable {@link org.codemine.schedule.BukkitRun} that uses a custom {@link org.codemine.schedule.TimeUnit} to define time in Units
 * and not as real time.
 * <p>The {@link org.codemine.schedule.BukkitRun} is a direct copy of {@link org.bukkit.scheduler.BukkitRunnable} with 3 overload methods to take advantage
 * of the {@link org.codemine.schedule.TimeUnit} which in itself as an adaption of the class count in java's concurrent package {@link java.util.concurrent.TimeUnit}.
 * <p><tt>TimeUnit</tt> has been adapted to allow it to understand the minecraft Tick.
 *
 * @author Relicum
 * @version 0.0.1
 */
public class RunningSign extends BukkitRun {


    private final long DAY = 1728000;
    private final long HOUR = 72000;
    private final long MINUTE = 1200;
    private final long SECOND = 20;
    private final CDS plugin;
    private final Location signLocation;
    private SignTimer signTimer;
    private BlockState signState;
    private Sign sign;
    private boolean running = false;

    public RunningSign(Plugin plugin, SignTimer signTimer) {
        this.plugin = (CDS) plugin;
        this.signTimer = signTimer;
        this.signLocation = signTimer.getSignLocation().getLocation();
        this.signState = signTimer.getSignLocation().getLocation().getBlock().getState();
        if (!checkBlock()) {
            MessageUtil.logInfoFormatted("The location does not contain a sign");
            MessageUtil.logInfoFormatted("CountDown Is stopping");
            this.cancel();
        }
        running = true;
        MessageUtil.logInfoFormatted("The Count down timer will start in 5 seconds");
    }

    public long getTotal() {
        return getSignTimer().getTimeLeft();
    }

    @Override
    public void run() {
        if (getSignTimer().isCompleted()) {
            MessageUtil.logInfoFormatted("The CountDownSign has completed, but the task still seems to be running,possible its still waiting to be shutdown");
            return;
        }
        if (getSignTimer().getTimeLeft() > -1) {
            String[] li = getPattern(getSignTimer().getTimeLeft());
            loadSign();
            sign.setLine(1, MessageUtil.addColor(li[0]));
            sign.setLine(2, MessageUtil.addColor(li[1]));
            signState.update(true);
            getSignTimer().decrementTimeLeft();
            getSignTimer().setLine(1, li[0]);
            getSignTimer().setLine(2, li[1]);
        }
        if (getSignTimer().getTimeLeft() <= -1) {

            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Boom boom boom");
            getSignTimer().setCompleted(true);
            getSignTimer().setPaused(false);

            running = false;
            plugin.stopCountdownTask(false, true);
        }

    }

    public void cancelTheTask() {
        if (plugin.isDebug())
            MessageUtil.logDebug(Level.INFO, "The CDT Task stopped from file :" + this.getClass().getSimpleName());
        running = false;
        this.cancel();
    }

    //Check chunk is loaded
    protected boolean chunkLoaded() {
        return signLocation.getChunk().isLoaded();
    }

    //Load the chunk
    protected boolean loadChunk() {
        return signLocation.getChunk().load();
    }

    //Check the block is still at that location
    protected boolean checkBlock() {
        return signLocation.getBlock().getType().name().contains("SIGN");
    }

    protected void loadSign() {
        this.sign = (Sign) signState;

    }

    public synchronized SignTimer getSignTimer() {
        return signTimer;
    }

    public Map<String, Object> getSignTimerMap() {
        return getSignTimer().serialize();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String[] getPattern(long left) {
        long remaining;
        String[] lines = new String[2];
        StrBuilder sb = new StrBuilder();
        sb.append("&b&l");
        if (left > DAY) {
            lines[0] = "&a&l" + (left / DAY) + " Days";
            remaining = left % DAY;
        } else {
            lines[0] = "&a&l 0 Days";
            remaining = left;
        }
        if (remaining > HOUR) {
            sb.append(remaining / HOUR).append("H-");
            remaining = remaining % HOUR;
        } else {
            sb.append("0").append("H-");
        }
        if (remaining > MINUTE) {
            sb.append(remaining / MINUTE).append("M-");
            remaining = remaining % MINUTE;
        } else {
            sb.append("0").append("M-");
        }
        if (remaining > SECOND) {
            sb.append(remaining / SECOND).append("S");
        } else {
            sb.append("0").append("S");
        }
        lines[1] = sb.toString();
        return lines;
        // return "%DAY%:D %HOUR%:H %MIN%:M";
    }

    /**
     * Gets BlockState
     *
     * @return Value of signState.
     */
    public synchronized BlockState getSignState() {
        return signState;
    }

    /**
     * Gets signLocation.
     *
     * @return Value of signLocation.
     */
    public synchronized Location getSignLocation() {
        return signLocation;
    }
}
