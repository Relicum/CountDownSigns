package org.codemine.countdownsigns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

/**
 * Name: SignCountDownStartEvent.java Created: 15 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class SignCountDownStartEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Sign sign;
    private long period;
    private long total;
    private Location location;
    /**
     * Instantiates a new Sign count down start event.
     *
     * @param block  the Sign Block
     * @param period the period between updates
     * @param total  the total time to run before the Block below explodes
     */
    public SignCountDownStartEvent(Block block,long period,long total){
        super(block);
        if (!(block instanceof Sign)) {
            this.setCancelled(true);
            System.out.println("Error: Block was not an instance of a sign");
            return;
        }
        this.sign = (Sign)block;
        this.period = period;
        this.total = total;
        this.location = block.getLocation();

    }
    /**
     * Gets sign.
     *
     * @return the sign block
     */
    public Sign getSign(){
        return sign;
    }
    /**
     * Gets total remain time in milli Seconds
     *
     * @return the total time left until the block explodes
     */
    public long getTotal(){
        return total;
    }
    /**
     * Gets period.
     *
     * @return the period between tasks in milli seconds
     */
    public long getPeriod(){
        return period;
    }
    /**
     * Gets location of the sign block
     *
     * @return the location of the sign block
     */
    public Location getLocation(){
        return location;
    }
    @Override
    public HandlerList getHandlers(){
        return handlers;

    }
    public static HandlerList getHandlerList(){
        return handlers;

    }
    @Override
    public boolean isCancelled(){
        return cancelled;

    }
    @Override
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;

    }
}
