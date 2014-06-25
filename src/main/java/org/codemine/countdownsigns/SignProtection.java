package org.codemine.countdownsigns;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

/**
 * Name: SignProtection.java Created: 25 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class SignProtection implements Listener {

    private final CDS plugin;

    private final Location location;

    private final Location block;

    //private final BlockState signState;

    //private final BlockState blockState;

    public SignProtection(CDS pl, Location loc) {

        this.plugin = pl;
        this.location = loc;
        this.block = loc.clone().subtract(0, 1, 0);
        // this.signState = loc.getBlock().getState();
        //this.blockState = loc.getBlock().getLocation().subtract(0, 1, 0).getBlock().getState();

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerBreak(BlockBreakEvent e) {

        if (e.getBlock().getLocation().equals(location) || e.getBlock().getLocation().equals(block)) {
            BlockState blockState = e.getBlock().getState();
            e.setCancelled(true);
            blockState.update(true);
        } else
            return;


        if (e.getPlayer().hasPermission("countdown.sign.place") || e.getPlayer().isOp()) {
            MessageUtil.sendAdminMessage(e.getPlayer(), "Even admins or op can not break the signs");
            MessageUtil.sendAdminMessage(e.getPlayer(), "To remove the sign &6/cds remove sign");
        } else {
            MessageUtil.sendErrorMessage(e.getPlayer(), "You can not break that sign");
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDamage(BlockDamageEvent e) {

        if (e.getBlock().getLocation().equals(location) || e.getBlock().getLocation().equals(block)) {
            BlockState blockState = e.getBlock().getState();
            e.setCancelled(true);
            blockState.update(true);

            if (e.getPlayer().hasPermission("countdown.sign.place") || e.getPlayer().isOp()) {
                MessageUtil.sendAdminMessage(e.getPlayer(), "Even admins or op can not damage the signs");
                MessageUtil.sendAdminMessage(e.getPlayer(), "To remove the sign &6/cds remove sign");
            } else {
                MessageUtil.sendErrorMessage(e.getPlayer(), "You can not damage or break that sign");
            }


        }
    }

}
