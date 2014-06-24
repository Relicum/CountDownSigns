package org.codemine.countdownsigns;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.codemine.jchatter.JChat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Name: CommandManager.java Created: 19 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class CommandManager implements TabExecutor {

    private final CDS plugin;
    private final List<String> TAB1=ImmutableList.of("help", "setup", "toggle", "remove", "status");
    private final List<String> TAB2=ImmutableList.of("setup", "toggle", "remove", "status");
    private final List<String> REMOVE=ImmutableList.of("chickens", "sign", "help");

    private JChat jChat;

    public CommandManager(CDS pl) {
        plugin=pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {

            sender.sendMessage(ChatColor.RED + "You can not run commands from the console");
            return true;
        }

        Player player=(Player) sender;
        if(cmd.getName().equalsIgnoreCase("cds") && args.length == 0) {

            showSetUpHelp(player);
            return true;
        }

        Validate.notNull(args, "Error you must use at least one command after cds");
        //Parse args provided there is at least one arg
        if(args.length >= 1) {

            //Displays the status of timer
            if(args[0].equalsIgnoreCase("toggle") && (player.hasPermission("countdown.sign.toggle") || player.isOp())) {
                if(plugin.getTask() != null && plugin.getTask().isRunning()) {
                    plugin.getTask().cancelTheTask();
                    MessageUtil.sendMessage(player, "The countdown sign has been toggle &6OFF");
                    return true;
                }
                else {
                    if(plugin.getConfig().contains("sign.signtimer")) {
                        SignTimer signTimer=(SignTimer) plugin.getConfig().get("sign.signtimer");
                        if(signTimer.getTimeLeft() == 0) {
                            MessageUtil.sendErrorMessage(player, "This count down has already ended");
                            return true;
                        }
                        else {
                            plugin.startTask();
                            MessageUtil.sendMessage(player, "The countdown sign has been toggle &6ON");
                            return true;
                        }
                    }
                    MessageUtil.sendErrorMessage(player, "Count down is not running as there are no settings in the config file for it");
                    return true;
                }
            }
            //Help section
            if(args[0].equalsIgnoreCase("help") && (player.hasPermission("countdown.sign.help") || player.isOp())) {
                if(args.length == 1) {
                    showSetUpHelp(player);
                    player.sendMessage("");
                    return true;
                }
                System.out.println("length=" + args.length);
                if(args.length == 2) {
                    if(args[1].equalsIgnoreCase("setup")) {
                        MessageUtil.clearChat(player);
                        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.GREEN, ChatColor.BLUE, ChatColor.ITALIC, '-'));
                        MessageUtil.sendRawMessage(player, MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, "   How to setup the sign"));
                        MessageUtil.sendRawMessage(player, "&6On the first line of the sign type &a[CDS]");
                        MessageUtil.sendAdminMessage(player, "&aThe 2nd line requires a min of 1, max 3 time periods");
                        MessageUtil.sendAdminMessage(player, "&6Formats: &93&f:&4D &97&f:&4H &945&f:&4M &921&f:&4S");
                        MessageUtil.sendAdminMessage(player, "&aSo for 1 day you would add &b1&f:&bD &a or &b1&f:&bd");
                        MessageUtil.sendAdminMessage(player, "&aMore than one period use single space to separate them");
                        MessageUtil.sendAdminMessage(player, "&6Example: &92&f:&4D&f &94&f:&4H&f &930&f:&4M &a add the format on 2nd line of sign");
                        player.sendMessage("");
                        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.BLUE, ChatColor.GREEN, ChatColor.ITALIC, '-'));
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("toggle")) {
                        MessageUtil.clearChat(player);
                        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.GREEN, ChatColor.BLUE, ChatColor.ITALIC, '-'));
                        MessageUtil.sendRawMessage(player, MessageUtil.centeredHeading(ChatColor.DARK_RED, ChatColor.ITALIC, "Toggle the Countdown On and Off "));
                        MessageUtil.sendAdminMessage(player, "Default the Countdown won't start until toggled on");
                        showToggle(player);
                        MessageUtil.sendRawMessage(player, " ");
                        MessageUtil.sendAdminMessage(player, "Use the status command to check sign status");
                        showStatus(player);
                        MessageUtil.sendRawMessage(player, " ");
                        MessageUtil.sendRawMessage(player, MessageUtil.fullline(ChatColor.BLUE, ChatColor.GREEN, ChatColor.ITALIC, '-'));

                        return true;
                    }
                    if(args[1].equalsIgnoreCase("remove")) {
                        MessageUtil.sendMessage(player, "help remove selected");
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("status")) {
                        MessageUtil.sendMessage(player, "help status selected");
                        return true;
                    }
                }

                MessageUtil.sendErrorMessage(player, "To many arguments for help command");
                return true;

            }

            //Status section
            if(args[0].equalsIgnoreCase("status") && (player.hasPermission("countdown.sign.status") || player.isOp())) {
                if(plugin.getTask() != null && plugin.getTask().isRunning()) {
                    MessageUtil.sendMessage(player, "The countdown sign is currently &6RUNNING");
                    return true;
                }
                else {
                    if(plugin.getConfig().contains("sign.signtimer")) {
                        if(!(plugin.getConfig().get("sign.signtimer") instanceof SignTimer)) {
                            MessageUtil.sendMessage(player, "The count down is not running as configs are not an instance of SignTimer");
                            return true;
                        }
                        SignTimer signTimer=(SignTimer) plugin.getConfig().get("sign.signtimer");
                        if(signTimer.getTimeLeft() == 0) {
                            MessageUtil.sendMessage(player, "This countdown sign is not running and has finished its countdown");
                            return true;
                        }
                        else {
                            MessageUtil.sendMessage(player, "The countdown sign is currently &6PAUSED");
                            return true;
                        }
                    }
                    MessageUtil.sendMessage(player, "Count down is not running, no settings in config file for a sign");
                    return true;
                }

            }

            //Remove Sign and configs section
            if(args[0].equalsIgnoreCase("remove") && (player.hasPermission("countdown.sign.remove") || player.isOp())) {

                if(args.length >= 2) {

                    if(args[1].equals("chickens")) {

                        List<Entity> syncList=Collections.synchronizedList(player.getNearbyEntities(30.0d, 30.0d, 30.0d));

                        if(!syncList.isEmpty()) {

                            synchronized(syncList) {
                                ListIterator<Entity> iterator=syncList.listIterator();

                                try {

                                    while(iterator.hasNext()) {

                                        removeTheChicks(iterator.next(), player);
                                    }

                                    iterator=null;
                                    syncList=null;
                                    MessageUtil.debugGameMessage(player, "It would appear that all chickens were successfully marked for removal");

                                }
                                catch(Exception e) {

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
                    if(args[1].equals("sign")) {

                        SignTimer tmp;

                        //If is currently running stop it.
                        if(plugin.getTask() != null && plugin.getTask().isRunning()) {

                            plugin.getTask().cancelTheTask();
                        }
                        if(plugin.getConfig().get("sign.signtimer", "FAIL") == "FAIL") {

                            MessageUtil.sendErrorMessage(player, "Error: Unable to remove sign, you do not have a sign setup!");
                            return true;
                        }

                        tmp=(SignTimer) plugin.getConfig().get("sign.signtimer");
                        //delete the sign
                        if(tmp.getSignLocation().getLocation().getBlock().getType().name().contains("SIGN")) {

                            try {

                                BlockState blockState=tmp.getSignLocation().getLocation().getBlock().getState();
                                blockState.setType(Material.AIR);
                                blockState.update(true);

                            }
                            catch(Exception e) {

                                MessageUtil.sendErrorMessage(player, "Error happened while deleting the sign it self");
                                e.printStackTrace();
                                return true;
                            }

                        }

                        try {

                            plugin.getConfig().getConfigurationSection("sign").set("signtimer", null);
                            plugin.saveConfig();
                            plugin.reloadConfig();
                            plugin.setTaskToNull();
                            tmp=null;

                        }
                        catch(Exception e) {

                            MessageUtil.sendErrorMessage(player, "Error while trying to delete sign timer configs from file");
                            e.printStackTrace();
                            return true;
                        }

                        MessageUtil.sendMessage(player, "You have successfully removed the sign and its config");

                        return true;
                    }

                    //Help for remove command
                    if(args[1].equalsIgnoreCase("help")) {

                        player.performCommand("cds help remove");
                        return true;
                    }
                }
                MessageUtil.sendErrorMessage(player, "Invalid command usage: /cds remove help|chickens|sign");
                return true;
            }//End of remove command
            MessageUtil.sendErrorMessage(player, "Command not found " + cmd.getName() + " " + Arrays.toString(args));
            return true;
        }//end of any cds command
        System.out.println("Reach the bottom of command block SHOULD NEVER SEE THIS if you do contact Relicum");
        System.out.println(Arrays.toString(args));
        return true;

    }

    //This method removes all the chickens spawned from explosion
    private boolean removeTheChicks(Entity entity, Player player) {
        if(entity.getType().equals(EntityType.CHICKEN)) {
            if(entity.isValid()) {
                entity.remove();
                MessageUtil.sendMessage(player, "A Chicken has been marked for removal");
                return true;
            }
            else {
                MessageUtil.sendMessage(player, "The Chicken was already marked for removal");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("cds") && args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], TAB1, new ArrayList<String>(TAB1.size()));
        }
        if(args.length == 2) {
            if(cmd.getName().equalsIgnoreCase("cds") && args[0].equalsIgnoreCase("help")) {

                return StringUtil.copyPartialMatches(args[1], TAB2, new ArrayList<String>(TAB2.size()));

            }
            if(args[0].equalsIgnoreCase("remove")) {

                return StringUtil.copyPartialMatches(args[1], REMOVE, new ArrayList<String>(REMOVE.size()));

            }
        }

        return ImmutableList.of();
    }

    public JChat getjChat() {
        jChat=new JChat("[");
        jChat.color(ChatColor.DARK_PURPLE)
             .then("CDS")
             .color(ChatColor.AQUA)
             .then("]")
             .color(ChatColor.DARK_PURPLE)
             .then(" ");
        return jChat;

    }

    public void showSetUpHelp(Player player) {
        getjChat()
          .then("Need help? ")
          .color(ChatColor.GREEN)
          .then("SETUP")
          .color(ChatColor.BLUE)
          .style(ChatColor.BOLD, ChatColor.UNDERLINE)
          .itemTooltip("Setup Guide", Arrays.asList("&cIf you need help setting up a sign", " ", "&bThen just click the link"))
          .command("/cds help setup")
          .then(" ")
          .color(ChatColor.GREEN)
          .then("TOGGLE")
          .color(ChatColor.RED)
          .style(ChatColor.BOLD, ChatColor.UNDERLINE)
          .itemTooltip("Toggle Guide", Arrays.asList("&cIf you need help starting and stopping the countdown", " ", "&bThen just click the link"))
          .command("/cds help toggle")
          .send(player);
    }

    public void showToggle(Player player) {

        getjChat()
          .then("Run the command: ")
          .color(ChatColor.GREEN)
          .then("/cds toggle")
          .color(ChatColor.GOLD)
          .style(ChatColor.ITALIC)
          .then(" or click ")
          .color(ChatColor.GREEN)
          .then("TOGGLE")
          .color(ChatColor.AQUA)
          .style(ChatColor.UNDERLINE, ChatColor.BOLD)
          .itemTooltip("Toggle Guide", Arrays.asList("&5The toggle command allows you to ", "stop and start the countdown.", " ",
                                                      "&6The countdown does not reset if you toggle it off", "or on. It will not reset on server restart", " ",
                                                      "&aRun &6/cds toggle &a or click on the link"))
          .command("/cds toggle")
          .send(player);

    }

    public void showStatus(Player player) {

        getjChat()
          .then("Run the command: ")
          .color(ChatColor.GREEN)
          .then("/cds status")
          .color(ChatColor.GOLD)
          .style(ChatColor.ITALIC)
          .then(" or click ")
          .color(ChatColor.GREEN)
          .then("STATUS")
          .color(ChatColor.BLUE)
          .style(ChatColor.UNDERLINE, ChatColor.BOLD)
          .tooltip(ChatColor.translateAlternateColorCodes('&', "&6Click to see Countdown status"))
          .command("/cds status")
          .then(" to view the status")
          .color(ChatColor.GREEN)
          .send(player);

    }

}
