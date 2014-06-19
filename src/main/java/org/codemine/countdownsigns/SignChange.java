package org.codemine.countdownsigns;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.codemine.schedule.TimeUnit;

/**
 * Delays with the Placing of signs
 *
 * @author Relicum
 * @version 0.0.1
 */
public class SignChange implements Listener {

    private final CDS CDS;
    private final long WEEK = 12096000;
    private final long DAY = 1728000;
    private final long HOUR = 72000;
    private final long MINUTE = 1200;
    private final long SECOND = 20;
    private String displayFormat = "%sD:%sH:%sM:%sS";

    public SignChange(CDS CDS)
    {
        this.CDS = CDS;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignPlace(SignChangeEvent e)
    {
        if(e.getLine(0).equalsIgnoreCase(CDS.getConfig().getString("sign.settings.identifier", "[CDS]")))
        {
            if(!e.getBlock().getType().name().equalsIgnoreCase("SIGN_POST"))
            {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "You must use a Sign Post that is on top of a block");
                return;
            }
            if(!e.getPlayer().hasPermission("countdown.sign.place") && !e.getPlayer().isOp())
            {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "You do not have permission to place CountDown Signs");
                return;
            }
            if(e.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR))
            {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.sendErrorMessage(e.getPlayer(), "The Sign Post must have a block directly below and be attached to it");
                return;
            }
            //All Checks Done now check time format is valid
            String[] times = e.getLine(1).trim().split(" ");
            if(times.length < 1 || times[0].isEmpty())
            {
                e.getBlock().breakNaturally();
                e.setCancelled(true);
                MessageUtil.clearChat(e.getPlayer());
                MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.fullline(ChatColor.GREEN, ChatColor.BLUE, ChatColor.ITALIC, '-'));
                MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, "Invalid Count Down Format"));
                e.getPlayer().sendMessage("");
                MessageUtil.sendAdminMessage(e.getPlayer(), "&aThe valid format requires a minimum of 1 time frame");
                MessageUtil.sendAdminMessage(e.getPlayer(), "&6Formats: &91&f:&5W &93&f:&4D &97&f:&4H &945&f:&4N");
                MessageUtil.sendAdminMessage(e.getPlayer(), "&aSo for 1 day you would add &b1&f:&bD &a or &b1&f:&bd");
                MessageUtil.sendAdminMessage(e.getPlayer(), "&aMore than one period use single space to separate them");
                MessageUtil.sendAdminMessage(e.getPlayer(), "&6Example: &92&f:&4D&f &94&f:&4H&f &930&f:&4M &a add the format on 2nd line of sign");
                e.getPlayer().sendMessage("");
                MessageUtil.sendRawMessage(e.getPlayer(), MessageUtil.fullline(ChatColor.BLUE, ChatColor.GREEN, ChatColor.ITALIC, '-'));
                return;
            }
            long total = 0;
            String pattern = "wdhms";
            //Parse Count Down Length
            for(int i = 0 ; i < times.length ; i++)
            {
                String[] tmp = times[i].split(":");
                if(tmp.length != 2)
                {
                    MessageUtil.debugGameMessage(e.getPlayer(), "One of the Count Down Settings is in correct");
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    return;
                }
                //Test the period is valid
                if(!pattern.contains(tmp[1].toLowerCase()))
                {
                    MessageUtil.sendErrorMessage(e.getPlayer(), "Invalid period: " + tmp[1]);
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    throw new IllegalArgumentException("Invalid time period: " + tmp[1]);

                }
                //Test the value is an int
                int tmpNumber;
                try
                {
                    tmpNumber = Integer.parseInt(tmp[0]);
                }
                catch(NumberFormatException ne)
                {
                    MessageUtil.sendErrorMessage(e.getPlayer(), ne.getMessage());
                    e.getBlock().breakNaturally();
                    e.setCancelled(true);
                    ne.printStackTrace();
                    return;
                }
                //Past tests now add value to total
                switch(tmp[1])
                {
                    case "w":
                    case "W":
                        total += WEEK * tmpNumber;
                        break;
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
                        MessageUtil.sendErrorMessage(e.getPlayer(), "Invalid time period: " + tmp[1]);
                        e.getBlock().breakNaturally();
                        e.setCancelled(true);
                        throw new IllegalArgumentException("Invalid time period: " + tmp[1]);
                }

            }
            if(CDS.isDebug())
            {
                MessageUtil.debugGameMessage(e.getPlayer(), "Total number of milli seconds is " + total);
            }
            String[] lines = CDS.getConfig().getStringList("sign.lines").toArray(new String[4]);
            String[] line = getPattern(total);
            System.out.println(line[0]);
            System.out.println(line[1]);
            for(int i = 0 ; i < 4 ; i++)
            {
                if(i == 1)
                {
                    MessageUtil.sendAdminMessage(e.getPlayer(), "Should be writing to line 2 " + line[0]);
                    e.setLine(i, MessageUtil.addColor(line[0]));

                }
                else
                {
                    if(i == 2)
                    {
                        e.setLine(i, MessageUtil.addColor(line[1]));

                    }
                    else
                    {
                        e.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
                    }
                }
            }
            SignTimer signTimer = new SignTimer();
            signTimer.setTimeLeft(total);
            signTimer.setUpdatePeriod(TimeUnit.SECONDS.toTicks(1));
            signTimer.setIdentifier(CDS.getConfig().getString("sign.settings.identifier", "[CDS]"));
            signTimer.setLine(0, lines[0]);
            signTimer.setLine(1, line[0]);
            signTimer.setLine(2, line[1]);
            signTimer.setLine(3, lines[3]);
            signTimer.setSignLocation(new BlockLoc(e.getBlock().getLocation().getBlockX(), e.getBlock().getLocation().getBlockY(), e.getBlock().getLocation().getBlockZ(),
                                                    e.getBlock().getLocation().getWorld().getName()));
            signTimer.setExplodingLocation(new BlockLoc(e.getBlock().getLocation().getBlockX(), e.getBlock().getLocation().getBlockY() - 1, e.getBlock().getLocation().getBlockZ(),
                                                         e.getBlock().getLocation().getWorld().getName()));
            try
            {
                CDS.getConfig().set("sign.signtimer", signTimer);
                CDS.saveConfig();
                CDS.reloadConfig();
            }
            catch(Exception e1)
            {
                MessageUtil.sendErrorMessage(e.getPlayer(), "Error saving sign timer: " + e1.getMessage());
                e1.printStackTrace();
            }
            e.getBlock().getState().update(true);
            e.getPlayer().sendMessage("");
            MessageUtil.sendAdminMessage(e.getPlayer(), "New Sign Created");

        }
    }

    public String formatTime(long left)
    {
        long days = ((left / 1000) % 1728000);
        return "";

    }

    public String getDisplayFormat(int d, int h, int m, int s)
    {
        return String.format(displayFormat, String.valueOf(d), String.valueOf(h), String.valueOf(m), String.valueOf(s));
    }

    public String[] getPattern(long left)
    {
        long remaining;
        String[] lines = new String[2];
        StrBuilder sb = new StrBuilder();
        sb.append("&b&l");
        if(left > DAY)
        {
            lines[0] = "&a&l" + (left / DAY) + " Days";
            remaining = left % DAY;
        }
        else
        {
            lines[0] = "&a&l 0 Days";
            remaining = left;
        }
        if(remaining > HOUR)
        {
            sb.append(remaining / HOUR).append("H-");
            remaining = remaining % HOUR;
        }
        else
        {
            sb.append("0").append("H-");
        }
        if(remaining > MINUTE)
        {
            sb.append(remaining / MINUTE).append("M-");
            remaining = remaining % MINUTE;
        }
        else
        {
            sb.append("0").append("M-");
        }
        if(remaining > SECOND)
        {
            sb.append(remaining / SECOND).append("S");
        }
        else
        {
            sb.append("0").append("S");
        }
        lines[1] = sb.toString();
        return lines;
        // return "%DAY%:D %HOUR%:H %MIN%:M";
    }
}
