package org.codemine.countdownsigns;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.codemine.jchatter.JChat;
import org.codemine.schedule.TimeUnit;

import java.util.Arrays;

/**
 * Creates the sign and the countdown length.
 *
 * @author Relicum
 * @version 0.0.1
 */
public class SignChange implements Listener {

    private final CDS plugin;
    private final long DAY = 1728000;
    private final long HOUR = 72000;
    private final long MINUTE = 1200;
    private final long SECOND = 20;
    private String displayFormat = "%sD:%sH:%sM:%sS";
    private JChat jChat;

    public SignChange(CDS plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignPlace(SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase(plugin.getConfig().getString("sign.settings.identifier", "[CDS]"))) {
            if (!e.getBlock().getType().name().equalsIgnoreCase("SIGN_POST")) {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "You must use a Sign Post that is on top of a block");
                return;
            }
            if (!e.getPlayer().hasPermission("countdown.sign.place") && !e.getPlayer().isOp()) {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "You do not have permission to place CountDown Signs");
                return;
            }
            if (e.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "The Sign Post must have a block directly below and be attached to it");
                return;
            }
            //All Checks Done now check time format is valid
            String[] times = e.getLine(1).trim().split(" ");
            if (times.length < 1 || times[0].isEmpty()) {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                e.getPlayer().performCommand("cds help setup");

                return;
            }
            long total = 0;
            String pattern = "dhms";
            //Parse Count Down Length
            for (int i = 0; i < times.length; i++) {
                String[] tmp = times[i].split(":");
                if (tmp.length != 2) {
                    MessageUtil.debugGameMessage(e.getPlayer(), "One of the Count Down Settings is in correct");
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    return;
                }
                //Test the period is valid
                if (!pattern.contains(tmp[1].toLowerCase())) {
                    MessageUtil.sendErrorMessage(e.getPlayer(), "Invalid period: " + tmp[1]);
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    throw new IllegalArgumentException("Invalid time period: " + tmp[1]);

                }
                //Test the value is an int
                int tmpNumber;
                try {
                    tmpNumber = Integer.parseInt(tmp[0]);
                } catch (NumberFormatException ne) {
                    MessageUtil.sendErrorMessage(e.getPlayer(), ne.getMessage());
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    ne.printStackTrace();
                    return;
                }
                //Past tests now add value to total
                switch (tmp[1]) {
                    case "d":
                    case "D":
                        total += DAY * tmpNumber;
                        break;
                    case "h":
                    case "H":
                        total += HOUR * tmpNumber;
                        break;
                    case "m":
                    case "M":
                        total += MINUTE * tmpNumber;
                        break;
                    case "s":
                    case "S":
                        total += SECOND * tmpNumber;
                        break;
                    default:
                        e.getPlayer().performCommand("cds help setup");
                        MessageUtil.sendErrorMessage(e.getPlayer(), "Invalid time period: " + tmp[1]);
                        e.getBlock().breakNaturally();
                        e.setCancelled(true);
                        throw new IllegalArgumentException("Invalid time period: " + tmp[1]);
                }

            }

            String[] lines = plugin.getConfig().getStringList("sign.lines").toArray(new String[4]);
            String[] line = getPattern(total);

            for (int i = 0; i < 4; i++) {
                if (i == 1) {

                    e.setLine(i, MessageUtil.addColor(line[0]));

                } else {
                    if (i == 2) {

                        e.setLine(i, MessageUtil.addColor(line[1]));

                    } else {
                        e.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
                    }
                }
            }

            SignTimer signTimer = new SignTimer();
            signTimer.setTimeLeft(total);
            signTimer.setUpdatePeriod(TimeUnit.SECONDS.toTicks(1));
            signTimer.setPaused(true);
            signTimer.setIdentifier(plugin.getConfig().getString("sign.settings.identifier", "[CDS]"));
            signTimer.setLine(0, lines[0]);
            signTimer.setLine(1, line[0]);
            signTimer.setLine(2, line[1]);
            signTimer.setLine(3, lines[3]);
            signTimer.setSignLocation(new BlockLoc(e.getBlock().getLocation().getBlockX(),
                    e.getBlock().getLocation().getBlockY(),
                    e.getBlock().getLocation().getBlockZ(),
                    e.getBlock().getLocation().getWorld().getName()));
            signTimer.setExplodingLocation(new BlockLoc(e.getBlock().getLocation().getBlockX(),
                    e.getBlock().getLocation().getBlockY() - 1,
                    e.getBlock().getLocation().getBlockZ(),
                    e.getBlock().getLocation().getWorld().getName()));
            try {
                plugin.setSignTimer(signTimer);
                plugin.toggle = false;
                plugin.saveSignTimer(true);
                plugin.registerProtection();


            } catch (Exception e1) {
                MessageUtil.sendErrorMessage(e.getPlayer(), "Error saving sign timer: " + e1.getMessage());
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                e1.printStackTrace();
                signTimer = null;
                line = null;
                lines = null;
                return;
            }
            signTimer = null;
            e.getBlock().getState().update(true);
            MessageUtil.clearChat(e.getPlayer(), 10);
            MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.fullline(ChatColor.GREEN, ChatColor.BLUE, ChatColor.ITALIC, '-'));
            MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, "New Countdown Sign Created"));
            e.getPlayer().sendRawMessage("");
            MessageUtil.sendMessage(e.getPlayer(), "Total number of Ticks is &6" + total);
            MessageUtil.sendMessage(e.getPlayer(), "Countdown set: " + line[0] + " " + line[1]);
            e.getPlayer().sendRawMessage("");
            showClickStart(e.getPlayer());
            MessageUtil.sendMessage(e.getPlayer(), "Or run the &6/cds toggle &a command to start it");
            MessageUtil.sendMessage(e.getPlayer(), "Toggle the Countdown as ofter as you like");
            MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.fullline(ChatColor.BLUE, ChatColor.GREEN, ChatColor.ITALIC, '-'));


        }
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

    public JChat getjChat() {
        jChat = new JChat("[");
        jChat.color(ChatColor.DARK_PURPLE)
                .then("CDS")
                .color(ChatColor.AQUA)
                .then("]")
                .color(ChatColor.DARK_PURPLE)
                .then(" ");
        return jChat;

    }

    public void showClickStart(Player player) {
        getjChat()
                .then("To start the CountDown click ")
                .color(ChatColor.GREEN)
                .then("START")
                .color(ChatColor.BLUE)
                .style(ChatColor.UNDERLINE)
                .itemTooltip("Click to Start Countdown", Arrays.asList("&cTo start the Countdown just click here", " ", "&bOr when you are ready run the command /cds toggle"))
                .command("/cds toggle")
                .send(player);
    }
}
