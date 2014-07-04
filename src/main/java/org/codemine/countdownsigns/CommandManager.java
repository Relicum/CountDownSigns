package org.codemine.countdownsigns;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.codemine.jchatter.JChat;
import org.codemine.jchatter.JSymbols;

import java.util.*;
import java.util.logging.Level;

import static org.bukkit.ChatColor.*;

/**
 * CommandManager managers the commands including Tab completion.
 *
 * @author Relicum
 * @version 0.0.1
 */
public class CommandManager implements TabExecutor {

    private final CDS plugin;
    private final List<String> TAB1 = ImmutableList.of("help", "toggle", "remove", "status", "tp", "set");
    private final List<String> TAB2 = ImmutableList.of("setup", "toggle", "remove", "status", "formats", "tp", "set");
    private final List<String> REMOVE = ImmutableList.of("chickens", "sign", "help");
    private final List<String> SETOP = ImmutableList.of("protection", "enabled", "help", "explosion", "autostart");
    private final List<String> SETT = ImmutableList.of("true", "false");

    private JChat jChat;

    public CommandManager(CDS pl) {
        plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {

            sender.sendMessage(ChatColor.RED + "You can not run commands from the console");
            return true;
        }

        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("cds") && args.length == 0) {

            setHelpHeader(player, ChatColor.AQUA + "   Need Help ? Select Below");
            player.sendMessage("");
            showSetUpHelp(player);
            player.sendMessage("");
            showWikiLink(player);
            player.sendMessage("");
            setMessageFooter(player);
            return true;
        }

        Validate.notNull(args, "Error you must use at least one command after cds");
        //Parse args provided there is at least one arg

        //-----------------------------

        //toggle the Sign Timer on and off
        if (args[0].equalsIgnoreCase("toggle") && (player.hasPermission("countdown.sign.toggle") || player.isOp())) {
            if (args.length >= 2) {
                player.performCommand("cds help toggle");
                return true;
            }
            int result = plugin.toggleCountdown();

            switch (result) {
                case -1:
                    MessageUtil.sendErrorMessage(player, "This count down has already ended");
                    break;
                case 1:
                    MessageUtil.sendMessage(player, "The countdown sign has been toggled &6OFF");
                    break;
                case 2:
                    MessageUtil.sendMessage(player, "The countdown sign has been toggled &6ON");
                    break;
                default:
                    MessageUtil.sendErrorMessage(player, "Count down is not running as there are no settings in the config file for it");
                    break;
            }
            return true;
        }

        //Help command
        if (args[0].equalsIgnoreCase("help") && (player.hasPermission("countdown.sign.help") || player.isOp())) {
            if (args.length == 1) {
                setHelpHeader(player, ChatColor.AQUA + "   Need Help ? Select Below");
                player.sendMessage("");
                showSetUpHelp(player);
                player.sendMessage("");
                showWikiLink(player);
                player.sendMessage("");
                setMessageFooter(player);
                return true;
            }

            if (args[1].equalsIgnoreCase("setup")) {

                setHelpHeader(player, "    How to setup the sign");
                MessageUtil.sendAdminMessage(player, "&6On the first line of the sign type &b[CDS]");
                MessageUtil.sendAdminMessage(player, "&aThe 2nd line requires a min of 1, max 3 time periods");
                MessageUtil.sendAdminMessage(player, "&6Formats: &93&f:&4D &97&f:&4H &945&f:&4M &921&f:&4S");
                MessageUtil.sendAdminMessage(player, "&aSo for 1 day you would add &b1&f:&bD &a or &b1&f:&bd");
                MessageUtil.sendAdminMessage(player, "&aMore than one period use single space to separate them");
                MessageUtil.sendAdminMessage(player, "&6Example: &92&f:&4D&f &94&f:&4H&f &930&f:&4M &a add the format on 2nd line of sign");

                player.sendMessage("");
                setMessageFooter(player);
                return true;
            }
            if (args[1].equalsIgnoreCase("toggle")) {

                setHelpHeader(player, "Toggle the Countdown On and Off");
                player.sendMessage("");
                MessageUtil.sendAdminMessage(player, "Default the Countdown won't start until toggled on");
                showToggle(player);
                player.sendMessage("");
                MessageUtil.sendAdminMessage(player, "Use the status command to check sign status");
                showStatus(player);
                player.sendMessage("");
                showWikiLink(player);
                setMessageFooter(player);

                return true;
            }
            if (args[1].equalsIgnoreCase("remove")) {

                setHelpHeader(player, " Removing the Sign or Chickens");
                player.sendMessage("");
                removeSignLink(player);
                player.sendMessage("");
                removeChickenLink(player);
                player.sendMessage("");
                showWikiLink(player);
                setMessageFooter(player);

                return true;
            }
            if (args[1].equalsIgnoreCase("tp")) {
                tptoSignLink(player);
                return true;
            }
            if (args[1].equalsIgnoreCase("status")) {
                showStatus(player);
                return true;
            }
            if (args[1].equalsIgnoreCase("formats")) {

                setHelpHeader(player, "Valid Sign Input Formats");
                MessageUtil.sendMessage(player, GOLD + "Add to Top Line: " + RED + "[CDS]");
                MessageUtil.sendRawMessage(player, MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, "Adding Time values and Units"));
                MessageUtil.sendMessage(player, GREEN + "Number of inputs is between 1 & 3 " + GOLD + "value:timeunit");
                MessageUtil.sendMessage(player, "All inputs on 2nd line with a space between them");
                MessageUtil.sendMessage(player, GOLD + "DAYS        " + JSymbols.WHITE_STAR + GREEN + " Min=1 Max=07" + RED + " D " + GREEN + "or " + RED + "d");
                MessageUtil.sendMessage(player, GOLD + "HOUR        " + JSymbols.WHITE_STAR + GREEN + " Min=1 Max=24" + RED + " H" + GREEN + " or " + RED + "h");
                MessageUtil.sendMessage(player, GOLD + "MINUTES    " + JSymbols.WHITE_STAR + GREEN + " Min=1 Max=60" + RED + " M" + GREEN + " or " + RED + "m");
                MessageUtil.sendMessage(player, GOLD + "SECOND     " + JSymbols.WHITE_STAR + GREEN + " Min=1 Max=60" + RED + " S" + GREEN + " or " + RED + "s");
                showWikiLink(player);
                setMessageFooter(player);

                return true;
            }

            if (args[1].equalsIgnoreCase("set")) {
                if (!plugin.checkSignTimerIsLoaded()) {
                    MessageUtil.sendErrorMessage(player, "Currently no countdown sign is setup");
                    showWikiLink(player);
                    return true;
                }
                setHelpHeader(player, "Current Countdown Settings");
                player.sendMessage("");
                if (!plugin.getSignTimer().isPaused())
                    MessageUtil.sendMessage(player, "Countdown Status: " + ChatColor.GOLD + "RUNNING");
                else
                    MessageUtil.sendMessage(player, "Countdown Status: " + ChatColor.GOLD + "PAUSED");
                MessageUtil.sendMessage(player, "Explosion: " + ChatColor.GOLD + String.valueOf(plugin.getSignTimer().isExplosion()).toUpperCase());
                MessageUtil.sendMessage(player, "Protection: " + ChatColor.GOLD + String.valueOf(plugin.getConfig().getBoolean("sign.settings.auto-protect")).toUpperCase());
                MessageUtil.sendMessage(player, "AutoStart: " + ChatColor.GOLD + String.valueOf(plugin.getSignTimer().isStartOnRestart()).toUpperCase());
                player.sendMessage("");
                showWikiLink(player);
                setMessageFooter(player);
                return true;
            }


            showSetUpHelp(player);
            return true;
        }

        //Status section
        if (args[0].equalsIgnoreCase("status") && (player.hasPermission("countdown.sign.status") || player.isOp())) {
            if (args.length >= 2) {
                player.performCommand("cds help status");
                return true;
            }

            if (plugin.getRunningSign() != null && plugin.getRunningSign().isRunning()) {
                MessageUtil.sendMessage(player, "The countdown sign is currently &b" + JSymbols.HEAVY_CHECK_MARK + " &6RUNNING");
                return true;
            }

            if (!plugin.containsSignTimer()) {
                MessageUtil.sendAdminMessage(player, "No Count down running, or scheduled to run.");
                return true;
            }

            if (plugin.containsSignTimer()) {
                if (!(plugin.getConfig().get("sign.signtimer") instanceof SignTimer)) {
                    MessageUtil.sendErrorMessage(player, "The count down is not running as configs are not an instance of SignTimer");
                    return true;
                }
                if (!plugin.isCountDownRunning()) {
                    MessageUtil.sendMessage(player, "The countdown sign is currently &b" + JSymbols.HEAVY_BALLOT_X + " &6PAUSED");
                    return true;

                }
                if (plugin.getSignTimer() != null && plugin.getSignTimer().isCompleted()) {
                    MessageUtil.sendAdminMessage(player, "This countdown sign is not running and has finished its countdown");
                    return true;
                }

            }

        }

        //Set command section
        if (args[0].equalsIgnoreCase("set") && (player.hasPermission("countdown.sign.set") || player.isOp())) {

            if (args.length == 1) {
                player.performCommand("cds set help");
                return true;
            }

            //Set Protection command
            if (args[1].equalsIgnoreCase("protection") && args.length == 3) {

                if (Boolean.valueOf(args[2]) == plugin.getConfig().getBoolean("sign.settings.auto-protect")) {
                    MessageUtil.sendAdminMessage(player, "Nothing to change protection is already set to " + args[2]);
                    return true;
                }

                if (Boolean.valueOf(args[2])) {

                    plugin.getConfig().set("sign.settings.auto-protect", true);
                    plugin.registerProtection();
                    MessageUtil.logDebug(Level.INFO, "Sign Protection has been Activated");
                    MessageUtil.sendAdminMessage(player, "Sign Protection has been Activated");

                } else {
                    plugin.getConfig().set("sign.settings.auto-protect", false);
                    plugin.unregisterEvent();
                    MessageUtil.logDebug(Level.INFO, "Sign Protection has been Deactivated");
                    MessageUtil.sendAdminMessage(player, "Sign Protection has been Deactivated");

                }

                plugin.saveConfig();

                return true;
            }
            if (args[1].equalsIgnoreCase("explosion") && args.length == 3) {

                if (!plugin.checkSignTimerIsLoaded()) {
                    MessageUtil.sendErrorMessage(player, "Unable to change Explosion value as no sign is currently defined");
                    return true;
                }

                if (Boolean.valueOf(args[2]) == plugin.getSignTimer().isExplosion()) {
                    MessageUtil.sendAdminMessage(player, "Nothing to change explosion is already set to " + args[2]);
                    return true;
                }


                if (!Boolean.valueOf(args[2])) {

                    plugin.getSignTimer().setExplosion(false);
                    MessageUtil.logDebug(Level.INFO, "Sign Explosion effect has been Deactivated");
                    MessageUtil.sendAdminMessage(player, "Sign Explosion effect has been Deactivated");
                } else {
                    plugin.getSignTimer().setExplosion(true);
                    MessageUtil.logDebug(Level.INFO, "Sign Explosion effect has been Activated");
                    MessageUtil.sendAdminMessage(player, "Sign Explosion effect has been Activated");
                }

                plugin.saveSignTimer(true);
                plugin.reloadConfig();
                return true;

            }

            if (args[1].equalsIgnoreCase("autostart") && args.length == 3) {

                if (!plugin.checkSignTimerIsLoaded()) {
                    MessageUtil.sendErrorMessage(player, "Unable to change AutoStart value as no sign is currently defined");
                    return true;
                }

                if (Boolean.valueOf(args[2]) == plugin.getSignTimer().isStartOnRestart()) {
                    MessageUtil.sendAdminMessage(player, "Nothing to change auto restart is already set to " + args[2]);
                    return true;
                }

                if (!Boolean.valueOf(args[2])) {

                    plugin.getSignTimer().setStartOnRestart(false);
                    MessageUtil.logDebug(Level.INFO, "AutoRestart has been Deactivated");
                    MessageUtil.sendAdminMessage(player, "AutoRestart has been Deactivated");
                } else {
                    plugin.getSignTimer().setStartOnRestart(true);
                    MessageUtil.logDebug(Level.INFO, "AutoRestart has been Activated");
                    MessageUtil.sendAdminMessage(player, "AutoRestart has been Activated");
                }

                plugin.saveSignTimer(true);
                plugin.reloadConfig();
                return true;

            }


            if (args[1].equalsIgnoreCase("help")) {
                player.performCommand("cds help set");
                return true;
            }

            MessageUtil.sendAdminMessage(player, "Command &6/cds set [args]&a used to set plugin settings WIP");
            return true;
        }

        //Remove section
        if (args[0].equalsIgnoreCase("remove") && (player.hasPermission("countdown.sign.remove") || player.isOp())) {

            if (args[1].equalsIgnoreCase("chickens")) {

                List<Entity> syncList = Collections.synchronizedList(player.getNearbyEntities(30.0d, 30.0d, 30.0d));

                if (!syncList.isEmpty()) {

                    synchronized (syncList) {
                        ListIterator<Entity> iterator = syncList.listIterator();

                        try {

                            while (iterator.hasNext()) {

                                removeTheChicks(iterator.next(), player);
                            }

                            iterator = null;
                            syncList = null;
                            MessageUtil.debugGameMessage(player, "It would appear that all chickens were successfully marked for removal");

                        } catch (Exception e) {

                            MessageUtil.sendErrorMessage(player, "Exception thrown while trying to remove chickens");
                            e.printStackTrace();
                            return true;

                        }

                    }

                    return true;
                }

                MessageUtil.sendAdminMessage(player, "No chickens found for removal");

                return true;

            }
            //Remove the sign and  sign configs
            if (args[1].equalsIgnoreCase("sign")) {


                if (!plugin.containsSignTimer() && !plugin.checkSignTimerIsLoaded()) {

                    MessageUtil.sendErrorMessage(player, "Error: Unable to remove sign, you do not have a sign setup! Or there are no records left");
                    return true;
                }

                if (plugin.isCountDownRunning()) {
                    try {
                        plugin.stopCountdownTask(false, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (plugin.getSignTimer().getSignLocation().getLocation().getBlock().getType().name().contains("SIGN")) {

                    if (plugin.removeABlock(plugin.getSignTimer().getSignLocation().getLocation())) {
                        MessageUtil.sendAdminMessage(player, "Sign Successfully removed");
                    } else {
                        MessageUtil.sendErrorMessage(player, "Unknown error deleting sign");
                        if (plugin.isDebug())
                            MessageUtil.logDebug(Level.SEVERE, "Unable to delete sign when using the remove command");
                    }
                }

                if (!plugin.deleteSignConfigs()) {
                    MessageUtil.sendErrorMessage(player, "Error trying to remove sign configs");
                    if (plugin.isDebug())
                        MessageUtil.logDebug(Level.SEVERE, "Failed to delete the signs configs when using the remove command");

                    return true;
                }

                plugin.setSignTimer(null);
                plugin.unregisterEvent();

                MessageUtil.sendAdminMessage(player, "Successfully removed the sign configs");

                return true;
            }
            if (args[1].equalsIgnoreCase("help")) {
                player.performCommand("cds help remove");
                return true;

            }
            MessageUtil.sendErrorMessage(player, "Invalid command usage: /cds remove help|chickens|sign");
            return true;
        }

        //Start of TP command
        if (args[0].equalsIgnoreCase("tp") && (player.hasPermission("countdown.sign.tp") || player.isOp())) {
            if (args.length >= 2) {
                player.performCommand("cds help tp");
                return true;
            }

            if (plugin.checkSignTimerIsLoaded()) {
                MessageUtil.sendMessage(player, "Teleporting you to sign location");
                player.teleport(plugin.getSignTimer().getSignLocation().getLocation());
                return true;
            }
            MessageUtil.sendErrorMessage(player, "No configs found: Unable to teleport you to Sign location.");
            return true;
        }

        System.out.println("Reach the bottom of command block SHOULD NEVER SEE THIS if you do contact Relicum");
        System.out.println(Arrays.toString(args));
        return true;

    }

    //This method removes all the chickens spawned from explosion
    private boolean removeTheChicks(Entity entity, Player player) {
        if (entity.getType().equals(EntityType.CHICKEN)) {
            if (entity.isValid()) {
                entity.remove();
                MessageUtil.sendMessage(player, "A Chicken has been marked for removal");
                return true;
            } else {
                MessageUtil.sendMessage(player, "The Chicken was already marked for removal");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("cds") && args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], TAB1, new ArrayList<String>(TAB1.size()));
        }
        if (args.length == 2) {
            if (cmd.getName().equalsIgnoreCase("cds") && args[0].equalsIgnoreCase("help")) {

                return StringUtil.copyPartialMatches(args[1], TAB2, new ArrayList<String>(TAB2.size()));

            }
            if (args[0].equalsIgnoreCase("remove")) {

                return StringUtil.copyPartialMatches(args[1], REMOVE, new ArrayList<String>(REMOVE.size()));

            }

            if (args[0].equalsIgnoreCase("set")) {

                return StringUtil.copyPartialMatches(args[1], SETOP, new ArrayList<String>(SETOP.size()));

            }
        }

        if (args.length == 3) {

            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("protection") || args[1].equalsIgnoreCase("explosion") || args[1].equalsIgnoreCase("autostart")) {

                    return StringUtil.copyPartialMatches(args[2], SETT, new ArrayList<String>(SETT.size()));
                }

            }

        }

        return ImmutableList.of();
    }

    protected void setHelpHeader(Player player, String title) {
        Validate.notNull(player, "Must mass a Player instance to set message header");
        Validate.isTrue((title.length() < 33), "Error: Max length the title can be is 32 chars");
        MessageUtil.clearChat(player, 30);
        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.GREEN, ChatColor.BLUE, ChatColor.ITALIC, '-'));
        MessageUtil.sendRawMessage(player, MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, title));

    }

    protected void setMessageFooter(Player player) {
        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.BLUE, ChatColor.GREEN, ChatColor.ITALIC, '-'));

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

    //TODO Handel exception correctly
    public void showSetUpHelp(Player player) {
        try {
            getjChat()
                    .then("SETUP")
                    .color(BLUE)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Setup Help", Arrays.asList("&cIf you need help setting up a sign", " ", "&bThen just click the link"))
                    .command("/cds help setup")
                    .then(" ")
                    .color(GREEN)
                    .then("TOGGLE")
                    .color(RED)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Toggle Help", Arrays.asList("&cIf you need help starting and stopping the countdown", " ", "&bThen just click the link"))
                    .command("/cds help toggle")
                    .then(" ")
                    .color(GREEN)
                    .then("REMOVE")
                    .color(AQUA)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Remove Help", Arrays.asList("&cIf you need help removing or deleting the countdown", "&cOr removing any chickens that are not", "&cremoved automatically", " ", "&bThen just click the link"))
                    .command("/cds help remove")
                    .then(" ")
                    .color(GREEN)
                    .then("SETTINGS")
                    .color(RED)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6How to change settings", Arrays.asList("&cThis section will show you how to", " ", "&cchange the following settings", " ", "&b* Sign Protection", "&b* AutoStart", "&b* Disable Explosion", "&b* Disable Plugin"))
                    .command("/cds help set")
                    .then(" ")
                    .color(GREEN)
                    .then("TP")
                    .color(BLUE)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Help using the TP command", Arrays.asList("&cA simple command to find the sign", " ", "&cTeleports you to the sign location if", "&cyou ever forget the location", " ", "&cRun the command &6/cds tp", " ", "&aOr click to teleport there now"))
                    .command("/cds tp")
                    .then(" ")
                    .color(GREEN)
                    .then("FORMATS")
                    .color(RED)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Help with sign input formats", Arrays.asList("&cHelp with sign inputs", " ", "&cDisplays all valid sign inputs", "&cand the correct formats", " ", "&aThen just click the link"))
                    .command("/cds help formats")
                    .then(" ")
                    .color(GREEN)
                    .then("STATUS")
                    .color(AQUA)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&6Displays the Countdown Status", Arrays.asList("&cSimple command to display the Status of the countdown", " ", "&cDisplays if the countdown is running", "&cor if paused or if its not set up", " ", "&aThen just click the link"))
                    .command("/cds help status")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWikiLink(Player player) {

        try {
            getjChat()
                    .then("Full help guide found at the wiki ")
                    .color(LIGHT_PURPLE)
                    .then("CLICK HERE")
                    .color(BLUE)
                    .style(ITALIC, UNDERLINE)
                    .tooltip("&aClick to visit the plugins wiki")
                    .link("https://github.com/Relicum/CountDownSigns/wiki")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCommand(Player player, String title, String cmdName, String cmd, List<String> lore, ChatColor messColor, ChatColor linkColor) {

        try {
            getjChat()
                    .then(title)
                    .color(messColor)
                    .then(cmdName)
                    .color(linkColor)
                    .style(UNDERLINE)
                    .itemTooltip(GOLD + title, lore)
                    .command(cmd)
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //TODO Handel exception correctly
    public void showToggle(Player player) {

        try {
            getjChat()
                    .then("Run the command: ")
                    .color(GREEN)
                    .then("/cds toggle")
                    .color(GOLD)
                    .style(ITALIC)
                    .tooltip("&aThe actual command format")
                    .then(" or click ")
                    .color(GREEN)
                    .then("TOGGLE")
                    .color(ChatColor.AQUA)
                    .style(ITALIC, UNDERLINE)
                    .itemTooltip("&bToggle Guide", Arrays.asList("&5The toggle command allows you to stop and start", "&5the countdown.", " ",
                            "&6The countdown does not reset if you toggle it off", "&6or on. It will not reset on server restart either", " ",
                            "&aRun &6/cds toggle &a or click on the link"))
                    .command("/cds toggle")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO Handel exception correctly
    public void showStatus(Player player) {

        try {
            getjChat()
                    .then("View Status: ")
                    .color(ChatColor.GREEN)
                    .then("/cds status")
                    .color(GOLD)
                    .style(ITALIC)
                    .tooltip("&aThe actual command format")
                    .then(" or click ")
                    .color(GREEN)
                    .then("STATUS")
                    .color(BLUE)
                    .style(ITALIC, UNDERLINE)
                    .tooltip("&6Click to see Countdown status", " ", "&aIt will also show the current", "&asettings for the countdown", " ", "&5NB: The settings view has", "&5not been implemented yet")
                    .command("/cds status")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO Handel exception correctly
    public void removeSignLink(Player player) {

        try {
            getjChat()
                    .then("Remove sign: ")
                    .color(ChatColor.GREEN)
                    .then("/cds remove sign")
                    .color(GOLD)
                    .style(ChatColor.ITALIC)
                    .tooltip("&aThe actual command format")
                    .then(" or click ")
                    .color(ChatColor.GREEN)
                    .then("REMOVE")
                    .color(ChatColor.BLUE)
                    .style(ChatColor.UNDERLINE, ChatColor.ITALIC)
                    .tooltip("&6Click to remove sign", " ", "&aThis will remove the sign and delete", "&athe configs from file", " ", "&5If the countdown is already running", "&5it will automatically be to stopped for you")
                    .command("/cds remove sign")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO Handel exception correctly
    public void removeChickenLink(Player player) {

        try {
            getjChat()
                    .then("Remove chickens: ")
                    .color(ChatColor.GREEN)
                    .then("/cds remove chickens")
                    .color(GOLD)
                    .style(ChatColor.ITALIC)
                    .tooltip("&aThe actual command format")
                    .then(" or click ")
                    .color(ChatColor.GREEN)
                    .then("REMOVE")
                    .color(ChatColor.BLUE)
                    .style(ChatColor.UNDERLINE, ChatColor.ITALIC)
                    .tooltip("&6Click to remove any left over chickens", " ", "&aUse if there is ever left over chickens", " ", "&5Mainly used when debugging")
                    .command("/cds remove chickens")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO Handel exception correctly
    public void tptoSignLink(Player player) {

        try {
            getjChat()
                    .then("To TP to sign ")
                    .color(ChatColor.GREEN)
                    .then(JSymbols.AIRPLANE + " ")
                    .color(GOLD)
                    .then(": click ")
                    .color(GREEN)
                    .then("TELEPORT")
                    .color(ChatColor.BLUE)
                    .style(ChatColor.UNDERLINE, ChatColor.ITALIC)
                    .tooltip("&6Click to teleport direct to sign countdown sign location", " ", "&aUseful if you forgot where you placed the sign")
                    .command("/cds tp")
                    .send(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
